package com.guru.app.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.guru.app.MainActivity
import com.guru.app.R
import com.guru.app.data.GuruDatabase
import com.guru.app.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {
    const val CHANNEL_ID = "guru_reminders"
    private const val EXTRA_ID = "reminder_id"
    const val ACTION_FIRE = "com.guru.app.FIRE"
    const val ACTION_DONE = "com.guru.app.DONE"
    const val ACTION_SNOOZE = "com.guru.app.SNOOZE"

    fun createChannel(context: Context) {
        val ch = NotificationChannel(CHANNEL_ID, "GuRu reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Meds, workouts, smoothies and meal alerts"
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    /** Next time this reminder should fire, respecting its weekday mask (bit0 = Monday). */
    fun nextTrigger(r: Reminder, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        if (r.daysMask == 0) return null
        for (offset in 0..7) {
            val candidate = now.toLocalDate().plusDays(offset.toLong()).atTime(r.hour, r.minute)
            val bit = 1 shl (candidate.dayOfWeek.value - 1)
            if (candidate.isAfter(now) && (r.daysMask and bit) != 0) return candidate
        }
        return null
    }

    private fun pending(context: Context, id: Long, action: String): PendingIntent {
        val i = Intent(context, ReminderReceiver::class.java).setAction(action).putExtra(EXTRA_ID, id)
        val slot = when (action) { ACTION_FIRE -> 0; ACTION_DONE -> 1; else -> 2 }
        val code = (id * 3 + slot).toInt()
        return PendingIntent.getBroadcast(context, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(context: Context, r: Reminder, at: LocalDateTime? = nextTrigger(r)) {
        at ?: return
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pending(context, r.id, ACTION_FIRE)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
    }

    fun cancel(context: Context, id: Long) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context, id, ACTION_FIRE))
    }

    fun idFrom(intent: Intent) = intent.getLongExtra(EXTRA_ID, -1)

    fun notify(context: Context, r: Reminder) {
        val open = PendingIntent.getActivity(
            context, r.id.toInt(), Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val emoji = when (r.type) {
            "Meds" -> "💊"; "Workout" -> "🏋️"; "Smoothie" -> "🥤"; "Meal" -> "🍽️"
            "Water" -> "💧"; "Supplements" -> "🌿"; else -> "🔔"
        }
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_guru)
            .setContentTitle("$emoji ${r.title}")
            .setContentText(r.note.ifBlank { "GuRu reminder – ${r.type}" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(if (r.type == "Meds") NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            // Actions also appear on paired Wear OS / Galaxy watches automatically
            .addAction(0, "Done", pending(context, r.id, ACTION_DONE))
            .addAction(0, "Snooze 10 min", pending(context, r.id, ACTION_SNOOZE))
            .extend(NotificationCompat.WearableExtender())
            .build()
        try { NotificationManagerCompat.from(context).notify(r.id.toInt(), n) } catch (_: SecurityException) {}
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = ReminderScheduler.idFrom(intent)
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val r = GuruDatabase.get(context).dao().reminder(id) ?: return@launch
                when (intent.action) {
                    ReminderScheduler.ACTION_FIRE -> {
                        ReminderScheduler.notify(context, r)
                        ReminderScheduler.schedule(context, r) // queue the next occurrence
                    }
                    ReminderScheduler.ACTION_DONE -> NotificationManagerCompat.from(context).cancel(r.id.toInt())
                    ReminderScheduler.ACTION_SNOOZE -> {
                        NotificationManagerCompat.from(context).cancel(r.id.toInt())
                        // one-off snooze; the regular schedule continues afterwards
                        ReminderScheduler.schedule(context, r, LocalDateTime.now().plusMinutes(10))
                    }
                }
            } finally { pending.finish() }
        }
    }
}

/** Alarms are wiped on reboot – put them back. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                GuruDatabase.get(context).dao().allReminders().filter { it.enabled }
                    .forEach { ReminderScheduler.schedule(context, it) }
            } finally { pending.finish() }
        }
    }
}
