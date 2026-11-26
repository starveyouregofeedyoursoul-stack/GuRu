package com.guru.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.guru.app.data.Poses
import kotlin.math.*

/**
 * GuRu's in-house exercise animation: a glowing figure looping the move from keyframe poses.
 * Same engine and pose data as the web version (tools/poses.py). No video, no network.
 */
@Composable
fun ExerciseDemo(pattern: String, modifier: Modifier = Modifier, label: String = "Animated demonstration") {
    val p = Poses.all[pattern] ?: Poses.all.getValue("stand")
    val bounds = remember(pattern) { bounds(p) }
    val time by rememberInfiniteTransition(label = "demo").animateFloat(
        0f, p.frames.size.toFloat(), infiniteRepeatable(tween(p.ms * p.frames.size, easing = LinearEasing)), label = "t",
    )
    Canvas(
        modifier.fillMaxWidth().aspectRatio(16f / 10f).clip(RoundedCornerShape(16.dp))
            .background(Brush.radialGradient(listOf(Color(0xFF1F2660), Color(0xFF0B0E29))))
            .semantics { contentDescription = label },
    ) { drawFigure(p, bounds, time) }
}


private const val TORSO = .9f; private const val NECK = .1f; private const val HEAD = .22f
private const val THIGH = .95f; private const val SHIN = .95f; private const val UPPER = .65f; private const val FORE = .6f

private class J(val hip: Offset, val sh: Offset, val hc: Offset, val lk: Offset, val lft: Offset, val rk: Offset, val rft: Offset,
                val le: Offset, val lh: Offset, val re: Offset, val rh: Offset, val up: Float) {
    val pts get() = listOf(hip, sh, lk, lft, rk, rft, le, lh, re, rh, hc + Offset(0f, HEAD), hc - Offset(0f, HEAD), hc + Offset(HEAD, 0f), hc - Offset(HEAD, 0f))
}
private fun rad(a: Float) = a * PI.toFloat() / 180f
private fun limb(p: Offset, a: Float, len: Float) = Offset(p.x + sin(rad(a)) * len, p.y + cos(rad(a)) * len)

private fun joints(f: FloatArray): J {
    val t = f[0]; val hd = f[1]
    val hip = Offset.Zero; val sh = Offset(sin(rad(t)) * TORSO, -cos(rad(t)) * TORSO)
    val hc = Offset(sh.x + sin(rad(t + hd)) * (NECK + HEAD), sh.y - cos(rad(t + hd)) * (NECK + HEAD))
    val lk = limb(hip, f[2], THIGH); val rk = limb(hip, f[4], THIGH)
    val le = limb(sh, f[6], UPPER); val re = limb(sh, f[8], UPPER)
    return J(hip, sh, hc, lk, limb(lk, f[3], SHIN), rk, limb(rk, f[5], SHIN), le, limb(le, f[7], FORE), re, limb(re, f[9], FORE), f[10])
}

private fun frameAt(p: Poses.Pattern, pos: Float): FloatArray {
    val n = p.frames.size; val i = floor(pos).toInt().mod(n); val x = pos - floor(pos)
    val k = (1 - cos(PI.toFloat() * x)) / 2
    val a = p.frames[i]; val b = p.frames[(i + 1) % n]
    return FloatArray(a.size) { idx ->
        if (idx == 10) a[idx] + (b[idx] - a[idx]) * k
        else { val d = ((b[idx] - a[idx] + 540f) % 360f) - 180f; a[idx] + d * k }
    }
}

private fun groundShift(p: Poses.Pattern, j: J): Float = when {
    p.floating && p.prop == "bike" -> -2.25f
    p.floating -> 0f
    else -> -j.pts.maxOf { it.y } - j.up
}

private class B(val x0: Float, val x1: Float, val y0: Float, val y1: Float)
private fun bounds(p: Poses.Pattern): B {
    var x0 = 1e9f; var x1 = -1e9f; var y0 = 1e9f; var y1 = -1e9f
    for (i in p.frames.indices) for (s in 0..4) {
        val j = joints(frameAt(p, i + s / 4f)); val g = groundShift(p, j)
        for (q in j.pts) { x0 = min(x0, q.x); x1 = max(x1, q.x); y0 = min(y0, q.y + g); y1 = max(y1, q.y + g) }
    }
    if (!p.floating || p.prop == "bike") y1 = max(y1, 0f)
    when (p.prop) {
        "bike" -> { x0 = min(x0, -1.7f); x1 = max(x1, 2f) }
        "rail" -> x1 = max(x1, 2.8f)
        "sled" -> x1 += 1f
        "tread" -> { x0 = min(x0, -1.6f); x1 = max(x1, 2f); y0 = min(y0, -2.6f) }
    }
    return B(x0, x1, y0, y1)
}

private fun DrawScope.drawFigure(p: Poses.Pattern, b: B, time: Float) {
    val W = size.width; val H = size.height
    val u = minOf(W * .76f / (b.x1 - b.x0), H * .78f / (b.y1 - b.y0), H * .8f / 3.4f)
    val ox = W / 2 - (b.x0 + b.x1) / 2 * u; val oy = H * .5f - (b.y0 + b.y1) / 2 * u
    val j = joints(frameAt(p, time)); val g = groundShift(p, j)
    fun T(q: Offset) = Offset(ox + q.x * u, oy + (q.y + g) * u)
    fun seg(a: Offset, c: Offset, w: Float, col: Color) = drawLine(col, T(a), T(c), w * u, StrokeCap.Round)
    val floorY = oy + 0f * u
    if (p.prop == "water") {
        for (r in 0..1) {
            val path = Path(); var x = b.x0 - .5f; var first = true
            while (x <= b.x1 + .5f) { val y = j.sh.y + g - .35f + r * .9f + sin(x * 3 + time * 2) * .05f
                val pt = Offset(ox + x * u, oy + y * u); if (first) { path.moveTo(pt.x, pt.y); first = false } else path.lineTo(pt.x, pt.y); x += .1f }
            drawPath(path, Color(0x595CE1C6), style = Stroke(.04f * u))
        }
    } else {
        drawLine(Brush.horizontalGradient(listOf(Color(0x007C9CFF), Color(0x8C7C9CFF), Color(0x007C9CFF))),
            Offset(0f, floorY), Offset(W, floorY), .03f * u)
        drawOval(Color(0x1F7C9CFF), topLeft = Offset(ox + (j.hip.x - .9f) * u, floorY - .07f * u), size = Size(1.8f * u, .14f * u))
    }
    drawProps(p.prop, j, ::T, u, back = true)
    val far = Color(0x738FA6FF)
    seg(j.hip, j.lk, .2f, far); seg(j.lk, j.lft, .17f, far); seg(j.sh, j.le, .15f, far); seg(j.le, j.lh, .13f, far)
    // soft glow behind the near body
    drawLine(Color(0x337C9CFF), T(j.hip), T(j.sh), .55f * u, StrokeCap.Round)
    seg(j.hip, j.sh, .3f, Color(0xFFB9C8FF))
    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFA9B9FF)), T(j.hc), HEAD * u), HEAD * u, T(j.hc))
    seg(j.hip, j.rk, .21f, Color(0xFF9FB4FF)); seg(j.rk, j.rft, .18f, Color(0xFF9FB4FF))
    seg(j.sh, j.re, .16f, Color(0xFFD2DCFF)); seg(j.re, j.rh, .14f, Color(0xFFD2DCFF))
    for (q in listOf(j.rk, j.re)) drawCircle(Color(0xFF5CE1C6), .045f * u, T(q))
    drawProps(p.prop, j, ::T, u, back = false)
}

private fun DrawScope.drawProps(prop: String?, j: J, T: (Offset) -> Offset, u: Float, back: Boolean) {
    prop ?: return
    val gold = Color(0xFFFFC876); val faint = Color(0x38FFFFFF); val fill = Color(0x14FFFFFF)
    val hip = T(j.hip); val sh = T(j.sh); val rh = T(j.rh); val lh = T(j.lh)
    if (back) {
        when (prop) {
            "wall" -> { val x = minOf(hip.x, sh.x) - .22f * u; drawRect(fill, Offset(x - .25f * u, hip.y - 4f * u), Size(.25f * u, 4f * u)) }
            "bench" -> { val x0 = minOf(hip.x, sh.x) - .15f * u; val x1 = maxOf(hip.x, sh.x) + .1f * u; val top = maxOf(hip.y, sh.y) + .17f * u
                val bottom = T(j.rft).y; drawRect(fill, Offset(x0, top), Size(x1 - x0, bottom - top)); drawRect(faint, Offset(x0, top), Size(x1 - x0, bottom - top), style = Stroke(.04f * u)) }
            "bench2" -> { val bottom = T(j.rft).y; drawRect(fill, Offset(minOf(lh.x, rh.x) - .6f * u, rh.y), Size(.8f * u, bottom - rh.y)) }
            "rail" -> { val y = T(j.rft).y.coerceAtLeast(hip.y) - .08f * u; drawLine(faint, Offset(hip.x - .4f * u, y), Offset(hip.x + 2.4f * u, y), .04f * u)
                drawCircle(faint, .38f * u, Offset(hip.x + 2.4f * u, y - .37f * u), style = Stroke(.04f * u)) }
            "sled" -> { val y = T(j.rft).y; drawRect(fill, Offset(rh.x + .05f * u, y - .85f * u), Size(.9f * u, .85f * u)) }
            "bike" -> { val gy0 = hip.y + 2.25f * u
                for (dx in listOf(-1.05f, 1.35f)) drawCircle(faint, .58f * u, Offset(hip.x + dx * u, gy0 - .6f * u), style = Stroke(.05f * u))
                val bb = Offset(hip.x + .2f * u, gy0 - .75f * u)
                drawLine(faint, Offset(hip.x - 1.05f * u, gy0 - .6f * u), bb, .05f * u); drawLine(faint, bb, hip, .05f * u)
                drawLine(faint, bb, Offset(hip.x + 1.35f * u, gy0 - .6f * u), .05f * u); drawLine(faint, Offset(hip.x + 1.35f * u, gy0 - .6f * u), rh, .05f * u) }
            "tread" -> { val y = T(j.rft).y.coerceAtLeast(T(j.lft).y)
                drawLine(faint, Offset(hip.x - 1.5f * u, y), Offset(hip.x + 1.6f * u, y - .1f * u), .08f * u)
                drawLine(faint, Offset(hip.x + 1.55f * u, y - .1f * u), Offset(hip.x + 1.75f * u, y - 2.4f * u), .06f * u) }
            "pbar" -> drawLine(faint, rh, Offset(rh.x, 0f), .04f * u)
            "wallr" -> { val x = maxOf(T(j.rft).x, T(j.lft).x) + .05f * u; drawRect(fill, Offset(x, hip.y - 4f * u), Size(.25f * u, 4.5f * u)) }
            "steps" -> { val y = maxOf(T(j.rft).y, T(j.lft).y); val path = Path().apply { moveTo(hip.x - 1.6f * u, y)
                for (i in 0..3) { lineTo(hip.x + (-.2f + i * .55f) * u, y - i * .25f * u); lineTo(hip.x + (-.2f + i * .55f) * u, y - (.25f + i * .25f) * u) } }
                drawPath(path, faint, style = Stroke(.04f * u)) }
        }
        return
    }
    when (prop) {
        "db" -> { for ((h, a) in listOf(rh to 1f, lh to .5f)) { drawRect(gold.copy(alpha = a), Offset(h.x - .17f * u, h.y - .035f * u), Size(.34f * u, .07f * u))
            drawCircle(gold.copy(alpha = a), .08f * u, Offset(h.x - .17f * u, h.y)); drawCircle(gold.copy(alpha = a), .08f * u, Offset(h.x + .17f * u, h.y)) } }
        "kb" -> { drawCircle(gold, .17f * u, Offset(rh.x, rh.y + .22f * u)); drawArc(gold, 180f, 180f, false, Offset(rh.x - .09f * u, rh.y - .02f * u), Size(.18f * u, .18f * u), style = Stroke(.05f * u)) }
        "ball" -> drawCircle(Color(0xFFFF8A5C), .24f * u, Offset(rh.x + .05f * u, rh.y - .08f * u))
        "pbar" -> drawLine(gold, Offset(rh.x - .7f * u, rh.y), Offset(rh.x + .7f * u, rh.y), .07f * u, StrokeCap.Round)
    }
}
