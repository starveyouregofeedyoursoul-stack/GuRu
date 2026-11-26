package com.guru.app.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Volume
import com.guru.app.data.ScheduledWorkout
import java.time.*

/**
 * Health Connect is Android's shared health data hub. Writing here makes GuRu workouts show up in
 * Samsung Health, Fitbit, Google Fit, Garmin Connect, Oura, etc., and reading here pulls in steps,
 * heart rate and exercise logged by any watch or app the user has connected.
 */
class HealthConnectManager(private val context: Context) {

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
    )

    fun status(): Int = HealthConnectClient.getSdkStatus(context)
    val isAvailable get() = status() == HealthConnectClient.SDK_AVAILABLE

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun hasAllPermissions(): Boolean =
        isAvailable && client.permissionController.getGrantedPermissions().containsAll(permissions)

    /** Opens Play Store so the user can install/update Health Connect (needed on Android 13 and below). */
    fun installIntent() = Intent(Intent.ACTION_VIEW, Uri.parse(
        "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"))
        .setPackage("com.android.vending")

    data class Summary(
        val stepsToday: Long,
        val activeKcalToday: Double,
        val avgHeartRateToday: Long?,
        val recentSessions: List<String>,
    )

    suspend fun summary(): Summary {
        val zone = ZoneId.systemDefault()
        val startOfDay = LocalDate.now().atStartOfDay(zone).toInstant()
        val now = Instant.now()
        val today = TimeRangeFilter.between(startOfDay, now)

        val agg = client.aggregate(
            AggregateRequest(
                setOf(StepsRecord.COUNT_TOTAL, ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL, HeartRateRecord.BPM_AVG),
                today,
            )
        )
        val sessions = client.readRecords(
            ReadRecordsRequest(ExerciseSessionRecord::class, TimeRangeFilter.between(now.minus(Duration.ofDays(7)), now))
        ).records.sortedByDescending { it.startTime }.take(10).map {
            val mins = Duration.between(it.startTime, it.endTime).toMinutes()
            val day = it.startTime.atZone(zone).toLocalDate()
            val src = it.metadata.dataOrigin.packageName.substringAfterLast('.')
            "${day.dayOfWeek.name.take(3)} ${day.monthValue}/${day.dayOfMonth} · ${it.title ?: "Workout"} · $mins min · via $src"
        }
        return Summary(
            stepsToday = agg[StepsRecord.COUNT_TOTAL] ?: 0,
            activeKcalToday = agg[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
            avgHeartRateToday = agg[HeartRateRecord.BPM_AVG],
            recentSessions = sessions,
        )
    }

    /**
     * Most recent sleep session (last 36 h) from any app that shares sleep with Health Connect:
     * Samsung Health, Fitbit/Pixel Watch, Oura, Garmin, and myAir if ResMed shares sleep there.
     * Returns bedtime and wake time as LocalDateTime.
     */
    suspend fun lastSleep(): Pair<LocalDateTime, LocalDateTime>? {
        val now = Instant.now()
        val recs = client.readRecords(ReadRecordsRequest(SleepSessionRecord::class, TimeRangeFilter.between(now.minus(Duration.ofHours(36)), now))).records
        val s = recs.maxByOrNull { it.endTime } ?: return null
        val z = ZoneId.systemDefault()
        return LocalDateTime.ofInstant(s.startTime, z) to LocalDateTime.ofInstant(s.endTime, z)
    }

    /** Write a completed GuRu workout as an exercise session. */
    suspend fun writeWorkout(w: ScheduledWorkout) {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.ofEpochDay(w.epochDay).atStartOfDay(zone)
            .plusMinutes(w.startMinuteOfDay.toLong()).toInstant()
        val end = start.plus(Duration.ofMinutes(w.durationMin.toLong()))
        val type = when (w.category) {
            "Strength" -> ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING
            "HIIT" -> ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING
            "Cardio" -> ExerciseSessionRecord.EXERCISE_TYPE_RUNNING
            "Yoga/Pilates" -> ExerciseSessionRecord.EXERCISE_TYPE_PILATES
            "Mobility" -> ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING
            else -> ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT
        }
        client.insertRecords(listOf(
            ExerciseSessionRecord(
                startTime = start, startZoneOffset = zone.rules.getOffset(start),
                endTime = end, endZoneOffset = zone.rules.getOffset(end),
                exerciseType = type, title = w.title, notes = w.exercises,
            )
        ))
    }

    suspend fun logWater(ml: Double) {
        val now = Instant.now()
        val off = ZoneId.systemDefault().rules.getOffset(now)
        client.insertRecords(listOf(
            HydrationRecord(startTime = now.minusSeconds(60), startZoneOffset = off, endTime = now, endZoneOffset = off,
                volume = Volume.milliliters(ml))
        ))
    }
}

/** Hand a workout to Google Calendar / Samsung Calendar / any calendar app. */
fun calendarIntent(w: ScheduledWorkout): Intent {
    val zone = ZoneId.systemDefault()
    val start = LocalDate.ofEpochDay(w.epochDay).atStartOfDay(zone).plusMinutes(w.startMinuteOfDay.toLong())
    return Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, "GuRu: ${w.title}")
        .putExtra(CalendarContract.Events.DESCRIPTION, w.exercises)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.toInstant().toEpochMilli())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start.plusMinutes(w.durationMin.toLong()).toInstant().toEpochMilli())
}
