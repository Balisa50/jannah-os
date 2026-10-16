package com.jannahos.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// A soft mosque silhouette in the top-right of the hero card. Subtle, deep, not distracting.
@Composable
fun MosqueBackdrop(modifier: Modifier = Modifier, tint: Color = JannahColors.Gold.copy(alpha = 0.10f)) {
    Canvas(modifier = modifier) {
        drawMosque(tint)
    }
}

private fun DrawScope.drawMosque(tint: Color) {
    val w = size.width
    val h = size.height
    val base = h * 0.92f
    val cx = w * 0.72f

    // Main dome
    val domeR = w * 0.20f
    val domePath = Path().apply {
        moveTo(cx - domeR, base)
        cubicTo(
            cx - domeR, base - domeR * 1.4f,
            cx + domeR, base - domeR * 1.4f,
            cx + domeR, base
        )
        close()
    }
    drawPath(domePath, color = tint)

    // Small spire on dome
    drawRect(
        color = tint,
        topLeft = Offset(cx - 1.5f, base - domeR * 1.55f),
        size = Size(3f, domeR * 0.35f)
    )
    // Crescent dot
    drawCircle(color = tint, radius = 3f, center = Offset(cx, base - domeR * 1.65f))

    // Left minaret
    val lmX = cx - domeR - w * 0.035f
    drawRect(color = tint, topLeft = Offset(lmX - 3f, base - h * 0.55f), size = Size(6f, h * 0.55f))
    drawCircle(color = tint, radius = 4f, center = Offset(lmX, base - h * 0.55f))

    // Right minaret
    val rmX = cx + domeR + w * 0.035f
    drawRect(color = tint, topLeft = Offset(rmX - 3f, base - h * 0.55f), size = Size(6f, h * 0.55f))
    drawCircle(color = tint, radius = 4f, center = Offset(rmX, base - h * 0.55f))

    // Two small side domes
    val sdR = domeR * 0.5f
    drawCircle(color = tint, radius = sdR, center = Offset(cx - domeR * 0.85f, base - sdR * 0.8f))
    drawCircle(color = tint, radius = sdR, center = Offset(cx + domeR * 0.85f, base - sdR * 0.8f))

    // Base building
    drawRect(
        color = tint,
        topLeft = Offset(cx - domeR * 1.3f, base - 2f),
        size = Size(domeR * 2.6f, 3f)
    )
}

// A simple sun glyph for prayer rows. Small, elegant.
@Composable
fun SunGlyph(color: Color, rising: Boolean = false, size: androidx.compose.ui.unit.Dp = 16.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.minDimension * 0.28f
        drawCircle(color = color, radius = r, center = c)
        if (!rising) {
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0))
                val start = Offset(
                    (c.x + Math.cos(angle) * (r + 2f)).toFloat(),
                    (c.y + Math.sin(angle) * (r + 2f)).toFloat()
                )
                val end = Offset(
                    (c.x + Math.cos(angle) * (r + 4f)).toFloat(),
                    (c.y + Math.sin(angle) * (r + 4f)).toFloat()
                )
                drawLine(color = color, start = start, end = end, strokeWidth = 1.4f)
            }
        }
    }
}

// A crescent moon glyph.
@Composable
fun MoonGlyph(color: Color, size: androidx.compose.ui.unit.Dp = 16.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.minDimension * 0.32f
        drawCircle(color = color, radius = r, center = c)
        drawCircle(
            color = Color.Transparent,
            radius = r * 0.78f,
            center = Offset(c.x + r * 0.42f, c.y - r * 0.22f)
        )
    }
}

// Small open-book glyph for the Quran quick action.
@Composable
fun BookGlyph(color: Color, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val w = this.size.width
        val h = this.size.height
        val spineX = w / 2f
        // Left page
        val leftPath = Path().apply {
            moveTo(spineX, h * 0.22f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.15f, h * 0.25f, w * 0.1f, h * 0.3f)
            lineTo(w * 0.1f, h * 0.82f)
            cubicTo(w * 0.2f, h * 0.76f, w * 0.35f, h * 0.72f, spineX, h * 0.78f)
            close()
        }
        drawPath(leftPath, color = color)
        // Right page
        val rightPath = Path().apply {
            moveTo(spineX, h * 0.22f)
            cubicTo(w * 0.65f, h * 0.15f, w * 0.85f, h * 0.25f, w * 0.9f, h * 0.3f)
            lineTo(w * 0.9f, h * 0.82f)
            cubicTo(w * 0.8f, h * 0.76f, w * 0.65f, h * 0.72f, spineX, h * 0.78f)
            close()
        }
        drawPath(rightPath, color = color)
        drawLine(color = Color.Black.copy(alpha = 0.4f),
            start = Offset(spineX, h * 0.22f),
            end = Offset(spineX, h * 0.78f),
            strokeWidth = 1.2f)
    }
}

// Prayer-beads glyph for Dhikr. A simple circle with tick marks.
@Composable
fun BeadsGlyph(color: Color, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension * 0.35f
        val beads = 12
        for (i in 0 until beads) {
            val angle = (i * (360.0 / beads)) * Math.PI / 180.0
            val x = cx + (Math.cos(angle) * r).toFloat()
            val y = cy + (Math.sin(angle) * r).toFloat()
            drawCircle(color = color, radius = this.size.minDimension * 0.045f,
                center = Offset(x, y))
        }
        // Small tassel
        drawCircle(color = color, radius = this.size.minDimension * 0.05f,
            center = Offset(cx, cy + r + this.size.minDimension * 0.12f))
    }
}
