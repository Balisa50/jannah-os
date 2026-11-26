package com.jannahos

import android.content.Context
import android.util.Log

// All encrypted-database reads from UI go through here. If SQLCipher fails
// to open (rare but real on some devices), we return safe fallbacks instead
// of crashing the composition. Failure surfaces as an empty state, not an exit.
object SafeStore {
    private const val TAG = "SafeStore"

    /**
     * Fail-soft reads must not fail-silent. Keep the UI alive, but emit a
     * privacy-conscious diagnostic so an encrypted database failure is
     * discoverable during support and testing. Never log user records/values.
     */
    fun <T> read(default: T, block: () -> T): T =
        try {
            block()
        } catch (error: Exception) {
            Log.e(TAG, "Local data read failed (${error.javaClass.simpleName}); fallback used.")
            default
        }

    fun stats30(context: Context): WakeLog.FormationStats =
        read(WakeLog.FormationStats(0, 0, 0, 0, 0f)) {
            Stores.wake(context).formationStats(30)
        }

    fun stats(context: Context, days: Int): WakeLog.FormationStats =
        read(WakeLog.FormationStats(0, 0, 0, 0, 0f)) {
            Stores.wake(context).formationStats(days)
        }

    fun lastNights(context: Context, n: Int): List<WakeLog.Event> =
        read(emptyList()) { Stores.wake(context).lastNights(n) }

    fun prayedCount(context: Context): Int =
        read(0) { Stores.wake(context).prayedCount() }

    fun latestDiary(context: Context): DiaryStore.Entry? =
        read(null) { Stores.diary(context).latestTagIn24h() }

    fun diaryRecent(context: Context, n: Int): List<DiaryStore.Entry> =
        read(emptyList()) { Stores.diary(context).recent(n) }

    fun struggleHabits(context: Context): List<StruggleTracker.Habit> =
        read(emptyList()) { Stores.struggle(context).habits() }

    fun fastToday(context: Context): String? =
        read(null) { Stores.fast(context).todayKind() }

    fun fastCount30(context: Context): Int =
        read(0) { Stores.fast(context).countLast(30) }

    fun sleepNights(context: Context, n: Int): List<SleepLog.Entry> =
        read(emptyList()) { Stores.sleep(context).lastNights(n) }

    fun sleepAvg(context: Context, n: Int): Float =
        read(0f) { Stores.sleep(context).averageHours(n) }

    fun duaAll(context: Context): List<DuaJournalStore.Dua> =
        read(emptyList()) { Stores.duaJournal(context).all() }

    fun duaAnsweredCount(context: Context): Int =
        read(0) { Stores.duaJournal(context).answeredCount() }

    fun duaPendingCount(context: Context): Int =
        read(0) { Stores.duaJournal(context).pendingCount() }

    fun lettersReady(context: Context): List<LetterStore.Letter> =
        read(emptyList()) { Stores.letters(context).ready() }

    fun lettersSealed(context: Context): List<LetterStore.Letter> =
        read(emptyList()) { Stores.letters(context).sealed() }

    fun quranBookmarks(context: Context): List<QuranBookmarkStore.Bookmark> =
        read(emptyList()) { Stores.bookmarks(context).all() }

    fun hifdhDue(context: Context): List<HifdhLog.Entry> =
        read(emptyList()) { Stores.hifdh(context).dueNow() }

    fun hifdhTotal(context: Context): Int =
        read(0) { Stores.hifdh(context).totalMemorised() }

    fun hifdhInSurah(context: Context, surah: Int): Int =
        read(0) { Stores.hifdh(context).memorisedInSurah(surah) }

    fun financeMonth(context: Context, key: String): FinanceStore.Month? =
        read(null) { Stores.finance(context).month(key) }

    fun financeExpenses(context: Context, key: String): List<FinanceStore.Expense> =
        read(emptyList()) { Stores.finance(context).expenses(key) }

    fun financeSpent(context: Context, key: String): Double =
        read(0.0) { Stores.finance(context).totalSpent(key) }

    fun sadaqahRecent(context: Context, n: Int): List<SadaqahLog.Gift> =
        read(emptyList()) { Stores.sadaqah(context).recent(n) }

    fun sadaqahTotals(context: Context): Map<String, Double> =
        read(emptyMap()) { Stores.sadaqah(context).totalByCurrency() }

    fun interventionStats(context: Context, days: Int): List<InterventionLog.KindStat> =
        read(emptyList()) { Stores.intervention(context).stats(days) }

    fun frictionInsight(context: Context): FrictionMap.Insight? =
        read(null) { FrictionMap.topInsight(context) }

    fun frictionPrayed7(context: Context): Int =
        read(0) { FrictionMap.prayedLast7Days(context) }

    fun frictionMissed7(context: Context): Int =
        read(0) { FrictionMap.missesLast7Days(context) }

    fun sadaqahCount30(context: Context): Int =
        read(0) { Stores.sadaqah(context).countLast30() }
}
