package com.jannahos

import android.content.Context

// Every encrypted DB instance is cached so opening a tab does not re-open SQLCipher.
// Thread-safe init. Uses applicationContext so we never leak an Activity.
object Stores {
    @Volatile private var w: WakeLog? = null
    @Volatile private var d: DiaryStore? = null
    @Volatile private var st: StruggleTracker? = null
    @Volatile private var f: FastLog? = null
    @Volatile private var sl: SleepLog? = null
    @Volatile private var dj: DuaJournalStore? = null
    @Volatile private var l: LetterStore? = null
    @Volatile private var h: HifdhLog? = null
    @Volatile private var fin: FinanceStore? = null
    @Volatile private var sd: SadaqahLog? = null
    @Volatile private var bm: QuranBookmarkStore? = null
    @Volatile private var iv: InterventionLog? = null

    fun wake(ctx: Context): WakeLog =
        w ?: synchronized(this) { w ?: WakeLog(ctx.applicationContext).also { w = it } }

    fun diary(ctx: Context): DiaryStore =
        d ?: synchronized(this) { d ?: DiaryStore(ctx.applicationContext).also { d = it } }

    fun struggle(ctx: Context): StruggleTracker =
        st ?: synchronized(this) { st ?: StruggleTracker(ctx.applicationContext).also { st = it } }

    fun fast(ctx: Context): FastLog =
        f ?: synchronized(this) { f ?: FastLog(ctx.applicationContext).also { f = it } }

    fun sleep(ctx: Context): SleepLog =
        sl ?: synchronized(this) { sl ?: SleepLog(ctx.applicationContext).also { sl = it } }

    fun duaJournal(ctx: Context): DuaJournalStore =
        dj ?: synchronized(this) { dj ?: DuaJournalStore(ctx.applicationContext).also { dj = it } }

    fun letters(ctx: Context): LetterStore =
        l ?: synchronized(this) { l ?: LetterStore(ctx.applicationContext).also { l = it } }

    fun hifdh(ctx: Context): HifdhLog =
        h ?: synchronized(this) { h ?: HifdhLog(ctx.applicationContext).also { h = it } }

    fun finance(ctx: Context): FinanceStore =
        fin ?: synchronized(this) { fin ?: FinanceStore(ctx.applicationContext).also { fin = it } }

    fun sadaqah(ctx: Context): SadaqahLog =
        sd ?: synchronized(this) { sd ?: SadaqahLog(ctx.applicationContext).also { sd = it } }

    fun bookmarks(ctx: Context): QuranBookmarkStore =
        bm ?: synchronized(this) { bm ?: QuranBookmarkStore(ctx.applicationContext).also { bm = it } }

    fun intervention(ctx: Context): InterventionLog =
        iv ?: synchronized(this) { iv ?: InterventionLog(ctx.applicationContext).also { iv = it } }
}
