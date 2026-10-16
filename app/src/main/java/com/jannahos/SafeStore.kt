package com.jannahos

import android.content.Context

// All encrypted-database reads from UI go through here. If SQLCipher fails
// to open (rare but real on some devices), we return safe fallbacks instead
// of crashing the composition. Failure surfaces as an empty state, not an exit.
object SafeStore {

    fun <T> read(default: T, block: () -> T): T =
        try { block() } catch (t: Throwable) { default }

    fun stats30(context: Context): WakeLog.FormationStats =
        read(WakeLog.FormationStats(0, 0, 0, 0, 0f)) {
            WakeLog(context).formationStats(30)
        }

    fun stats(context: Context, days: Int): WakeLog.FormationStats =
        read(WakeLog.FormationStats(0, 0, 0, 0, 0f)) {
            WakeLog(context).formationStats(days)
        }

    fun lastNights(context: Context, n: Int): List<WakeLog.Event> =
        read(emptyList()) { WakeLog(context).lastNights(n) }

    fun prayedCount(context: Context): Int =
        read(0) { WakeLog(context).prayedCount() }

    fun latestDiary(context: Context): DiaryStore.Entry? =
        read(null) { DiaryStore(context).latestTagIn24h() }

    fun diaryRecent(context: Context, n: Int): List<DiaryStore.Entry> =
        read(emptyList()) { DiaryStore(context).recent(n) }

    fun struggleHabits(context: Context): List<StruggleTracker.Habit> =
        read(emptyList()) { StruggleTracker(context).habits() }

    fun fastToday(context: Context): String? =
        read(null) { FastLog(context).todayKind() }

    fun fastCount30(context: Context): Int =
        read(0) { FastLog(context).countLast(30) }

    fun sleepNights(context: Context, n: Int): List<SleepLog.Entry> =
        read(emptyList()) { SleepLog(context).lastNights(n) }

    fun sleepAvg(context: Context, n: Int): Float =
        read(0f) { SleepLog(context).averageHours(n) }

    fun duaAll(context: Context): List<DuaJournalStore.Dua> =
        read(emptyList()) { DuaJournalStore(context).all() }

    fun duaAnsweredCount(context: Context): Int =
        read(0) { DuaJournalStore(context).answeredCount() }

    fun duaPendingCount(context: Context): Int =
        read(0) { DuaJournalStore(context).pendingCount() }

    fun lettersReady(context: Context): List<LetterStore.Letter> =
        read(emptyList()) { LetterStore(context).ready() }

    fun lettersSealed(context: Context): List<LetterStore.Letter> =
        read(emptyList()) { LetterStore(context).sealed() }

    fun quranBookmarks(context: Context): List<QuranBookmarkStore.Bookmark> =
        read(emptyList()) { QuranBookmarkStore(context).all() }

    fun hifdhDue(context: Context): List<HifdhLog.Entry> =
        read(emptyList()) { HifdhLog(context).dueNow() }

    fun hifdhTotal(context: Context): Int =
        read(0) { HifdhLog(context).totalMemorised() }

    fun hifdhInSurah(context: Context, surah: Int): Int =
        read(0) { HifdhLog(context).memorisedInSurah(surah) }

    fun financeMonth(context: Context, key: String): FinanceStore.Month? =
        read(null) { FinanceStore(context).month(key) }

    fun financeExpenses(context: Context, key: String): List<FinanceStore.Expense> =
        read(emptyList()) { FinanceStore(context).expenses(key) }

    fun financeSpent(context: Context, key: String): Double =
        read(0.0) { FinanceStore(context).totalSpent(key) }

    fun sadaqahRecent(context: Context, n: Int): List<SadaqahLog.Gift> =
        read(emptyList()) { SadaqahLog(context).recent(n) }

    fun sadaqahTotals(context: Context): Map<String, Double> =
        read(emptyMap()) { SadaqahLog(context).totalByCurrency() }

    fun sadaqahCount30(context: Context): Int =
        read(0) { SadaqahLog(context).countLast30() }
}
