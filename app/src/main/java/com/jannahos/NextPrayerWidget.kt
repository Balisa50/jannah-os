package com.jannahos

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NextPrayerWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, com.jannahos.R.layout.widget_next_prayer)
            
            try {
                val times = SalahScheduler.todayTimes(Settings.lat(context), Settings.lon(context))
                val now = System.currentTimeMillis()
                val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
                
                val nextKey = ordered.firstOrNull { (times[it] ?: 0L) > now }
                
                if (nextKey != null) {
                    val nextTime = times[nextKey] ?: now
                    val diff = nextTime - now
                    val h = diff / 3600000
                    val m = (diff / 60000) % 60
                    val s = (diff / 1000) % 60
                    val timeStr = String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
                    val clockStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(nextTime))
                    
                    views.setTextViewText(com.jannahos.R.id.widget_prayer_name, SalahTracker.label(nextKey).uppercase())
                    views.setTextViewText(com.jannahos.R.id.widget_time_clock, "at $clockStr")
                    
                    // Native chronometer ticks down automatically
                    views.setChronometer(com.jannahos.R.id.widget_chronometer, nextTime, null, true)
                    views.setChronometerCountDown(com.jannahos.R.id.widget_chronometer, true)
                } else {
                    views.setTextViewText(com.jannahos.R.id.widget_prayer_name, "FAJR")
                    views.setTextViewText(com.jannahos.R.id.widget_time_clock, "Tomorrow")
                }
            } catch (e: Exception) {
                views.setTextViewText(com.jannahos.R.id.widget_prayer_name, "Jannah OS")
                views.setTextViewText(com.jannahos.R.id.widget_time_clock, "")
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
