package com.standbypro.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import com.standbypro.domain.WeatherCondition
import kotlin.math.cos
import kotlin.math.sin

/** Hand-drawn, colour weather glyphs (Apple-weather style) that scale crisply at any size. */
@Composable
fun WeatherGlyph(condition: WeatherCondition, modifier: Modifier = Modifier, monochrome: Color? = null) {
    Canvas(modifier = modifier) { drawWeatherGlyph(condition, Offset(size.width / 2f, size.height / 2f), size.minDimension / 2f, monochrome) }
}

private val SunColor = Color(0xFFFFCC33)
private val MoonColor = Color(0xFFE9E4D0)
private val CloudColor = Color(0xFFF2F4F8)
private val CloudShade = Color(0xFFB8C0CC)
private val RainColor = Color(0xFF5AC8FA)
private val BoltColor = Color(0xFFFFD60A)

fun DrawScope.drawWeatherGlyph(condition: WeatherCondition, c: Offset, r: Float, mono: Color? = null) {
    when (condition) {
        WeatherCondition.CLEAR_DAY -> sun(c, r * 0.95f, mono)
        WeatherCondition.CLEAR_NIGHT -> moon(c, r * 0.8f, mono)
        WeatherCondition.PARTLY_CLOUDY_DAY -> {
            sun(Offset(c.x - r * 0.25f, c.y - r * 0.25f), r * 0.62f, mono)
            cloud(Offset(c.x + r * 0.12f, c.y + r * 0.2f), r * 0.78f, mono ?: CloudColor)
        }
        WeatherCondition.PARTLY_CLOUDY_NIGHT -> {
            moon(Offset(c.x - r * 0.28f, c.y - r * 0.28f), r * 0.5f, mono)
            cloud(Offset(c.x + r * 0.12f, c.y + r * 0.2f), r * 0.78f, mono ?: CloudColor)
        }
        WeatherCondition.CLOUDY -> {
            cloud(Offset(c.x + r * 0.22f, c.y - r * 0.18f), r * 0.6f, mono?.copy(alpha = 0.6f) ?: CloudShade)
            cloud(Offset(c.x - r * 0.05f, c.y + r * 0.15f), r * 0.85f, mono ?: CloudColor)
        }
        WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> {
            cloud(Offset(c.x, c.y - r * 0.2f), r * 0.85f, mono ?: CloudColor)
            val drops = if (condition == WeatherCondition.HEAVY_RAIN) 4 else 3
            for (i in 0 until drops) {
                val x = c.x - r * 0.45f + i * (r * 0.9f / (drops - 1))
                drawLine(mono ?: RainColor, Offset(x, c.y + r * 0.35f), Offset(x - r * 0.12f, c.y + r * 0.75f), r * 0.1f, StrokeCap.Round)
            }
        }
        WeatherCondition.THUNDERSTORM -> {
            cloud(Offset(c.x, c.y - r * 0.25f), r * 0.85f, mono ?: CloudShade)
            val bolt = Path().apply {
                moveTo(c.x + r * 0.08f, c.y + r * 0.05f)
                lineTo(c.x - r * 0.2f, c.y + r * 0.5f)
                lineTo(c.x + r * 0.02f, c.y + r * 0.5f)
                lineTo(c.x - r * 0.12f, c.y + r * 0.95f)
                lineTo(c.x + r * 0.3f, c.y + r * 0.38f)
                lineTo(c.x + r * 0.06f, c.y + r * 0.38f)
                close()
            }
            drawPath(bolt, mono ?: BoltColor)
        }
        WeatherCondition.SNOW -> {
            cloud(Offset(c.x, c.y - r * 0.2f), r * 0.85f, mono ?: CloudColor)
            for (i in 0 until 3) {
                val p = Offset(c.x - r * 0.4f + i * r * 0.4f, c.y + r * 0.6f + if (i == 1) r * 0.15f else 0f)
                for (k in 0 until 3) {
                    val a = k * 60.0 * Math.PI / 180.0
                    val dx = (cos(a) * r * 0.11f).toFloat()
                    val dy = (sin(a) * r * 0.11f).toFloat()
                    drawLine(mono ?: Color.White, Offset(p.x - dx, p.y - dy), Offset(p.x + dx, p.y + dy), r * 0.05f, StrokeCap.Round)
                }
            }
        }
        WeatherCondition.FOG -> {
            for (i in 0 until 4) {
                val y = c.y - r * 0.45f + i * r * 0.3f
                val inset = if (i % 2 == 0) 0f else r * 0.18f
                drawLine(mono ?: CloudShade, Offset(c.x - r * 0.8f + inset, y), Offset(c.x + r * 0.8f - inset, y), r * 0.13f, StrokeCap.Round)
            }
        }
        WeatherCondition.WINDY -> {
            val col = mono ?: CloudColor
            for (i in 0 until 3) {
                val y = c.y - r * 0.4f + i * r * 0.4f
                val path = Path().apply {
                    moveTo(c.x - r * 0.85f, y)
                    lineTo(c.x + r * (0.35f - i * 0.15f), y)
                    quadraticTo(c.x + r * (0.75f - i * 0.15f), y, c.x + r * (0.62f - i * 0.15f), y - r * 0.28f)
                }
                drawPath(path, col, style = Stroke(r * 0.1f, cap = StrokeCap.Round))
            }
        }
    }
}

private fun DrawScope.sun(c: Offset, r: Float, mono: Color?) {
    val col = mono ?: SunColor
    drawCircle(col.copy(alpha = 0.18f), r * 0.95f, c)
    drawCircle(col, r * 0.5f, c)
    for (i in 0 until 8) {
        val a = i * 45.0 * Math.PI / 180.0
        val s = Offset(c.x + (cos(a) * r * 0.66f).toFloat(), c.y + (sin(a) * r * 0.66f).toFloat())
        val e = Offset(c.x + (cos(a) * r * 0.9f).toFloat(), c.y + (sin(a) * r * 0.9f).toFloat())
        drawLine(col, s, e, r * 0.11f, StrokeCap.Round)
    }
}

private fun DrawScope.moon(c: Offset, r: Float, mono: Color?) {
    val col = mono ?: MoonColor
    val path = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(c, r))
    }
    val cut = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(Offset(c.x + r * 0.45f, c.y - r * 0.3f), r * 0.85f))
    }
    val crescent = Path.combine(androidx.compose.ui.graphics.PathOperation.Difference, path, cut)
    drawPath(crescent, col)
}

private fun DrawScope.cloud(c: Offset, r: Float, color: Color) {
    translate(0f, 0f) {
        drawCircle(color, r * 0.42f, Offset(c.x - r * 0.38f, c.y + r * 0.1f))
        drawCircle(color, r * 0.55f, Offset(c.x + r * 0.05f, c.y - r * 0.12f))
        drawCircle(color, r * 0.4f, Offset(c.x + r * 0.45f, c.y + r * 0.12f))
        drawRoundRect(
            color,
            topLeft = Offset(c.x - r * 0.8f, c.y + r * 0.05f),
            size = androidx.compose.ui.geometry.Size(r * 1.65f, r * 0.47f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.24f, r * 0.24f)
        )
    }
}
