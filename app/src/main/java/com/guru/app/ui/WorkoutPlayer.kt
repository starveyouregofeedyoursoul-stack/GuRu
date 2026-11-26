package com.guru.app.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.guru.app.data.ExerciseGuide
import com.guru.app.data.ExerciseGuides
import kotlinx.coroutines.delay

/** Full-screen guided session: one move at a time with how-to cues, a work/rest timer and a demo link. */
@Composable
fun WorkoutPlayer(title: String, exercises: String, onClose: () -> Unit, onFinish: () -> Unit) {
    val moves = remember(exercises) {
        exercises.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.endsWith(":") && !it.lowercase().startsWith("rest") }
    }
    if (moves.isEmpty()) { onClose(); return }
    var index by remember { mutableIntStateOf(0) }
    var resting by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(true) }
    var workSec by remember { mutableIntStateOf(45) }
    val restSec = 15
    val line = moves[index]
    val total = if (resting) restSec else ExerciseGuides.secondsIn(line) ?: workSec
    var left by remember(index, resting) { mutableIntStateOf(total) }
    val haptic = LocalHapticFeedback.current
    val ctx = LocalContext.current

    // keep the screen awake while training
    DisposableEffect(Unit) {
        val w = (ctx as? Activity)?.window
        w?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { w?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    LaunchedEffect(index, resting, paused) {
        while (!paused && left > 0) { delay(1000); left-- }
        if (!paused && left == 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            when {
                !resting && index < moves.lastIndex -> resting = true
                resting -> { resting = false; index++ }
                else -> { paused = true; onFinish() }
            }
        }
    }

    Dialog(onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Guru.background)) {
            StarField(Modifier.matchParentSize())
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Text("Move ${index + 1} of ${moves.size}", color = Guru.TextDim)
                    }
                    IconButton(onClose) { Icon(Icons.Default.Close, "Close") }
                }
                // progress segments
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    moves.indices.forEach { i ->
                        val c by animateColorAsState(if (i <= index) Guru.Aurora else Guru.Glass, label = "seg")
                        Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(c))
                    }
                }

                TimerRing(left, total, resting, Modifier.size(220.dp))
                Spacer(Modifier.height(16.dp))

                AnimatedContent(index to resting, label = "move", transitionSpec = {
                    (slideInHorizontally(spring(0.8f)) { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                }) { (i, rest) ->
                    if (rest) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Breathe. Next up:", color = Guru.TextDim)
                            Text(moves[i + 1], style = MaterialTheme.typography.headlineMedium)
                        }
                    } else {
                        MoveCoaching(moves[i])
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton({ if (index > 0) { index--; resting = false } }, Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Previous")
                    }
                    GradientButton(if (paused) "Start" else "Pause",
                        if (paused) Icons.Default.PlayArrow else Icons.Default.Pause) { paused = !paused }
                    FilledTonalIconButton({
                        if (index < moves.lastIndex) { index++; resting = false } else onFinish()
                    }, Modifier.size(56.dp)) { Icon(Icons.Default.SkipNext, "Next") }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Work time", color = Guru.TextDim)
                    listOf(30, 45, 60).forEach { s -> GlassChip("${s}s", workSec == s) { workSec = s; if (!resting) left = s } }
                }
            }
        }
    }
}

@Composable
private fun TimerRing(left: Int, total: Int, resting: Boolean, modifier: Modifier) {
    val progress by animateFloatAsState(left / total.toFloat().coerceAtLeast(1f), tween(900, easing = LinearEasing), label = "ring")
    val color = if (resting) Guru.Mint else Guru.Aurora
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = 14.dp.toPx()
            drawArc(Color(0x22FFFFFF), 0f, 360f, false, style = Stroke(w), topLeft = Offset(w / 2, w / 2),
                size = Size(size.width - w, size.height - w))
            drawArc(color, -90f, 360f * progress, false, style = Stroke(w, cap = StrokeCap.Round),
                topLeft = Offset(w / 2, w / 2), size = Size(size.width - w, size.height - w))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%d:%02d".format(left / 60, left % 60), fontSize = 52.sp, fontWeight = FontWeight.Bold)
            Text(if (resting) "REST" else "WORK", color = color, letterSpacing = 3.sp)
        }
    }
}

/** The "show me how" panel for a single exercise line. */
@Composable
fun MoveCoaching(line: String) {
    val guide = ExerciseGuides.find(line)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(line, style = MaterialTheme.typography.headlineMedium)
        guide?.let { Text(it.muscles, color = Guru.TextDim) }
        Spacer(Modifier.height(12.dp))
        if (guide != null) {
            ExerciseDemo(com.guru.app.data.Poses.forGuide[guide.name] ?: "stand", label = "Animated demonstration of ${guide.name}")
            Spacer(Modifier.height(10.dp))
            GuideCard(guide)
        } else Text("No built-in guide for this move yet.", color = Guru.TextDim)
    }
}

@Composable
fun GuideCard(g: ExerciseGuide) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("How to do it", style = MaterialTheme.typography.titleMedium)
            g.steps.forEachIndexed { i, s ->
                Row {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(Guru.accent), contentAlignment = Alignment.Center) {
                        Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                    Spacer(Modifier.width(10.dp)); Text(s)
                }
            }
            HorizontalDivider(color = Guru.GlassBorder)
            Row { Text("💡 ", fontSize = 16.sp); Text(g.cue, color = Guru.Mint) }
            Row { Text("⚠️ ", fontSize = 16.sp); Text("Avoid: ${g.avoid}", color = Guru.Ember) }
        }
    }
}
