package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object FridayKahfScheduler {
    // Five optional check-ins. Each alarm is re-armed independently.
    private val reminderHours = intArrayOf(9, 12, 15, 18, 21)

    fun scheduleAll(ctx: Context) {
        reminderHours.indices.forEach { scheduleNext(ctx, it) }
    }

    /**
     * Re-arm only the reminder that just fired. Re-arming all five from the
     * first callback would replace the remaining four alarms for this Friday.
     */
    fun scheduleNext(ctx: Context, reminderIndex: Int) {
        if (reminderIndex !in reminderHours.indices) return
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = nextFridayAt(reminderHours[reminderIndex], System.currentTimeMillis())
        val intent = Intent(ctx, FridayKahfReceiver::class.java).apply {
            putExtra("reminder_index", reminderIndex)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            ctx, 8000 + reminderIndex, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
            )
        }
    }

    internal fun nextFridayAt(hour: Int, nowMillis: Long): Long {
        require(hour in 0..23)
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY ||
            cal.timeInMillis <= nowMillis
        ) {
            val daysUntilFriday = (Calendar.FRIDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7
            cal.add(Calendar.DAY_OF_YEAR, if (daysUntilFriday == 0) 7 else daysUntilFriday)
        }
        return cal.timeInMillis
    }
}
