package com.guru.app.ui

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guru.app.data.Categories
import com.guru.app.data.Reminder

private fun emoji(type: String) = when (type) {
    "Meds" -> "💊"; "Workout" -> "🏋️"; "Smoothie" -> "🥤"; "Meal" -> "🍽️"
    "Water" -> "💧"; "Supplements" -> "🌿"; else -> "🔔"
}

/** One-tap starting points so setting up common alerts takes seconds. */
private val quickPresets = listOf(
    Reminder(title = "Morning meds", type = "Meds", hour = 8, minute = 0),
    Reminder(title = "Evening meds", type = "Meds", hour = 21, minute = 0),
    Reminder(title = "Vitamins", type = "Supplements", hour = 9, minute = 0),
    Reminder(title = "Pre-workout shake", type = "Smoothie", hour = 6, minute = 30, daysMask = 0b0011111),
    Reminder(title = "Post-workout smoothie", type = "Smoothie", hour = 18, minute = 0),
    Reminder(title = "Prep dinner", type = "Meal", hour = 17, minute = 30),
    Reminder(title = "Drink water", type = "Water", hour = 11, minute = 0),
    Reminder(title = "Workout time", type = "Workout", hour = 7, minute = 0, daysMask = 0b0010101),
)

@Composable
fun RemindersScreen(vm: GuruViewModel) {
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Reminder?>(null) }
    val ctx = LocalContext.current
    val am = ctx.getSystemService(AlarmManager::class.java)
    val needsExact = Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 96.dp)) {
            if (needsExact) item {
                GlassCard(Modifier.fillMaxWidth(), highlight = true, onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")))
                }) {
                    Text("⏰  Allow exact alarms so med reminders fire on the minute. Tap to enable.", Modifier.padding(14.dp))
                }
            }
            item {
                SectionHeader("Quick add", "Tap one, then adjust the time")
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickPresets) { p -> GlassChip("${emoji(p.type)} ${p.title}", false) { editing = p } }
                }
                SectionHeader("Your alerts", if (reminders.isEmpty()) "No alerts yet" else "Also shows on paired watches")
            }
            items(reminders, key = { it.id }) { r ->
                GlassCard(Modifier.fillMaxWidth(), onClick = { editing = r }, highlight = r.enabled) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(CircleShape).background(Guru.Glass), contentAlignment = Alignment.Center) {
                            Text(emoji(r.type), fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(fmtTime(r.hour, r.minute), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(r.title, fontWeight = FontWeight.SemiBold)
                            Text(daysLabel(r.daysMask) + if (r.note.isNotBlank()) " · ${r.note}" else "",
                                style = MaterialTheme.typography.bodySmall, color = Guru.TextDim, maxLines = 1)
                        }
                        Switch(r.enabled, { vm.saveReminder(r.copy(enabled = it)) })
                    }
                }
            }
        }
        GradientButton("New alert", Icons.Default.Add, Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            editing = Reminder(title = "", type = "Meds", hour = 8, minute = 0)
        }
    }

    editing?.let { r -> ReminderEditor(r, { editing = null }, { vm.saveReminder(it); editing = null }, { vm.deleteReminder(r); editing = null }) }
}

fun daysLabel(mask: Int) = when (mask) {
    0b1111111 -> "Every day"; 0b0011111 -> "Weekdays"; 0b1100000 -> "Weekends"
    else -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").filterIndexed { i, _ -> mask and (1 shl i) != 0 }.joinToString(", ")
}

@Composable
private fun ReminderEditor(r: Reminder, onDismiss: () -> Unit, onSave: (Reminder) -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(r.title) }
    var type by remember { mutableStateOf(r.type) }
    var hour by remember { mutableIntStateOf(r.hour) }
    var minute by remember { mutableIntStateOf(r.minute) }
    var mask by remember { mutableIntStateOf(r.daysMask) }
    var note by remember { mutableStateOf(r.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (r.id == 0L) "New alert" else "Edit alert") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("What to remind you") }, singleLine = true)
                ChipRow(Categories.reminderTypes, type, { type = it ?: type })
                TimeButton(hour, minute) { h, m -> hour = h; minute = m }
                DaysPicker(mask) { mask = it }
                OutlinedTextField(note, { note = it }, label = { Text("Note (dose, recipe…)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank() && mask != 0, onClick = {
                onSave(r.copy(title = title.trim(), type = type, hour = hour, minute = minute, daysMask = mask, note = note.trim(), enabled = true))
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (r.id != 0L) TextButton(onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                TextButton(onDismiss) { Text("Cancel") }
            }
        },
    )
}
