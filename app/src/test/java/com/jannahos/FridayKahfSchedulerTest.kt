package com.jannahos

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FridayKahfSchedulerTest {
    private fun date(day: Int, hour: Int, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun calendar(time: Long) = Calendar.getInstance().apply { timeInMillis = time }

    @Test fun fridayMorningReminderCanStillFireToday() {
        val now = date(27, 8)
        val next = calendar(FridayKahfScheduler.nextFridayAt(9, now))
        assertEquals(Calendar.FRIDAY, next.get(Calendar.DAY_OF_WEEK))
        assertEquals(27, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, next.get(Calendar.HOUR_OF_DAY))
    }

    @Test fun passedFridayReminderMovesToFollowingFriday() {
        val now = date(27, 10)
        val next = calendar(FridayKahfScheduler.nextFridayAt(9, now))
        assertEquals(Calendar.FRIDAY, next.get(Calendar.DAY_OF_WEEK))
        assertEquals(4, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, next.get(Calendar.MONTH))
        assertEquals(9, next.get(Calendar.HOUR_OF_DAY))
    }

    @Test fun thursdayReminderTargetsTomorrow() {
        val now = date(26, 10)
        val next = calendar(FridayKahfScheduler.nextFridayAt(9, now))
        assertEquals(Calendar.FRIDAY, next.get(Calendar.DAY_OF_WEEK))
        assertEquals(27, next.get(Calendar.DAY_OF_MONTH))
    }

    @Test fun saturdayReminderTargetsNextWeek() {
        val now = date(28, 10)
        val next = calendar(FridayKahfScheduler.nextFridayAt(9, now))
        assertEquals(Calendar.FRIDAY, next.get(Calendar.DAY_OF_WEEK))
        assertEquals(4, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, next.get(Calendar.MONTH))
    }

    @Test fun resultIsAlwaysStrictlyInTheFuture() {
        val now = date(27, 9)
        assertTrue(FridayKahfScheduler.nextFridayAt(9, now) > now)
    }
}
