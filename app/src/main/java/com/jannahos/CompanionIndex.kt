package com.jannahos

// The "living companion". User types a need. On-device rules surface
// verses, duas, and hadith matched to that need. No LLM. No cloud.
// Every entry is sourced.
object CompanionIndex {

    data class Verse(val arabic: String, val english: String, val source: String)
    data class Dua(val arabic: String, val transliteration: String, val english: String, val source: String)
    data class Hadith(val english: String, val source: String)

    data class Need(
        val id: String,
        val keywords: List<String>,
        val title: String,
        val verses: List<Verse>,
        val duas: List<Dua>,
        val ahadith: List<Hadith>,
        val actionLabel: String?,
        val actionRoute: String?
    )

    // Match a free-text need to the best category. Simple substring matching
    // against keywords. Returns null if nothing fits.
    fun match(input: String): Need? {
        val t = input.lowercase()
        var best: Need? = null
        var bestScore = 0
        needs.forEach { n ->
            val score = n.keywords.count { t.contains(it) }
            if (score > bestScore) {
                bestScore = score
                best = n
            }
        }
        return if (bestScore > 0) best else null
    }

    val needs: List<Need> = listOf(
        Need(
            id = "anxious",
            keywords = listOf("anxious", "anxiety", "worried", "worry", "panic", "scared", "fear", "overwhelmed", "stressed", "stress"),
            title = "When anxiety takes hold",
            verses = listOf(
                Verse(
                    "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                    "Verily, in the remembrance of Allah do hearts find rest.",
                    "Qur'an 13:28"
                ),
                Verse(
                    "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
                    "Allah does not burden a soul beyond what it can bear.",
                    "Qur'an 2:286"
                ),
                Verse(
                    "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
                    "And whoever relies upon Allah — then He is sufficient for him.",
                    "Qur'an 65:3"
                )
            ),
            duas = listOf(
                Dua(
                    "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ",
                    "Allahumma inni a'udhu bika mina-l-hammi wa-l-hazan, wa-l-'ajzi wa-l-kasal",
                    "O Allah, I seek refuge in You from worry, grief, incapacity, and laziness.",
                    "Sahih al-Bukhari 6369"
                )
            ),
            ahadith = listOf(
                Hadith(
                    "\"Allah is more joyful at the repentance of His servant than one of you who finds his lost camel in the desert.\"",
                    "Sahih Muslim 2747"
                )
            ),
            actionLabel = "Open morning/evening adhkar",
            actionRoute = "dhikr"
        ),

        Need(
            id = "sad",
            keywords = listOf("sad", "sadness", "depressed", "depression", "down", "lonely", "loneliness", "heartbroken", "crying", "cry", "grief"),
            title = "When the heart is heavy",
            verses = listOf(
                Verse(
                    "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
                    "Indeed, with hardship comes ease.",
                    "Qur'an 94:6"
                ),
                Verse(
                    "وَعَسَىٰ أَن تَكْرَهُوا شَيْئًا وَهُوَ خَيْرٌ لَّكُمْ",
                    "Perhaps you dislike a thing and it is good for you.",
                    "Qur'an 2:216"
                )
            ),
            duas = listOf(
                Dua(
                    "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                    "Hasbuna-llahu wa ni'ma-l-wakil",
                    "Allah is sufficient for us, and He is the best Disposer of affairs.",
                    "Qur'an 3:173"
                )
            ),
            ahadith = listOf(
                Hadith(
                    "\"Wondrous is the affair of the believer. If good befalls him he is grateful, and that is good for him. If hardship befalls him he is patient, and that is good for him.\"",
                    "Sahih Muslim 2999"
                )
            ),
            actionLabel = "Open the diary",
            actionRoute = "diary"
        ),

        Need(
            id = "rizq",
            keywords = listOf("money", "rizq", "poor", "broke", "job", "salary", "debt", "loan", "provision", "work", "unemployed"),
            title = "When rizq is tight",
            verses = listOf(
                Verse(
                    "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
                    "And whoever fears Allah — He will make a way out for him, and provide for him from where he does not expect.",
                    "Qur'an 65:2-3"
                ),
                Verse(
                    "إِنَّ اللَّهَ هُوَ الرَّزَّاقُ ذُو الْقُوَّةِ الْمَتِينُ",
                    "Indeed, it is Allah who is the Provider, the Lord of Strength, the Mighty.",
                    "Qur'an 51:58"
                )
            ),
            duas = listOf(
                Dua(
                    "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ",
                    "Allahumma-kfini bi halalika 'an haramik, wa aghnini bi fadlika 'amman siwak",
                    "O Allah, suffice me with what You have made lawful against what You have forbidden, and free me by Your grace from need of anyone besides You.",
                    "Jami' at-Tirmidhi 3563 (graded hasan)"
                )
            ),
            ahadith = listOf(
                Hadith(
                    "\"Whoever makes istighfar constant, Allah will make for him a way out of every distress, relief from every anxiety, and provision from where he does not expect.\"",
                    "Sunan Abi Dawud 1518 (graded hasan)"
                )
            ),
            actionLabel = "Open the finance screen",
            actionRoute = "finance"
        ),

        Need(
            id = "forgiveness",
            keywords = listOf("sin", "sinned", "guilt", "guilty", "repent", "repentance", "tawbah", "forgive", "forgiveness", "slipped", "failed", "ashamed"),
            title = "When you have fallen",
            verses = listOf(
                Verse(
                    "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ",
                    "Say: O My servants who have transgressed against themselves, do not despair of the mercy of Allah.",
                    "Qur'an 39:53"
                ),
                Verse(
                    "إِنَّ اللَّهَ يُحِبُّ التَّوَّابِينَ وَيُحِبُّ الْمُتَطَهِّرِينَ",
                    "Indeed, Allah loves those who repent and loves those who purify themselves.",
                    "Qur'an 2:222"
                )
            ),
            duas = listOf(
                Dua(
                    "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ إِنَّكَ أَنتَ التَّوَّابُ الرَّحِيمُ",
                    "Rabbi-ghfir li wa tub 'alayya, innaka anta-t-Tawwabu-r-Rahim",
                    "My Lord, forgive me and accept my repentance. You are the Accepter of repentance, the Merciful.",
                    "Sunan Abi Dawud 1516 (graded sahih)"
                )
            ),
            ahadith = listOf(
                Hadith(
                    "\"Allah extends His hand at night so that the one who sinned during the day may repent, and He extends His hand during the day so that the one who sinned at night may repent.\"",
                    "Sahih Muslim 2759"
                )
            ),
            actionLabel = "Open tawbah",
            actionRoute = "tawbah"
        ),

        Need(
            id = "sleep",
            keywords = listOf("sleep", "insomnia", "awake", "tired", "restless", "can't sleep", "cant sleep", "night"),
            title = "When sleep will not come",
            verses = listOf(
                Verse(
                    "وَهُوَ الَّذِي جَعَلَ لَكُمُ اللَّيْلَ لِبَاسًا وَالنَّوْمَ سُبَاتًا",
                    "And it is He who made the night a covering for you, and sleep a rest.",
                    "Qur'an 25:47"
                )
            ),
            duas = listOf(
                Dua(
                    "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                    "Bismika Allahumma amutu wa ahya",
                    "In Your name, O Allah, I die and I live.",
                    "Sahih al-Bukhari 6324"
                )
            ),
            ahadith = listOf(
                Hadith(
                    "Whoever recites Surah Al-Mulk before sleeping is protected from the punishment of the grave.",
                    "Jami' at-Tirmidhi 2891"
                )
            ),
            actionLabel = "Open tonight's plan",
            actionRoute = "bedtime"
        ),

        Need(
            id = "family",
            keywords = listOf("family", "mother", "mothers", "father", "fathers", "parents", "wife", "husband", "spouse", "kids", "children", "son", "daughter"),
            title = "For your family",
            verses = listOf(
                Verse(
                    "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
                    "Our Lord, grant us from among our spouses and offspring comfort to our eyes, and make us leaders of the righteous.",
                    "Qur'an 25:74"
                ),
                Verse(
                    "وَقَضَىٰ رَبُّكَ أَلَّا تَعْبُدُوا إِلَّا إِيَّاهُ وَبِالْوَالِدَيْنِ إِحْسَانًا",
                    "And your Lord has decreed that you worship none but Him, and that you be kind to parents.",
                    "Qur'an 17:23"
                )
            ),
            duas = listOf(
                Dua(
                    "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
                    "Rabbi-rhamhuma kama rabbayani saghira",
                    "My Lord, have mercy on them (my parents) as they raised me when I was small.",
                    "Qur'an 17:24"
                )
            ),
            ahadith = listOf(
                Hadith(
                    "\"The best of you are the best to their families.\"",
                    "Jami' at-Tirmidhi 3895 (graded sahih)"
                )
            ),
            actionLabel = "Open the dua journal",
            actionRoute = "dua_journal"
        ),

        Need(
            id = "gratitude",
            keywords = listOf("grateful", "gratitude", "thankful", "blessed", "happy", "joy", "content", "contentment"),
            title = "In gratitude",
            verses = listOf(
                Verse(
                    "وَإِذْ تَأَذَّنَ رَبُّكُمْ لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
                    "And when your Lord proclaimed: If you are grateful, I will surely increase you.",
                    "Qur'an 14:7"
                )
            ),
            duas = listOf(
                Dua(
                    "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
                    "Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik",
                    "O Allah, help me remember You, thank You, and worship You well.",
                    "Sunan Abi Dawud 1522 (graded sahih)"
                )
            ),
            ahadith = listOf(
                Hadith(
                    "\"Whoever does not thank people has not thanked Allah.\"",
                    "Sunan Abi Dawud 4811 (graded hasan)"
                )
            ),
            actionLabel = null,
            actionRoute = null
        )
    )
}
