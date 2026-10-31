package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object RamadanAlarmScheduler {
    private const val SUHOOR_REQ = 8801
    private const val IFTAR_REQ = 8802

    fun scheduleAlarms(ctx: Context) {
        val times = SalahScheduler.todayTimes(Settings.lat(ctx), Settings.lon(ctx))
        val fajrTime = times["fajr"] ?: return
        val maghribTime = times["maghrib"] ?: return

        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Suhoor Alarm: 30 minutes before Fajr
        val suhoorTime = fajrTime - (30 * 60 * 1000L)
        if (suhoorTime > System.currentTimeMillis()) {
            schedule(ctx, am, SUHOOR_REQ, suhoorTime, "Suhoor is ending", "You have 30 minutes until Fajr.")
        }

        // Iftar Alarm: 15 minutes before Maghrib
        val iftarTime = maghribTime - (15 * 60 * 1000L)
        if (iftarTime > System.currentTimeMillis()) {
            schedule(ctx, am, IFTAR_REQ, iftarTime, "Iftar is near", "15 minutes until Maghrib.")
        }
    }

    private fun schedule(ctx: Context, am: AlarmManager, reqCode: Int, time: Long, title: String, body: String) {
        val intent = Intent(ctx, RamadanAlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
        }
        val pi = PendingIntent.getBroadcast(ctx, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        }
    }
}
