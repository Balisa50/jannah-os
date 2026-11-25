package com.jannahos

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
import kotlin.random.Random

object QuranLockScheduler {
    private const val REQ_CODE = 777
    private val safeWindows = listOf(Pair(480, 660), Pair(840, 1020), Pair(1200, 1320))

    fun scheduleRandomLock(ctx: Context) {
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val targetDate = Calendar.getInstance()
        val currentMinute = targetDate.get(Calendar.HOUR_OF_DAY) * 60 +
            targetDate.get(Calendar.MINUTE)

        var targetMinute = -1
        for (window in safeWindows) {
            if (currentMinute < window.second) {
                val start = maxOf(currentMinute + 30, window.first)
                if (start < window.second) {
                    targetMinute = Random.nextInt(start, window.second)
                    break
                }
            }
        }

        if (targetMinute == -1) {
            // All remaining windows have passed: use tomorrow's first window.
            targetDate.add(Calendar.DAY_OF_YEAR, 1)
            targetMinute = Random.nextInt(safeWindows.first().first, safeWindows.first().second)
        }

        val triggerTime = targetDate.apply {
            set(Calendar.HOUR_OF_DAY, targetMinute / 60)
            set(Calendar.MINUTE, targetMinute % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        scheduleAlarm(ctx, alarmManager, triggerTime)
    }

    fun scheduleNextDay(ctx: Context) {
        val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val window = safeWindows.random()
        val targetMinute = Random.nextInt(window.first, window.second)
        val triggerTime = date.apply {
            set(Calendar.HOUR_OF_DAY, targetMinute / 60)
            set(Calendar.MINUTE, targetMinute % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleAlarm(ctx, alarmManager, triggerTime)
    }

    fun scheduleSnooze(ctx: Context, minutes: Int) {
        if (minutes <= 0) return
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (minutes * 60_000L)
        scheduleAlarm(ctx, alarmManager, triggerTime)
    }

    fun cancel(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, QuranLockReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx, REQ_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        pi.cancel()
    }

    private fun scheduleAlarm(ctx: Context, am: AlarmManager, timeMs: Long) {
        val intent = Intent(ctx, QuranLockReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            ctx, REQ_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
        }
    }
}
