package com.jannahos.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BookGlyph(color: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val w = this.size.width
        val h = this.size.height
        val spineX = w / 2f
        val leftPath = Path().apply {
            moveTo(spineX, h * 0.22f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.15f, h * 0.25f, w * 0.1f, h * 0.3f)
            lineTo(w * 0.1f, h * 0.82f)
            cubicTo(w * 0.2f, h * 0.76f, w * 0.35f, h * 0.72f, spineX, h * 0.78f)
            close()
        }
        drawPath(leftPath, color = color)
        val rightPath = Path().apply {
            moveTo(spineX, h * 0.22f)
            cubicTo(w * 0.65f, h * 0.15f, w * 0.85f, h * 0.25f, w * 0.9f, h * 0.3f)
            lineTo(w * 0.9f, h * 0.82f)
            cubicTo(w * 0.8f, h * 0.76f, w * 0.65f, h * 0.72f, spineX, h * 0.78f)
            close()
        }
        drawPath(rightPath, color = color)
    }
}

@Composable
fun BeadsGlyph(color: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension * 0.35f
        val beads = 12
        for (i in 0 until beads) {
            val angle = (i * (360.0 / beads)) * Math.PI / 180.0
            val x = cx + (Math.cos(angle) * r).toFloat()
            val y = cy + (Math.sin(angle) * r).toFloat()
            drawCircle(color = color, radius = this.size.minDimension * 0.05f, center = Offset(x, y))
        }
        drawCircle(color = color, radius = this.size.minDimension * 0.055f,
            center = Offset(cx, cy + r + this.size.minDimension * 0.12f))
    }
}

@Composable
fun SunGlyph(color: Color, size: Dp = 16.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.minDimension * 0.28f
        drawCircle(color = color, radius = r, center = c)
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45.0))
            val start = Offset((c.x + Math.cos(angle) * (r + 2f)).toFloat(),
                (c.y + Math.sin(angle) * (r + 2f)).toFloat())
            val end = Offset((c.x + Math.cos(angle) * (r + 4f)).toFloat(),
                (c.y + Math.sin(angle) * (r + 4f)).toFloat())
            drawLine(color = color, start = start, end = end, strokeWidth = 1.4f)
        }
    }
}

@Composable
fun MoonGlyph(color: Color, size: Dp = 16.dp) {
    Canvas(modifier = Modifier.height(size).fillMaxWidth(0f)) {
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.minDimension * 0.32f
        drawCircle(color = color, radius = r, center = c)
        drawCircle(color = Color.Transparent, radius = r * 0.78f,
            center = Offset(c.x + r * 0.42f, c.y - r * 0.22f))
    }
}
