package com.guru.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guru.app.data.ProfileMath
import com.guru.app.health.calendarIntent
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(vm: GuruViewModel) {
    val month by vm.month.collectAsStateWithLifecycle()
    val selected by vm.selectedDay.collectAsStateWithLifecycle()
    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val planned by vm.planned.collectAsStateWithLifecycle()
    val meals by vm.meals.collectAsStateWithLifecycle()
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val targets by vm.targets.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val mealById = meals.associateBy { it.id }
    var playing by remember { mutableStateOf<com.guru.app.data.ScheduledWorkout?>(null) }
    playing?.let { w ->
        WorkoutPlayer(w.title, w.exercises, onClose = { playing = null },
            onFinish = { playing = null; if (!w.done) vm.toggleDone(w) })
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton({ vm.month.value = month.minusMonths(1) }) { Icon(Icons.Default.ChevronLeft, "Previous") }
                Text("${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton({ vm.month.value = month.plusMonths(1) }) { Icon(Icons.Default.ChevronRight, "Next") }
            }
            Row { weekdayLetters.forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) } }

            val first = month.atDay(1)
            val gridStart = first.minusDays((first.dayOfWeek.value - 1).toLong())
            val workoutDays = workouts.groupBy { it.epochDay }
            val mealDays = planned.map { it.epochDay }.toSet()
            for (week in 0 until 6) {
                Row {
                    for (d in 0 until 7) {
                        val date = gridStart.plusDays((week * 7 + d).toLong())
                        DayCell(date, inMonth = date.month == month.month, isSelected = date == selected,
                            isToday = date == LocalDate.now(),
                            hasWorkout = workoutDays[date.toEpochDay()]?.isNotEmpty() == true,
                            allDone = workoutDays[date.toEpochDay()]?.all { it.done } == true,
                            hasMeal = date.toEpochDay() in mealDays,
                            modifier = Modifier.weight(1f)) { vm.selectedDay.value = date }
                    }
                }
            }
            Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Legend(MaterialTheme.colorScheme.primary, "Workout"); Legend(MaterialTheme.colorScheme.secondary, "Dinner")
            }
        }

        item {
            HorizontalDivider()
            Text(selected.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d")),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        val dayWorkouts = workouts.filter { it.epochDay == selected.toEpochDay() }
        val dayEats = planned.filter { it.epochDay == selected.toEpochDay() }.mapNotNull { mealById[it.mealId] }
        item {
            val kcalIn = dayEats.sumOf { it.kcal }; val proIn = dayEats.sumOf { it.protein }
            val burn = dayWorkouts.sumOf { ProfileMath.burn(it.met, it.durationMin, null, profile.weightLb) }
            GlassCard(Modifier.fillMaxWidth(), highlight = true) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row {
                        listOf("$kcalIn" to "kcal planned", "${proIn}g" to "protein", "$burn" to "kcal burn", "${dayWorkouts.sumOf { it.durationMin }}" to "active min")
                            .forEach { (v, l) -> Column(Modifier.weight(1f)) { Text(v, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge); Text(l, style = MaterialTheme.typography.labelSmall, color = Guru.TextDim) } }
                    }
                    targets?.let { t ->
                        LinearProgressIndicator(progress = { (kcalIn.toFloat() / t.kcal).coerceIn(0f, 1f) }, Modifier.fillMaxWidth(), color = Guru.Ember, trackColor = Guru.Glass)
                        Text("$kcalIn of ${t.kcal} kcal and $proIn of ${t.protein}g protein planned", style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
                    } ?: Text("Set up your profile on the Health tab for daily targets.", style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
                }
            }
        }
        item { SectionLabel("🏋️ Workouts", if (dayWorkouts.isEmpty()) "Nothing scheduled – add one from the Workouts tab" else null) }
        items(dayWorkouts, key = { "w${it.id}" }) { w ->
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(w.done, { vm.toggleDone(w) })
                    Column(Modifier.weight(1f)) {
                        Text(w.title, fontWeight = FontWeight.SemiBold)
                        Text("${fmtTime(w.startMinuteOfDay / 60, w.startMinuteOfDay % 60)} · ${w.durationMin} min · ≈${ProfileMath.burn(w.met, w.durationMin, null, profile.weightLb)} kcal" +
                            if (w.syncedToHealth) " · ✓ synced" else "", style = MaterialTheme.typography.bodySmall)
                        Text(w.exercises.lines().take(3).joinToString(" • "), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    IconButton({ playing = w }) { Icon(Icons.Default.PlayCircle, "Start guided", tint = Guru.Aurora) }
                    IconButton({ ctx.startActivity(calendarIntent(w)) }) { Icon(Icons.Default.EventAvailable, "Add to calendar app") }
                    IconButton({ vm.deleteWorkout(w) }) { Icon(Icons.Default.Delete, "Delete") }
                }
            }
        }

        val dayMeals = planned.filter { it.epochDay == selected.toEpochDay() }
        item { SectionLabel("🍽️ Dinner & Smoothies", if (dayMeals.isEmpty()) "No meals planned – try Auto-plan on the Meals tab" else null) }
        items(dayMeals, key = { "m${it.id}" }) { p ->
            val m = mealById[p.mealId] ?: return@items
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${if (p.slot == "Smoothie") "🥤" else "🍲"} ${m.name}", fontWeight = FontWeight.SemiBold)
                        Text(if (m.kcal > 0) "${m.kcal} kcal · ${m.protein}g protein" else "${m.prepType} · ${m.prepMinutes} min", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton({ vm.unplan(p) }) { Icon(Icons.Default.Close, "Remove") }
                }
            }
        }

        val dow = 1 shl (selected.dayOfWeek.value - 1)
        val dayRem = reminders.filter { it.enabled && it.daysMask and dow != 0 }
        item { SectionLabel("🔔 Alerts", if (dayRem.isEmpty()) "No alerts today" else null) }
        items(dayRem, key = { "r${it.id}" }) { r ->
            Text("${fmtTime(r.hour, r.minute)} — ${r.title} (${r.type})", Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun SectionLabel(title: String, empty: String?) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        empty?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color)); Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun DayCell(
    date: LocalDate, inMonth: Boolean, isSelected: Boolean, isToday: Boolean,
    hasWorkout: Boolean, allDone: Boolean, hasMeal: Boolean, modifier: Modifier, onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.aspectRatio(1f).padding(2.dp).clip(MaterialTheme.shapes.small)
            .background(when { isSelected -> cs.primary; isToday -> cs.primaryContainer; else -> Guru.Glass })
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Text(date.dayOfMonth.toString(),
            color = when { isSelected -> cs.onPrimary; !inMonth -> cs.outline; else -> cs.onSurface },
            style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (hasWorkout) Box(Modifier.size(6.dp).clip(CircleShape).background(if (isSelected) cs.onPrimary else if (allDone) cs.tertiary else cs.primary))
            if (hasMeal) Box(Modifier.size(6.dp).clip(CircleShape).background(cs.secondary))
        }
    }
}
