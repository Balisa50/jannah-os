package com.jannahos.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType

class JannahKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        val view = ComposeView(this)
        view.setContent {
            JannahKeyboardUI(
                onKeyPress = { char ->
                    currentInputConnection?.commitText(char.toString(), 1)
                },
                onDelete = {
                    currentInputConnection?.deleteSurroundingText(1, 0)
                },
                onInsertArabic = { text ->
                    currentInputConnection?.commitText(text, 1)
                }
            )
        }
        return view
    }
}

@Composable
fun JannahKeyboardUI(
    onKeyPress: (Char) -> Unit,
    onDelete: () -> Unit,
    onInsertArabic: (String) -> Unit
) {
    // Jannah Bar (Islamic Shortcuts)
    val jannahShortcuts = listOf(
        "ﷺ" to "ﷺ",
        "الله" to "الله",
        "SWT" to "سبحانه وتعالى"
    )

    // QWERTY Layout
    val rows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
        listOf("123", ",", " ", ".", "⏎")
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(JannahColors.Cream)
            .padding(8.dp)
    ) {
        // Jannah Bar
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            jannahShortcuts.forEach { (label, value) ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(JannahColors.OrangeSoft)
                        .clickable { onInsertArabic(value) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontFamily = FontFamily.Serif, color = JannahColors.OrangeDeep, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // QWERTY Rows
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    Box(
                        Modifier
                            .weight(if (key == " ") 4f else 1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable {
                                when (key) {
                                    "⌫" -> onDelete()
                                    "⏎" -> onKeyPress('\n')
                                    "⇧" -> {} // Shift logic for later
                                    "123" -> {} // Number logic for later
                                    else -> onKeyPress(key.first())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key.uppercase(),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 18.sp,
                            color = JannahColors.Ink,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
