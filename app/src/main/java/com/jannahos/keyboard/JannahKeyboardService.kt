package com.jannahos.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahTheme

class JannahKeyboardService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onCreateInputView(): View {
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@JannahKeyboardService)
            setViewTreeViewModelStoreOwner(this@JannahKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@JannahKeyboardService)
            
            // THIS IS THE FIX: Wrap in JannahTheme and Surface
            setContent {
                JannahTheme {
                    Surface(color = JannahColors.Cream) {
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
                }
            }
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
    val jannahShortcuts = listOf(
        "ﷺ" to "ﷺ",
        "الله" to "الله",
        "SWT" to "سبحانه وتعالى"
    )

    val rows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
        listOf("123", ",", " ", ".", "⏎")
    )

    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp) // Force a minimum height
            .background(JannahColors.Cream)
            .padding(8.dp)
            .navigationBarsPadding()
    ) {
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
                                    "⇧" -> {} 
                                    "123" -> {} 
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
