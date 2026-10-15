package com.jannahos

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val lat = 13.4549
    private val lon = -16.5790

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        render()
    }

    private fun render() {
        val next = TahajjudScheduler.computeTonight(lat, lon)
        val fmt = SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault())

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(64, 64, 64, 64)

        val heading = TextView(this)
        heading.text = "Tonight"
        heading.textSize = 28f
        heading.gravity = Gravity.CENTER

        val whenText = TextView(this)
        whenText.text = "Suggested tahajjud:\n" + fmt.format(next)
        whenText.textSize = 18f
        whenText.gravity = Gravity.CENTER
        whenText.setPadding(0, 32, 0, 64)

        val setBtn = Button(this)
        setBtn.text = "Set tahajjud alarm"
        setBtn.setOnClickListener {
            TahajjudScheduler.schedule(this, next)
            render()
        }

        val cancelBtn = Button(this)
        cancelBtn.text = "Cancel alarm"
        cancelBtn.setOnClickListener {
            TahajjudScheduler.cancel(this)
            render()
        }

        val status = TextView(this)
        val t = TahajjudScheduler.nextScheduled(this)
        status.text = if (t > 0L) "Alarm set: " + fmt.format(Date(t)) else "No alarm set"
        status.textSize = 14f
        status.gravity = Gravity.CENTER
        status.setPadding(0, 32, 0, 0)

        layout.addView(heading)
        layout.addView(whenText)
        layout.addView(setBtn)
        layout.addView(cancelBtn)
        layout.addView(status)

        setContentView(layout)
    }
}
