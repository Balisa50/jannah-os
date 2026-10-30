package com.jannahos.keyboard

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.jannahos.R

class JannahInputMethodService : InputMethodService() {

    private lateinit var keyboardRows: LinearLayout

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_layout, null)
        keyboardRows = root.findViewById(R.id.keyboard_rows)

        root.findViewById<TextView>(R.id.key_pbuh).setOnClickListener { commitText("ﷺ") }
        root.findViewById<TextView>(R.id.key_swt).setOnClickListener { commitText(" سبحانه وتعالى") }
        root.findViewById<TextView>(R.id.key_quran).setOnClickListener { commitText("/quran 2:255") }

        buildKeyboard()
        return root
    }

    private fun buildKeyboard() {
        keyboardRows.removeAllViews()
        val rows = listOf("qwertyuiop", "asdfghjkl", "⇧zxcvbnm⌫")

        rows.forEachIndexed { index, rowText ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            when (index) {
                1 -> row.setPadding(dp(12), 0, dp(12), 0)
                2 -> row.setPadding(dp(3), 0, dp(3), 0)
            }

            rowText.forEach { character ->
                val label = character.toString()
                val key = createKey(label)
                val weight = if (label == "⇧" || label == "⌫") 1.5f else 1f
                val params = LinearLayout.LayoutParams(0, dp(48), weight).apply {
                    setMargins(dp(2), dp(3), dp(2), dp(3))
                }
                row.addView(key, params)
            }
            keyboardRows.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        addBottomKey(bottomRow, "?123", 1.4f) {}
        addBottomKey(bottomRow, ",", 0.7f) { commitText(",") }
        addBottomKey(bottomRow, "English", 4.5f) { commitText(" ") }
        addBottomKey(bottomRow, ".", 0.7f) { commitText(".") }
        addBottomKey(bottomRow, "↵", 1.2f) {
            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
        }
        keyboardRows.addView(bottomRow)
    }

    private fun createKey(label: String): TextView {
        return TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(49, 45, 40))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (label.length == 1 && label[0].isLetter()) 20f else 15f)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(7).toFloat()
                setStroke(dp(1), Color.rgb(230, 224, 215))
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { handleKey(label) }
        }
    }

    private fun addBottomKey(row: LinearLayout, label: String, weight: Float, action: () -> Unit) {
        val key = createKey(label)
        val params = LinearLayout.LayoutParams(0, dp(46), weight).apply {
            setMargins(dp(2), dp(3), dp(2), dp(3))
        }
        key.setOnClickListener { action() }
        row.addView(key, params)
    }

    private fun handleKey(label: String) {
        when (label) {
            "⇧" -> {}
            "⌫" -> deleteBeforeCursor()
            else -> commitText(label)
        }
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun deleteBeforeCursor() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
