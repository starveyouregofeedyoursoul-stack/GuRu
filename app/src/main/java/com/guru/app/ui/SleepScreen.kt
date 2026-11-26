package com.guru.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guru.app.data.ExerciseGuides
import com.guru.app.data.Poses
import com.guru.app.data.SleepCoach
import com.guru.app.data.SleepEntry
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun SleepScreen(vm: GuruViewModel) {
    val nights by vm.sleep.collectAsStateWithLifecycle()
    val wake by vm.sleepWake.collectAsStateWithLifecycle()
    val hours by vm.sleepHours.collectAsStateWithLifecycle()
    val workouts by vm.workouts.collectAsStateWithLifecycle()
    val templates by vm.templates.collectAsStateWithLifecycle()
    val today = LocalDate.now().toEpochDay()
    var logging by remember { mutableStateOf<SleepEntry?>(null) }
    var playing by remember { mutableStateOf(false) }
    var guideName by remember { mutableStateOf<String?>(null) }
    val last = nights.maxByOrNull { it.epochDay }
    val insights = remember(nights, workouts, hours) {
        SleepCoach.insights(nights, hours.toDouble()) { day -> workouts.any { it.epochDay == day - 1 && it.done } }
    }
    val bed = vm.bedtimeMin()
    val wind = templates.firstOrNull { it.name == "Bedtime Wind-Down Stretch" }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            GlassCard(Modifier.fillMaxWidth(), highlight = true) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (last != null) {
                        Text("Last logged night", color = Guru.TextDim, style = MaterialTheme.typography.labelMedium)
                        Text(SleepCoach.hm(last.minutes.toDouble()), fontSize = 52.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${fmtTime(last.bedMin / 60, last.bedMin % 60)} → ${fmtTime(last.wakeMin / 60, last.wakeMin % 60)} · quality ${"★".repeat(last.quality)}${"☆".repeat(5 - last.quality)}" +
                            (last.cpapHours?.let { " · CPAP ${it}h" } ?: "") + (last.myAirScore?.let { ", myAir $it" } ?: ""), color = Guru.TextDim)
                    } else {
                        Text("How did you sleep?", style = MaterialTheme.typography.titleLarge)
                        Text("Log each morning. After 3 nights GuRu starts spotting patterns.", color = Guru.TextDim)
                    }
                    GradientButton("Log last night", Icons.Default.Bedtime) {
                        logging = nights.firstOrNull { it.epochDay == today } ?: SleepEntry(today, bed, wake)
                    }
                }
            }
        }
        item {
            SectionHeader("Last 7 nights", "Green great, blue okay, orange rough")
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().height(150.dp).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                    val maxM = maxOf(hours * 60 + 60, (nights.maxOfOrNull { it.minutes } ?: 0).toFloat())
                    for (i in 6 downTo 0) {
                        val d = today - i; val e = nights.firstOrNull { it.epochDay == d }
                        Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            if (e != null) Text("%.1f".format(e.minutes / 60f), fontSize = 11.sp, color = Guru.TextDim)
                            val col = when { e == null -> Color.Transparent; e.quality >= 4 -> Guru.Mint; e.quality >= 3 -> Guru.Aurora; else -> Guru.Ember }
                            Box(Modifier.fillMaxWidth().fillMaxHeight(((e?.minutes ?: 0) / maxM).coerceIn(0.02f, 1f) * 0.8f)
                                .clip(RoundedCornerShape(8.dp, 8.dp, 4.dp, 4.dp)).background(col))
                            Text(LocalDate.ofEpochDay(d).dayOfWeek.name.take(1), fontSize = 12.sp, color = Guru.TextDim)
                        }
                    }
                }
            }
        }
        item { SectionHeader("What GuRu noticed", if (insights.isEmpty()) "Log at least 3 nights to see your patterns" else null) }
        items(insights) { (good, txt) ->
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp)) {
                    Icon(if (good) Icons.Default.CheckCircle else Icons.Default.AutoAwesome, null, tint = if (good) Guru.Mint else Color(0xFFFFC876))
                    Spacer(Modifier.width(10.dp)); Text(txt)
                }
            }
        }
        item {
            SectionHeader("Tonight's plan")
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row {
                        listOf("Lights out by" to bed, "Wind down at" to bed - 45, "Last caffeine" to bed - 480).forEach { (l, m) ->
                            val mm = (m % 1440 + 1440) % 1440
                            Column(Modifier.weight(1f)) { Text(l, fontSize = 12.sp, color = Guru.TextDim); Text(fmtTime(mm / 60, mm % 60), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Wake", color = Guru.TextDim)
                        TimeButton(wake / 60, wake % 60) { h, m -> vm.setSleepGoal(h * 60 + m, hours) }
                        Text("Goal", color = Guru.TextDim)
                        listOf(7f, 7.5f, 8f, 9f).forEach { h -> GlassChip(if (h % 1f == 0f) "${h.toInt()}h" else "${h}h", hours == h) { vm.setSleepGoal(wake, h) } }
                    }
                    OutlinedButton({ vm.addSleepAlerts() }) { Icon(Icons.Default.NotificationsActive, null); Spacer(Modifier.width(6.dp)); Text("Add wind-down & caffeine alerts") }
                }
            }
        }
        item {
            SectionHeader("Wind-down routine", wind?.let { "${it.durationMin} gentle minutes. Tap a stretch to see how." })
            if (wind != null) GradientButton("Start wind-down", Icons.Default.PlayArrow) { playing = true }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(SleepCoach.windDownMoves) { name ->
                    GlassCard(Modifier.width(190.dp), onClick = { guideName = name }) {
                        Column(Modifier.padding(10.dp)) {
                            ExerciseDemo(Poses.forGuide[name] ?: "stand", label = name)
                            Spacer(Modifier.height(6.dp)); Text(name, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("Before-bed checklist")
            var chk by remember { mutableStateOf(vm.checklist(today)) }
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp)) {
                    SleepCoach.habits.forEachIndexed { i, h ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(i in chk, { on -> chk = if (on) chk + i else chk - i; vm.setChecklist(today, chk) }); Text(h)
                        }
                    }
                }
            }
        }
        item {
            SectionHeader("Evening eating")
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Skip in the evening", color = Guru.Ember, fontWeight = FontWeight.SemiBold)
                    SleepCoach.avoid.forEach { Text("•  $it") }
                    Spacer(Modifier.height(8.dp))
                    Text("Better choices", color = Guru.Mint, fontWeight = FontWeight.SemiBold)
                    SleepCoach.better.forEach { Text("•  $it") }
                }
            }
        }
        item {
            SectionHeader("myAir & CPAP")
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ResMed's myAir doesn't share your CPAP therapy numbers with other apps, so add them when you log your night: " +
                        "usage hours, mask seal, events per hour, mask on/offs and myAir score. Bed and wake times can come from Health Connect " +
                        "(your watch, Samsung Health, Fitbit and others).")
                    Text("General wellness guidance, not medical advice. If you snore loudly, stop breathing at night or feel very sleepy in the daytime, talk to your doctor.",
                        style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
                }
            }
        }
    }

    logging?.let { e -> SleepLogDialog(vm, e, onDismiss = { logging = null }) }
    if (playing && wind != null) WorkoutPlayer(wind.name, wind.exercises, onClose = { playing = false }, onFinish = { playing = false; vm.message.value = "Sleep well" })
    guideName?.let { n ->
        AlertDialog(onDismissRequest = { guideName = null }, confirmButton = { TextButton({ guideName = null }) { Text("Got it") } },
            title = { Text(n) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { MoveCoaching(n) } })
    }
}

@Composable
private fun SleepLogDialog(vm: GuruViewModel, e: SleepEntry, onDismiss: () -> Unit) {
    var bed by remember { mutableIntStateOf(e.bedMin) }
    var wake by remember { mutableIntStateOf(e.wakeMin) }
    var q by remember { mutableIntStateOf(e.quality) }
    var wakeups by remember { mutableIntStateOf(e.wakeups) }
    var factors by remember { mutableStateOf(e.factors.split(",").filter { it.isNotBlank() }.toSet()) }
    var hrs by remember { mutableStateOf(e.cpapHours?.toString() ?: "") }
    var ahi by remember { mutableStateOf(e.ahi?.toString() ?: "") }
    var onoff by remember { mutableStateOf(e.maskOnOff?.toString() ?: "") }
    var score by remember { mutableStateOf(e.myAirScore?.toString() ?: "") }
    var seal by remember { mutableStateOf(e.seal) }
    var notes by remember { mutableStateOf(e.notes) }
    var source by remember { mutableStateOf(e.source) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log your sleep") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Night ending ${LocalDate.ofEpochDay(e.epochDay)}", color = Guru.TextDim)
                TextButton({ scope.launch {
                    val s = vm.sleepFromHealth()
                    if (s != null) { bed = s.first.hour * 60 + s.first.minute; wake = s.second.hour * 60 + s.second.minute; source = "health_connect"; vm.message.value = "Filled from Health Connect" }
                    else vm.message.value = "No sleep found in Health Connect. Connect it on the Health tab."
                } }) { Icon(Icons.Default.Watch, null); Spacer(Modifier.width(6.dp)); Text("Fill times from my watch / Health Connect") }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bed"); TimeButton(bed / 60, bed % 60) { h, m -> bed = h * 60 + m }
                    Text("Wake"); TimeButton(wake / 60, wake % 60) { h, m -> wake = h * 60 + m }
                }
                Text("How rested do you feel?", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { (1..5).forEach { n -> GlassChip("$n", n <= q) { q = n } } }
                NumberField("Times you woke up", wakeups, { wakeups = it })
                Text("Anything that might have affected it?", style = MaterialTheme.typography.labelMedium)
                SleepCoach.factors.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { (k, l) -> GlassChip(l, k in factors) { factors = if (k in factors) factors - k else factors + k } } }
                }
                Text("CPAP / myAir (optional)", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(hrs, { hrs = it }, label = { Text("Hours") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(ahi, { ahi = it }, label = { Text("Events/hr") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(onoff, { onoff = it }, label = { Text("Mask on/off") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(score, { score = it }, label = { Text("myAir score") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GlassChip("Seal good", seal == "good") { seal = if (seal == "good") "" else "good" }
                    GlassChip("Needed adjusting", seal == "adjust") { seal = if (seal == "adjust") "" else "adjust" }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton({
                vm.saveSleep(SleepEntry(e.epochDay, bed, wake, q, wakeups, factors.joinToString(","), hrs.toDoubleOrNull(), ahi.toDoubleOrNull(),
                    onoff.toIntOrNull(), score.toIntOrNull(), seal, notes.trim(), source))
                onDismiss()
            }) { Text("Save night") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}
