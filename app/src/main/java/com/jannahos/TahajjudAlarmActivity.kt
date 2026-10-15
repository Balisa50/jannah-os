package com.jannahos

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class TahajjudAlarmActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        val title = TextView(this).apply {
            text = "It is time."
            textSize = 32f
            gravity = Gravity.CENTER
        }

        val sub = TextView(this).apply {
            text = "Get up. Make wudu. Pray."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 64)
        }

        val awake = Button(this).apply {
            text = "I am awake"
            setOnClickListener {
                // For now, just close. Next milestone: wudu screen.
                finish()
            }
        }

        val back = Button(this).apply {
            text = "Not now"
            setOnClickListener {
                // Logged for the "what got in the way" screen later.
                finish()
            }
        }

        layout.addView(title)
        layout.addView(sub)
        layout.addView(awake)
        layout.addView(back)

        setContentView(layout)
    }
}
