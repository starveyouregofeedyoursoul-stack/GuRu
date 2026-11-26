package com.guru.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.PI
import kotlin.math.sin

/** Animated bottle that fills to [pct] (0..1+) with a moving wave; glows when full. */
@Composable
fun WaterBottle(pct: Float, modifier: Modifier = Modifier) {
    val level by animateFloatAsState(pct.coerceIn(0f, 1f), spring(dampingRatio = .7f, stiffness = Spring.StiffnessVeryLow), label = "lvl")
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "ph")
    Canvas(modifier.size(120.dp, 240.dp).semantics { contentDescription = "Water bottle ${(pct * 100).toInt()}% full" }) {
        val s = size.width / 120f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)
        val bottle = Path().apply {
            moveTo(44 * s, 30 * s); lineTo(76 * s, 30 * s); lineTo(76 * s, 44 * s)
            cubicTo(76 * s, 50 * s, 98 * s, 58 * s, 98 * s, 78 * s); lineTo(98 * s, 216 * s)
            quadraticTo(98 * s, 228 * s, 86 * s, 228 * s); lineTo(34 * s, 228 * s)
            quadraticTo(22 * s, 228 * s, 22 * s, 216 * s); lineTo(22 * s, 78 * s)
            cubicTo(22 * s, 58 * s, 44 * s, 50 * s, 44 * s, 44 * s); close()
        }
        drawRoundRect(Color(0xFF5CE1C6), p(40f, 10f), Size(40 * s, 20 * s), CornerRadius(6 * s))
        clipPath(bottle) {
            drawRect(Color(0x0FFFFFFF))
            val top = 62f; val bot = 228f; val y = bot - (bot - top) * level
            fun wave(amp: Float, shift: Float, col: Brush) {
                val w = Path().apply {
                    moveTo(0f, size.height); lineTo(0f, y * s)
                    var x = 0f; while (x <= 120f) { lineTo(x * s, (y + sin(x / 120f * 4 * PI.toFloat() + phase + shift) * amp) * s); x += 4f }
                    lineTo(size.width, size.height); close()
                }
                drawPath(w, col)
            }
            if (level > 0f) {
                wave(4f, 1.6f, SolidColor(Color(0x8C5CE1C6)))
                wave(4f, 0f, Brush.verticalGradient(listOf(Color(0xFF7CD3FF), Color(0xFF3A6BFF)), startY = y * s, endY = bot * s))
            }
            for (f in listOf(.25f, .5f, .75f)) drawLine(Color(0x59FFFFFF), p(24f, bot - (bot - top) * f), p(34f, bot - (bot - top) * f), 1.5f * s)
        }
        if (pct >= 1f) drawPath(bottle, Color(0x665CE1C6), style = Stroke(10 * s))
        drawPath(bottle, Color(0x8CFFFFFF), style = Stroke(2.5f * s))
        drawLine(Color(0x40FFFFFF), p(30f, 90f), p(30f, 200f), 4 * s, StrokeCap.Round)
    }
}

/** Water card: bottle + % of today's personal goal + quick adds. */
@Composable
fun WaterCard(vm: GuruViewModel) {
    val had by vm.waterToday.collectAsStateWithLifecycle()
    val oz by vm.waterOz.collectAsStateWithLifecycle()
    val t by vm.targets.collectAsStateWithLifecycle()
    vm.profile.collectAsStateWithLifecycle(); vm.workouts.collectAsStateWithLifecycle()
    val goal = vm.waterGoalMl(); val pct = had.toFloat() / goal
    var custom by remember { mutableStateOf("") }
    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            WaterBottle(pct, Modifier.width(110.dp).height(220.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${(pct * 100).toInt()}%", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = if (pct >= 1f) Guru.Mint else Color.White)
                Text("${vm.fmtWater(had)} of ${vm.fmtWater(goal)}", fontWeight = FontWeight.SemiBold)
                Text(if (pct >= 1f) "Goal reached. Nice work!" else "${vm.fmtWater(goal - had)} to go", color = Guru.TextDim)
                val cups = if (oz) listOf(237 to "+8 oz", 500 to "+16.9 oz", 710 to "+24 oz") else listOf(250 to "+250 ml", 500 to "+500 ml", 750 to "+750 ml")
                cups.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { (ml, l) -> GlassChip(l, false) { vm.addWater(ml) } } } }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(custom, { custom = it.filter(Char::isDigit).take(4) }, label = { Text(if (oz) "oz" else "ml") }, singleLine = true, modifier = Modifier.width(90.dp))
                    TextButton({ custom.toIntOrNull()?.let { vm.addWater(if (oz) (it * 29.5735).toInt() else it) }; custom = "" }) { Text("Add") }
                    TextButton({ vm.undoWater() }) { Text("Undo") }
                }
            }
        }
        Text("Goal: " + (t?.let { "about 1 ml per calorie you burn (${it.tdee} kcal/day from your height, weight, age, sex and activity)" }
            ?: "about 0.5 oz per lb of body weight. Complete your profile for a more personal goal") +
            ", plus extra for today's workouts. Drink more in heat. If a doctor has limited your fluids, follow their advice.",
            style = MaterialTheme.typography.bodySmall, color = Guru.TextDim, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
        TextButton({ vm.toggleWaterUnit() }, Modifier.padding(start = 8.dp)) { Text("Show in ${if (oz) "ml" else "oz"}") }
    }
}
