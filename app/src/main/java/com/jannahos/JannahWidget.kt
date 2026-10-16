package com.jannahos

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JannahWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Update immediately when the user adds the widget.
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, JannahWidget::class.java))
        for (id in ids) updateWidget(context, mgr, id)
    }

    private fun updateWidget(
        context: Context,
        mgr: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.jannah_widget)

        val lat = Settings.lat(context)
        val lon = Settings.lon(context)
        val times = try { SalahScheduler.todayTimes(lat, lon) } catch (e: Exception) { emptyMap() }
        val now = System.currentTimeMillis()
        val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }

        val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (nextKey != null) {
            val t = times[nextKey]
            val label = SalahTracker.label(nextKey)
            views.setTextViewText(R.id.widget_prayer, label)
            views.setTextViewText(R.id.widget_time, hhmm.format(t))
        } else {
            views.setTextViewText(R.id.widget_prayer, "Fajr tomorrow")
            views.setTextViewText(R.id.widget_time, ", ")
        }

        // Tahajjud time
        val tahajjud = try { TahajjudScheduler.computeTonight(lat, lon) } catch (e: Exception) { Date() }
        views.setTextViewText(R.id.widget_tahajjud, "Tahajjud · " + hhmm.format(tahajjud))

        // Tap opens the app
        val open = Intent(context, MainActivity::class.java)
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getActivity(context, 0, open, flags)
        views.setOnClickPendingIntent(R.id.widget_prayer, pi)
        views.setOnClickPendingIntent(R.id.widget_tahajjud, pi)

        mgr.updateAppWidget(widgetId, views)
    }
}
