package com.jannahos.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun SwipeBackScreen(
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    var dragTotal by remember { mutableFloatStateOf(0f) }
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0) dragTotal += dragAmount
                        else dragTotal = 0f
                    },
                    onDragEnd = {
                        if (dragTotal > 100f) onBack()
                        dragTotal = 0f
                    },
                    onDragCancel = { dragTotal = 0f }
                )
            }
    ) {
        content()
    }
}
