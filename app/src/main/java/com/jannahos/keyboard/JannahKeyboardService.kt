package com.jannahos.keyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class JannahKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        // We are using native Android views here. It will 100% render.
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FBF6EE")) // Jannah Cream
            setPadding(16, 16, 16, 16)
        }

        // 1. The Jannah Bar
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        val shortcuts = listOf("ﷺ", "الله", "SWT", "Quran")
        shortcuts.forEach { text ->
            val btn = Button(this).apply {
                this.text = text
                setTextColor(Color.parseColor("#C2410C")) // Orange
                setBackgroundColor(Color.parseColor("#FFF3E0"))
                textSize = 14f
                setOnClickListener {
                    if (text == "Quran") {
                        // Later we will open a dialog here
                        currentInputConnection?.commitText("[Quran 2:255]", 1)
                    } else if (text == "SWT") {
                        currentInputConnection?.commitText("سبحانه وتعالى", 1)
                    } else {
                        currentInputConnection?.commitText(text, 1)
                    }
                }
            }
            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            params.setMargins(4, 0, 4, 0)
            topBar.addView(btn, params)
        }
        root.addView(topBar)

        // 2. QWERTY Layout
        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "DEL"),
            listOf("123", ",", " ", ".", "ENTER")
        )

        rows.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { key ->
                val btn = TextView(this).apply {
                    this.text = key.uppercase()
                    textSize = 18f
                    setTextColor(Color.parseColor("#2B1410")) // Jannah Ink
                    setBackgroundColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    setPadding(0, 24, 0, 24)
                    setOnClickListener {
                        when (key) {
                            "DEL" -> currentInputConnection?.deleteSurroundingText(1, 0)
                            "ENTER" -> currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
                            "SHIFT", "123" -> {}
                            else -> currentInputConnection?.commitText(key, 1)
                        }
                    }
                }
                val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, if (key == " ") 4f else 1f)
                params.setMargins(4, 4, 4, 4)
                rowLayout.addView(btn, params)
            }
            root.addView(rowLayout)
        }

        return root
    }
}
