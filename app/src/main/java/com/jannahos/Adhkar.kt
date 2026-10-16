package com.jannahos

// Seven authenticated adhkar. Each entry carries its own source, target,
// and the words a voice recognizer would likely match for it.
// Do not add entries without a verified reference.
object Adhkar {

    data class Item(
        val id: String,
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val source: String,
        val target: Int?,      // recommended count, null if open
        val stems: List<String>
    )

    val items: List<Item> = listOf(
        Item(
            id = "istighfar",
            arabic = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullaha wa atubu ilayh",
            translation = "I seek the forgiveness of Allah and turn to Him in repentance.",
            source = "Sahih al-Bukhari 6307, Sahih Muslim 6799",
            target = 100,
            stems = listOf("astaghfir", "astagfir", "istighfar", "forgiveness")
        ),
        Item(
            id = "subhanallah",
            arabic = "سُبْحَانَ اللَّهِ",
            transliteration = "SubhanAllah",
            translation = "Glory be to Allah.",
            source = "Sahih Muslim 596 (recited 33 times after prayer)",
            target = 33,
            stems = listOf("subhan", "subhaan", "subhanallah")
        ),
        Item(
            id = "alhamdulillah",
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "All praise is for Allah.",
            source = "Sahih Muslim 596 (recited 33 times after prayer)",
            target = 33,
            stems = listOf("alhamd", "hamd", "alhamdulillah")
        ),
        Item(
            id = "allahuakbar",
            arabic = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah is the Greatest.",
            source = "Sahih Muslim 596 (recited 34 times after prayer)",
            target = 34,
            stems = listOf("akbar", "allahu akbar", "allahuakbar")
        ),
        Item(
            id = "tahlil",
            arabic = "لَا إِلَهَ إِلَّا اللَّهُ",
            transliteration = "La ilaha illallah",
            translation = "There is no god but Allah.",
            source = "Sahih al-Bukhari 6407, Sahih Muslim 2691",
            target = null,
            stems = listOf("la ilaha", "illallah", "laa ilaaha", "illa allah")
        ),
        Item(
            id = "hawqala",
            arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "La hawla wa la quwwata illa billah",
            translation = "There is no might nor power except with Allah.",
            source = "Sahih al-Bukhari 6384, Sahih Muslim 2704",
            target = null,
            stems = listOf("hawla", "hawqala", "quwwata", "quwata")
        ),
        Item(
            id = "salawat",
            arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ",
            transliteration = "Allahumma salli 'ala Muhammad",
            translation = "O Allah, send blessings upon Muhammad.",
            source = "Qur'an 33:56, Sahih al-Bukhari 3370",
            target = null,
            stems = listOf("salli", "salah", "muhammad", "mohammed", "sallallahu")
        )
    )
}
