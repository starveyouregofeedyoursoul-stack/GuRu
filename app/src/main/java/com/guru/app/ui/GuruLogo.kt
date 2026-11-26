package com.guru.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.*
import kotlin.random.Random

/**
 * GuRu mark: a middle-aged man meditating in lotus pose, silhouetted in front of
 * a spiral Milky Way galaxy that slowly turns behind him.
 */
@Composable
fun GuruLogo(modifier: Modifier = Modifier, animate: Boolean = true) {
    val inf = rememberInfiniteTransition(label = "logo")
    val spin by inf.animateFloat(0f, if (animate) (2 * PI).toFloat() else 0f,
        infiniteRepeatable(tween(90_000, easing = LinearEasing)), label = "spin")
    val breathe by inf.animateFloat(if (animate) 0.85f else 1f, 1f,
        infiniteRepeatable(tween(4_000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breath")
    val stars = remember { galaxyStars() }

    Canvas(modifier) {
        val s = size.minDimension
        val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(0f, 0f, s, s)) }
        clipPath(circle) {
            drawRect(Brush.radialGradient(listOf(Color(0xFF1B1440), Color(0xFF070712)), Offset(s / 2, s * 0.42f), s * 0.7f))
            // glowing spiral arms (tilted disc, slowly rotating)
            for (arm in 0 until 2) {
                val path = Path()
                for (i in 0..100) {
                    val t = i / 100f
                    val p = galaxyPoint(arm * PI.toFloat() + t * 3.2f * PI.toFloat() + spin, 0.03f + t * 0.55f)
                    if (i == 0) path.moveTo(p.x * s, p.y * s) else path.lineTo(p.x * s, p.y * s)
                }
                listOf(Triple(0.065f, 0.10f, Color(0xFF8C7CFF)), Triple(0.037f, 0.16f, Color(0xFFB9C8FF)),
                    Triple(0.015f, 0.35f, Color(0xFFFFE2B0))).forEach { (w, a, c) ->
                    drawPath(path, c.copy(alpha = a), style = Stroke(w * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            stars.forEach { st ->
                val p = if (st.inDisc) galaxyPoint(st.angle + spin, st.r) else Offset(st.angle, st.r)
                drawCircle(st.color.copy(alpha = st.alpha), radius = st.size * s, center = Offset(p.x * s, p.y * s))
            }
            // core / halo behind the head, gently "breathing"
            val hc = Offset(s * 0.5f, s * 0.37f)
            drawCircle(Brush.radialGradient(
                listOf(Color(0xFFFFF1C9).copy(alpha = 0.95f * breathe), Color(0xFFFFB86B).copy(alpha = 0.5f), Color.Transparent),
                center = hc, radius = s * 0.28f * breathe,
            ), radius = s * 0.28f * breathe, center = hc)
            drawMeditator(s)
        }
        drawCircle(Color(0x55FFFFFF), radius = s / 2 - 0.5f, center = Offset(s / 2, s / 2), style = Stroke(1.2f))
    }
}

private const val TILT = -0.35f
private fun galaxyPoint(a: Float, r: Float): Offset {
    val x = cos(a) * r; val y = sin(a) * r * 0.45f
    return Offset(0.5f + x * cos(TILT) - y * sin(TILT), 0.40f + x * sin(TILT) + y * cos(TILT))
}

/** Same geometry as the launcher icon (res/drawable/ic_guru_foreground.xml). */
private fun DrawScope.drawMeditator(s: Float) {
    val ink = Color(0xFF05050C)
    fun o(x: Float, y: Float) = Offset(x * s, y * s)
    fun Path.m(x: Float, y: Float) = moveTo(x * s, y * s)
    fun Path.l(x: Float, y: Float) = lineTo(x * s, y * s)
    fun Path.c(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) = cubicTo(a * s, b * s, c * s, d * s, e * s, f * s)

    drawCircle(ink, 0.056f * s, o(0.5f, 0.335f))                                     // head
    drawOval(ink, topLeft = o(0.462f, 0.345f), size = Size(0.076f * s, 0.072f * s))  // short beard – the midlife touch
    drawPath(Path().apply {                                                           // shoulders, relaxed belly
        m(.47f, .39f); l(.53f, .39f); l(.535f, .42f)
        c(.58f, .425f, .615f, .435f, .625f, .47f)
        c(.625f, .52f, .60f, .55f, .605f, .58f)
        c(.625f, .62f, .62f, .66f, .60f, .69f)
        l(.40f, .69f); c(.38f, .66f, .375f, .62f, .395f, .58f)
        c(.40f, .55f, .375f, .52f, .375f, .47f)
        c(.385f, .435f, .42f, .425f, .465f, .42f); close()
    }, ink)
    drawPath(Path().apply {                                                           // crossed legs
        m(.19f, .745f); c(.19f, .69f, .27f, .655f, .36f, .665f)
        c(.43f, .675f, .57f, .675f, .64f, .665f)
        c(.73f, .655f, .81f, .69f, .81f, .745f)
        c(.70f, .775f, .30f, .775f, .19f, .745f); close()
    }, ink)
    drawOval(ink, topLeft = o(.665f, .655f), size = Size(.07f * s, .035f * s))       // hands on knees
    drawOval(ink, topLeft = o(.265f, .655f), size = Size(.07f * s, .035f * s))
    val arm = Stroke(width = 0.05f * s, cap = StrokeCap.Round)
    drawPath(Path().apply { m(.605f, .45f); c(.65f, .50f, .665f, .58f, .695f, .665f) }, ink, style = arm)
    drawPath(Path().apply { m(.395f, .45f); c(.35f, .50f, .335f, .58f, .305f, .665f) }, ink, style = arm)
}

private data class GStar(val angle: Float, val r: Float, val size: Float, val alpha: Float, val color: Color, val inDisc: Boolean)

private fun galaxyStars(): List<GStar> {
    val rnd = Random(7)
    val palette = listOf(Color(0xFFFFE2B0), Color(0xFFB9C8FF), Color(0xFFD4B5FF), Color.White)
    return buildList {
        for (arm in 0 until 2) for (i in 0 until 110) {
            val t = i / 110f
            add(GStar(arm * PI.toFloat() + t * 3.2f * PI.toFloat() + (rnd.nextFloat() - 0.5f) * 0.5f,
                0.03f + t * 0.55f + (rnd.nextFloat() - 0.5f) * 0.05f,
                0.003f + rnd.nextFloat() * 0.008f * (1 - t * 0.6f), (1 - t * 0.6f) * (0.55f + rnd.nextFloat() * 0.45f),
                palette[rnd.nextInt(palette.size)], true))
        }
        // background stars (x, y stored in angle, r)
        repeat(40) { add(GStar(rnd.nextFloat(), rnd.nextFloat(), 0.003f, 0.25f + rnd.nextFloat() * 0.4f, Color.White, false)) }
    }
}

/** Soft twinkling background stars for the whole app. */
@Composable
fun StarField(modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition(label = "twinkle").animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(8_000, easing = LinearEasing)), label = "t",
    )
    val pts = remember { List(90) { Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 6.28f) } }
    Canvas(modifier) {
        pts.forEach { (x, y, phase) ->
            val a = 0.15f + 0.35f * (0.5f + 0.5f * sin(t + phase))
            drawCircle(Color.White.copy(alpha = a), radius = 1.2f + phase / 6f, center = Offset(x * size.width, y * size.height))
        }
    }
}
