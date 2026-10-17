package com.jannahos.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput

// Swipe from the left edge to the right to go back.
// Threshold is 150dp of total drag distance.
fun Modifier.swipeToBack(onBack: () -> Unit): Modifier = composed {
    var dragTotal by remember { mutableStateOf(0f) }
    pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragEnd = {
                if (dragTotal > 150f) {
                    onBack()
                }
                dragTotal = 0f
            },
            onHorizontalDrag = { _, dragAmount ->
                // Only count drags starting from the left edge? Simpler: any drag
                if (dragAmount > 0) dragTotal += dragAmount
            }
        )
    }
}
