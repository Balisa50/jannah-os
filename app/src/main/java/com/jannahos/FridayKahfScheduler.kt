package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object FridayKahfScheduler {
    
    // 5 distinct times during Friday (24hr format)
    private val reminderHours = intArrayOf(9, 12, 15, 18, 21)

    fun scheduleAll(ctx: Context) {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        for (i in reminderHours.indices) {
            val hour = reminderHours[i]
            val triggerTime = getNextFridayAt(hour)

            val intent = Intent(ctx, FridayKahfReceiver::class.java).apply {
                putExtra("reminder_index", i)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                ctx, 8000 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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

    private fun getNextFridayAt(hour: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        // If the time has already passed today, or today isn't Friday, move to next Friday
        if (cal.timeInMillis <= System.currentTimeMillis() || cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
        }
        return cal.timeInMillis
    }
}
