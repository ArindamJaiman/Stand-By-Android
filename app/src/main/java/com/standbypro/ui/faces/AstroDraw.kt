package com.standbypro.ui.faces

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.standbypro.ui.design.safeRadialGradient
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.random.Random

/**
 * Renders the moon at [phase] (0 new → 0.5 full → 1 new) with a correct elliptical
 * terminator, soft limb shading and a few maria for realism.
 */
fun DrawScope.drawMoon(center: Offset, r: Float, phase: Double, lit: Color = Color(0xFFF1EAD2), dark: Color = Color(0xFF1B1D22)) {
    drawCircle(dark, r, center)
    val k = cos(2 * PI * phase).toFloat()          // 1 = new, -1 = full
    val waxing = phase < 0.5
    val disc = Path().apply { addOval(Rect(center, r)) }
    val half = Path().apply {
        val rect = Rect(center, r)
        if (waxing) addArc(rect, -90f, 180f) else addArc(rect, 90f, 180f)
        close()
    }
    val ellipse = Path().apply {
        addOval(Rect(center.x - r * abs(k), center.y - r, center.x + r * abs(k), center.y + r))
    }
    val litPath = if (k > 0) {
        Path.combine(PathOperation.Difference, half, ellipse)
    } else {
        Path.combine(PathOperation.Union, half, ellipse)
    }
    val clipped = Path.combine(PathOperation.Intersect, litPath, disc)
    drawPath(clipped, safeRadialGradient(listOf(lit, lit.copy(alpha = 0.85f), Color(0xFFBFB59A)), center = Offset(center.x - r * 0.25f, center.y - r * 0.25f), radius = r * 1.5f))
    // Maria (seas) — subtle darker patches clipped to the lit area.
    val maria = listOf(
        Triple(-0.25f, -0.2f, 0.22f), Triple(0.18f, -0.32f, 0.16f), Triple(0.05f, 0.05f, 0.2f),
        Triple(-0.35f, 0.25f, 0.13f), Triple(0.3f, 0.28f, 0.1f)
    )
    maria.forEach { (dx, dy, mr) ->
        val m = Path().apply { addOval(Rect(Offset(center.x + dx * r, center.y + dy * r), mr * r)) }
        drawPath(Path.combine(PathOperation.Intersect, m, clipped), Color(0xFF8E8571).copy(alpha = 0.28f))
    }
    // Earthshine on the dark limb.
    drawCircle(Color.White.copy(alpha = 0.04f), r, center)
}

/** Deterministic star field. [twinkle] 0..1 phase animates brightness. */
fun DrawScope.drawStars(count: Int, seed: Int, twinkle: Float, color: Color = Color.White, maxRadius: Float = size.minDimension * 0.006f) {
    val rnd = Random(seed)
    for (i in 0 until count) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        val base = rnd.nextFloat()
        val speed = 0.5f + rnd.nextFloat() * 2f
        val alpha = (0.25f + 0.75f * base) * (0.6f + 0.4f * cos((twinkle * speed + base) * 2f * PI.toFloat()))
        drawCircle(color.copy(alpha = alpha.coerceIn(0.05f, 1f)), maxRadius * (0.35f + base * 0.65f), Offset(x, y))
    }
}
