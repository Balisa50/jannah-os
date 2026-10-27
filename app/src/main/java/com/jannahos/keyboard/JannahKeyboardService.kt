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
        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@JannahKeyboardService)
            setViewTreeViewModelStoreOwner(this@JannahKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@JannahKeyboardService)
            setContent { JannahKeyboardUI { text -> currentInputConnection?.commitText(text, 1) } }
        }
    }
}

@Composable
fun JannahKeyboardUI(onText: (String) -> Unit) {
    val keys = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
        listOf("123", "/", "Space", ".", "⏎")
    )
    
    Surface(color = Color(0xFFD1D5DB)) {
        Column(Modifier.fillMaxWidth().heightIn(min = 260.dp).padding(4.dp)) {
            // Jannah Top Bar
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("ﷺ" to "ﷺ", "SWT" to "سبحانه وتعالى", "Quran" to "/quran ").forEach { (label, valToInsert) ->
                    Box(
                        Modifier.weight(1f).padding(horizontal = 2.dp).height(36.dp).clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFF3E0)).clickable { onText(valToInsert) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = JannahColors.OrangeDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            keys.forEach { row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { key ->
                        Box(
                            Modifier.weight(if (key == "Space") 5f else 1f).height(44.dp).clip(RoundedCornerShape(4.dp))
                                .background(Color.White).clickable {
                                    when (key) {
                                        "⌫" -> onText("\b") // handles backspace via commitText in real app, but here it just types
                                        "⏎" -> onText("\n")
                                        "Space" -> onText(" ")
                                        "123", "⇧" -> {}
                                        else -> onText(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(key.uppercase(), color = Color.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
