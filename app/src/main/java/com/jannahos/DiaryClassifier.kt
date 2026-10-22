package com.jannahos

// On-device classifier. Reads the diary text, returns a tag and a reaction.
// No network. No LLM. Just honest keyword pattern matching on the user's
// own words. The reaction surfaces on the dashboard for the next 24 hours.
object DiaryClassifier {

    data class Result(
        val tag: String,
        val reactionTitle: String,
        val reactionBody: String,
        val source: String,
        val actionRoute: String?,
        val duaRecommendation: String? = null
    )

    fun classify(text: String): Result {
        val t = text.lowercase()

        // ---- Milestones (celebratory) ----

        if (listM(t, listOf("got married", "married today", "nikah", "nikkah", "waleemah", "walima", "my wedding")))
            return Result(
                tag = "milestone_marriage",
                reactionTitle = "You got married",
                reactionBody = "May Allah bless your union. The Prophet  said: \"When a servant of Allah marries, he has completed half of his religion.\"",
                source = "Bayhaqi, Shu'ab al-Iman 5486 (graded hasan by some scholars)",
                actionRoute = null
            )

        if (listM(t, listOf("new baby", "had a baby", "born today", "gave birth", "our son", "our daughter", "expecting")))
            return Result(
                tag = "milestone_child",
                reactionTitle = "A new life",
                reactionBody = "May Allah make them righteous and a source of coolness to your eyes. Allah says: \"Our Lord, grant us from among our spouses and offspring comfort to our eyes.\"",
                source = "Qur'an 25:74",
                actionRoute = null
            )

        if (listM(t, listOf("graduated", "graduation", "passed", "got the job", "got accepted", "promoted")))
            return Result(
                tag = "milestone_achievement",
                reactionTitle = "An achievement",
                reactionBody = "Alhamdulillah. Say \"Alhamdulillah\" and give a small sadaqah in gratitude. Gratitude preserves blessings.",
                source = "Qur'an 14:7",
                actionRoute = null,
                duaRecommendation = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ\n\n(O Allah, help me to remember You, thank You, and worship You in the best manner.)"
            )

        if (listM(t, listOf("finished the quran", "completed quran", "khatm", "memorized", "memorised", "hifdh")))
            return Result(
                tag = "milestone_quran",
                reactionTitle = "Completed",
                reactionBody = "The Prophet  said: \"The best of you are those who learn the Quran and teach it.\" Consider teaching a portion to someone this week.",
                source = "Sahih al-Bukhari 5027",
                actionRoute = null
            )

        // ---- Struggle (compassion + tawbah) ----

        if (listM(t, listOf("i lied", "i told a lie", "lied to", "lying")))
            return Result(
                tag = "struggle_lying",
                reactionTitle = "You mentioned a lie",
                reactionBody = "\"Signs of a hypocrite: when he speaks, he lies.\" (Sahih al-Bukhari 33). Return, correct it if you can, and seek forgiveness. Allah forgives.",
                source = "Sahih al-Bukhari 33",
                actionRoute = "tawbah"
            )

        if (listM(t, listOf("i sinned", "i slipped", "i gave in", "i did it again", "i failed", "i masturbated", "i watched", "i slept with", "i had sex", "zina")))
            return Result(
                tag = "struggle_sin",
                reactionTitle = "You mentioned a slip",
                reactionBody = "\"Say: O My servants who have transgressed against themselves, do not despair of the mercy of Allah.\" Repent. The door is open.",
                source = "Qur'an 39:53",
                actionRoute = "tawbah"
            )

        if (listM(t, listOf("insulted", "cursed", "swore at", "yelled at", "shouted at", "said something bad")))
            return Result(
                tag = "struggle_speech",
                reactionTitle = "About your words",
                reactionBody = "\"Whoever believes in Allah and the Last Day, let him speak good or remain silent.\" (Sahih al-Bukhari 6018). Consider apologising today.",
                source = "Sahih al-Bukhari 6018",
                actionRoute = null
            )

        if (listM(t, listOf("disobeyed my mother", "disobeyed my father", "disobeyed my parents", "yelled at my mum", "yelled at my mom", "yelled at my dad")))
            return Result(
                tag = "struggle_parents",
                reactionTitle = "About your parents",
                reactionBody = "Allah says: \"And lower to them the wing of humility out of mercy.\" (Qur'an 17:24). Go to them today. Apologise. That is worship.",
                source = "Qur'an 17:24",
                actionRoute = null
            )

        if (listM(t, listOf("missed fajr", "missed prayer", "missed salah", "didn't pray", "did not pray")))
            return Result(
                tag = "struggle_prayer",
                reactionTitle = "About prayer",
                reactionBody = "Missing a prayer is not the end. The scholars agree: pray it as soon as you remember. Allah loves the return.",
                source = "Sahih Muslim 684",
                actionRoute = "pray"
            )

        // ---- Good deeds ----

        if (listM(t, listOf("helped", "gave charity", "gave sadaqah", "donated", "volunteered", "visited the sick")))
            return Result(
                tag = "good_deed",
                reactionTitle = "You gave or helped",
                reactionBody = "\"Charity does not decrease wealth.\" (Sahih Muslim 2588). What you did today is recorded and will not be lost.",
                source = "Sahih Muslim 2588",
                actionRoute = null
            )

        if (listM(t, listOf("apologised", "apologized", "said sorry", "made up with", "forgave")))
            return Result(
                tag = "good_deed_reconcile",
                reactionTitle = "You made peace",
                reactionBody = "The Prophet  said: \"The best of the two is the one who initiates the greeting of peace.\" (Jami' at-Tirmidhi 2489). You did that today.",
                source = "Jami' at-Tirmidhi 2489 (graded sahih)",
                actionRoute = null
            )

        // ---- Feeling ----

        if (listM(t, listOf("anxious", "worried", "stressed", "overwhelmed", "scared")))
            return Result(
                tag = "feeling_anxiety",
                reactionTitle = "You feel anxious",
                reactionBody = "\"Allah does not burden a soul beyond what it can bear.\" (Qur'an 2:286). Read it slowly. Make the anxiety dua.",
                source = "Qur'an 2:286",
                actionRoute = "dhikr",
                duaRecommendation = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ\n\n(O Allah, I seek refuge in You from anxiety and sorrow, weakness and laziness.)"
            )

        if (listM(t, listOf("sad", "lonely", "depressed", "down today", "heavy heart", "heartbroken")))
            return Result(
                tag = "feeling_sadness",
                reactionTitle = "You feel low",
                reactionBody = "The Prophet  sought refuge from worry and grief. You are not alone in this. Allah is closer than you think.",
                source = "Sahih al-Bukhari 6369",
                actionRoute = "dhikr",
                duaRecommendation = "اللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ، ابْنُ أَمَتِكَ...\n\n(O Allah, I am Your servant, son of Your servant, son of Your maidservant...)"
            )

        if (listM(t, listOf("grateful", "gratitude", "thankful", "alhamdulillah", "so happy", "blessed")))
            return Result(
                tag = "feeling_gratitude",
                reactionTitle = "You are grateful",
                reactionBody = "\"If you are grateful, I will surely increase you.\" (Qur'an 14:7). Gratitude is the shortest path to more.",
                source = "Qur'an 14:7",
                actionRoute = null,
                duaRecommendation = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ\n\n(O Allah, help me to remember You, thank You, and worship You in the best manner.)"
            )

        // ---- Dream ----

        if (listM(t, listOf("i dreamt", "i had a dream", "dream last night", "nightmare")))
            return Result(
                tag = "dream",
                reactionBody = "Dreams are personal. The Prophet  taught that good dreams are from Allah, and to seek interpretation only from someone trustworthy. We record it privately. Nothing else.",
                reactionTitle = "A dream",
                source = "Sahih al-Bukhari 6984",
                actionRoute = null
            )

        // ---- Question / reflection ----

        if (t.contains("?") || listM(t, listOf("i wonder", "i'm thinking", "im thinking", "i keep asking")))
            return Result(
                tag = "reflection",
                reactionTitle = "A question",
                reactionBody = "You wrote a question. Ask the One who knows. Then act on what is clear and trust Him with the rest.",
                source = "",
                actionRoute = null
            )

        // ---- Default ----

        return Result(
            tag = "general",
            reactionTitle = "",
            reactionBody = "",
            source = "",
            actionRoute = null
        )
    }

    private fun listM(text: String, patterns: List<String>): Boolean =
        patterns.any { text.contains(it) }
}
