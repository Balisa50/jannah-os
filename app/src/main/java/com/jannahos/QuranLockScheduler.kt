package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
import kotlin.random.Random

object QuranLockScheduler {
    fun scheduleRandomLock(ctx: Context) {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Calculate milliseconds until midnight
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
        }
        
        val timeUntilMidnight = midnight.timeInMillis - now.timeInMillis
        if (timeUntilMidnight <= 0) return

        // Pick a random time between now and midnight (e.g., 10 mins from now to 11:59 PM)
        val randomOffset = Random.nextLong(10 * 60 * 1000L, timeUntilMidnight)
        val triggerTime = now.timeInMillis + randomOffset

        val intent = Intent(ctx, QuranLockReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            ctx, 777, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
