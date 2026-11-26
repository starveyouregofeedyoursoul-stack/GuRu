package com.guru.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guru.app.data.*
import java.time.DayOfWeek
import kotlin.math.abs

fun categoryArt(cat: String): Pair<Brush, ImageVector> = when (cat) {
    "Strength" -> Brush.linearGradient(listOf(Color(0xFF3A47D5), Color(0xFF00D2FF))) to Icons.Default.FitnessCenter
    "HIIT" -> Brush.linearGradient(listOf(Color(0xFFFF512F), Color(0xFFDD2476))) to Icons.Default.LocalFireDepartment
    "Cardio" -> Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))) to Icons.Default.DirectionsRun
    "Mobility" -> Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))) to Icons.Default.SelfImprovement
    "Yoga/Pilates" -> Brush.linearGradient(listOf(Color(0xFFEE9CA7), Color(0xFF7F7FD5))) to Icons.Default.SelfImprovement
    "Trend" -> Brush.linearGradient(listOf(Color(0xFFF7971E), Color(0xFFFFD200))) to Icons.Default.TrendingUp
    "Sport" -> Brush.linearGradient(listOf(Color(0xFF1D976C), Color(0xFF93F9B9))) to Icons.Default.SportsTennis
    else -> Brush.linearGradient(listOf(Color(0xFF7C9CFF), Color(0xFFB47CFF))) to Icons.Default.Star
}

@Composable
fun WorkoutsScreen(vm: GuruViewModel) {
    val templates by vm.templates.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()
    val targets by vm.targets.collectAsStateWithLifecycle()
    val kcal: (WorkoutTemplate) -> Int = { ProfileMath.burn(it.met, it.durationMin, it.intensity, profile.weightLb) }
    val recommended = remember(templates, profile, targets) { ProfileMath.recommend(profile, targets, templates) }
    var open by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var editing by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var scheduling by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var playing by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var guide by remember { mutableStateOf<ExerciseGuide?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
            if (recommended.isNotEmpty()) item(key = "rec") {
                Box(Modifier.padding(horizontal = 16.dp)) { SectionHeader("Recommended for you", "Picked for your goal and profile") }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recommended, key = { "r${it.id}" }) { t -> PosterCard(t, kcal(t)) { open = t } }
                }
            }
            item {
                Box(Modifier.padding(horizontal = 16.dp)) { SectionHeader("Featured", "Swipe, then tap to open") }
                HeroCarousel(templates.filter { it.category == "Trend" || it.intensity == "Hard" }.take(6), kcal) { open = it }
            }
            templates.groupBy { it.category }.toSortedMap().forEach { (cat, list) ->
                item(key = "shelf-$cat") {
                    Box(Modifier.padding(horizontal = 16.dp)) { SectionHeader(cat) }
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(list, key = { it.id }) { t -> PosterCard(t, kcal(t)) { open = t } }
                    }
                }
            }
            item(key = "library") {
                Box(Modifier.padding(horizontal = 16.dp)) { SectionHeader("Exercise how-to library", "${ExerciseGuides.all.size} moves with step-by-step form") }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ExerciseGuides.all) { g ->
                        GlassCard(Modifier.width(150.dp), onClick = { guide = g }) {
                            Column(Modifier.padding(12.dp)) {
                                Icon(Icons.Default.School, null, tint = Guru.Aurora)
                                Text(g.name, fontWeight = FontWeight.SemiBold, maxLines = 2)
                                Text(g.muscles, style = MaterialTheme.typography.bodySmall, color = Guru.TextDim, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        GradientButton("Custom workout", Icons.Default.Add, Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            editing = WorkoutTemplate(name = "", category = "Custom", durationMin = 30, intensity = "Moderate", exercises = "")
        }
    }

    open?.let { t ->
        WorkoutDetail(t, kcal(t), profile.weightLb, onClose = { open = null },
            onStart = { playing = t }, onSchedule = { scheduling = t }, onEdit = { editing = t; open = null })
    }
    playing?.let { t ->
        WorkoutPlayer(t.name, t.exercises, onClose = { playing = null },
            onFinish = { playing = null; vm.message.value = "🔥 ${t.name} complete!" })
    }
    guide?.let { g ->
        AlertDialog(onDismissRequest = { guide = null }, confirmButton = { TextButton({ guide = null }) { Text("Got it") } },
            title = { Text(g.name) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { MoveCoaching(g.name) } })
    }
    editing?.let { t ->
        TemplateEditor(t, onDismiss = { editing = null },
            onSave = { vm.saveTemplate(it); editing = null },
            onDelete = { vm.deleteTemplate(t); editing = null })
    }
    scheduling?.let { t -> ScheduleDialog(vm, t) { scheduling = null } }
}

/** Apple-TV style carousel: the centred card is large and bright, neighbours shrink and dim. */
@Composable
private fun HeroCarousel(items: List<WorkoutTemplate>, kcal: (WorkoutTemplate) -> Int, onOpen: (WorkoutTemplate) -> Unit) {
    val state = rememberLazyListState()
    LazyRow(
        state = state, flingBehavior = rememberSnapFlingBehavior(state),
        contentPadding = PaddingValues(horizontal = 40.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items, key = { _, t -> "hero${t.id}" }) { i, t ->
            val focus by remember {
                derivedStateOf {
                    val info = state.layoutInfo
                    val item = info.visibleItemsInfo.firstOrNull { it.index == i } ?: return@derivedStateOf 0f
                    val center = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                    val itemCenter = item.offset + item.size / 2f
                    (1f - abs(center - itemCenter) / (info.viewportEndOffset - info.viewportStartOffset)).coerceIn(0f, 1f)
                }
            }
            val (bg, icon) = categoryArt(t.category)
            Box(
                Modifier.width(280.dp).height(170.dp)
                    .graphicsLayer {
                        val s = 0.86f + 0.14f * focus; scaleX = s; scaleY = s; alpha = 0.5f + 0.5f * focus
                    }
                    .clip(MaterialTheme.shapes.large).background(bg).clickable { onOpen(t) }.padding(18.dp),
            ) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(110.dp).align(Alignment.TopEnd))
                Column(Modifier.align(Alignment.BottomStart)) {
                    Text(t.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    Text(t.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${t.durationMin} min · ${t.intensity} · ≈${kcal(t)} kcal", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

@Composable
private fun PosterCard(t: WorkoutTemplate, kcal: Int, onClick: () -> Unit) {
    val (bg, icon) = categoryArt(t.category)
    GlassCard(Modifier.width(160.dp), onClick = onClick) {
        Box(Modifier.fillMaxWidth().height(90.dp).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Column(Modifier.padding(10.dp)) {
            Text(t.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("${t.durationMin} min · ${t.intensity} · ≈$kcal kcal${if (!t.isPreset) " · Mine" else ""}",
                style = MaterialTheme.typography.bodySmall, color = Guru.TextDim)
        }
    }
}

/** Full-screen detail: each move expands to show how to do it. */
@Composable
private fun WorkoutDetail(t: WorkoutTemplate, kcal: Int, weightLb: Double, onClose: () -> Unit, onStart: () -> Unit, onSchedule: () -> Unit, onEdit: () -> Unit) {
    val (bg, icon) = categoryArt(t.category)
    var expanded by remember { mutableStateOf<Int?>(0) }
    Dialog(onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Guru.background)) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(240.dp).background(bg)) {
                        Icon(icon, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(200.dp).align(Alignment.CenterEnd))
                        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Guru.Night))))
                        IconButton(onClose, Modifier.statusBarsPadding().padding(8.dp)) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            Text(t.category.uppercase(), style = MaterialTheme.typography.labelMedium, color = Guru.TextDim)
                            Text(t.name, style = MaterialTheme.typography.displaySmall)
                            Text("${t.durationMin} min · ${t.intensity} · ≈ $kcal kcal", color = Guru.TextDim)
                        }
                    }
                    Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        GradientButton("Start guided", Icons.Default.PlayArrow, onClick = onStart)
                        FilledTonalIconButton(onSchedule) { Icon(Icons.Default.CalendarMonth, "Schedule") }
                        FilledTonalIconButton(onEdit) { Icon(Icons.Default.Edit, "Edit") }
                    }
                    Text("Calorie burn is estimated for ${weightLb.toInt()} lb (set your profile on the Health tab).",
                        color = Guru.TextDim, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                    Text("Moves – tap for how-to", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 20.dp))
                }
                itemsIndexed(t.exercises.lines().filter { it.isNotBlank() }) { i, line ->
                    val g = ExerciseGuides.find(line)
                    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                        onClick = { expanded = if (expanded == i) null else i }, highlight = expanded == i) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(28.dp).clip(CircleShape).background(Guru.Glass), contentAlignment = Alignment.Center) { Text("${i + 1}") }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(line, fontWeight = FontWeight.SemiBold)
                                g?.let { Text(it.muscles, style = MaterialTheme.typography.bodySmall, color = Guru.TextDim) }
                            }
                            Icon(if (expanded == i) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                        }
                        AnimatedVisibility(expanded == i, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                            Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) { MoveCoaching(line) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateEditor(t: WorkoutTemplate, onDismiss: () -> Unit, onSave: (WorkoutTemplate) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(t.name) }
    var cat by remember { mutableStateOf(t.category) }
    var mins by remember { mutableIntStateOf(t.durationMin) }
    var intensity by remember { mutableStateOf(t.intensity) }
    var ex by remember { mutableStateOf(t.exercises) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (t.id == 0L) "New workout" else "Edit workout") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                Text("Type", style = MaterialTheme.typography.labelMedium)
                ChipRow(Categories.workout, cat, { cat = it ?: cat })
                Text("Intensity", style = MaterialTheme.typography.labelMedium)
                ChipRow(Categories.intensity, intensity, { intensity = it ?: intensity })
                NumberField("Minutes", mins, { mins = it })
                OutlinedTextField(ex, { ex = it }, label = { Text("Exercises (one per line)") }, minLines = 4,
                    supportingText = { Text("Tip: include a time like \"Plank 45s\" and the guided timer uses it") })
                Text("Quick add a move:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ExerciseGuides.all) { g -> GlassChip(g.name, false) { ex = (ex.trimEnd() + "\n" + g.name + " 3x10").trim() } }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave(t.copy(name = name.trim(), category = cat, durationMin = mins, intensity = intensity, exercises = ex.trim(),
                    met = if (t.id == 0L) mapOf("Easy" to 3.5, "Moderate" to 5.0, "Hard" to 7.0)[intensity] ?: 5.0 else t.met))
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (t.id != 0L) TextButton(onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                TextButton(onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun ScheduleDialog(vm: GuruViewModel, t: WorkoutTemplate, onDismiss: () -> Unit) {
    val start by vm.selectedDay.collectAsStateWithLifecycle()
    var mask by remember { mutableIntStateOf(1 shl (start.dayOfWeek.value - 1)) }
    var weeks by remember { mutableIntStateOf(4) }
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(0) }
    var addAlert by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule ${t.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Starting ${start.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${start.dayOfMonth} (change the day on the Plan tab)",
                    style = MaterialTheme.typography.bodySmall)
                Text("Repeat on", style = MaterialTheme.typography.labelMedium)
                DaysPicker(mask) { mask = it }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Weeks", weeks, { weeks = it.coerceIn(1, 52) }, Modifier.width(100.dp))
                    TimeButton(hour, minute) { h, m -> hour = h; minute = m }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(addAlert, { addAlert = it }); Text("Also remind me 15 min before")
                }
            }
        },
        confirmButton = {
            TextButton(enabled = mask != 0, onClick = {
                val days = DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()
                vm.applyTemplate(t, start, weeks, days, hour * 60 + minute)
                if (addAlert) {
                    val total = hour * 60 + minute - 15
                    val alertMin = if (total < 0) total + 1440 else total
                    vm.saveReminder(Reminder(title = "${t.name} in 15 min", type = "Workout",
                        hour = alertMin / 60, minute = alertMin % 60, daysMask = mask, note = t.exercises.lines().firstOrNull() ?: ""))
                }
                onDismiss()
            }) { Text("Add to calendar") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}
