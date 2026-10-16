package com.jannahos

// The morning and evening remembrances the Prophet ﷺ taught.
// Windows: morning between Fajr and sunrise; evening between Asr and Maghrib.
// Every entry has a verified source.
object MorningEveningAdhkar {

    data class Item(
        val id: String,
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val source: String,
        val count: Int
    )

    val morning = listOf(
        Item(
            id = "m_ayat_kursi",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
            transliteration = "Allahu la ilaha illa huwa-l-Hayyu-l-Qayyum...",
            translation = "Allah, there is no deity except Him, the Ever-Living, the Sustainer of existence. (Ayat al-Kursi)",
            source = "Qur'an 2:255. The Prophet ﷺ said whoever recites it in the morning is protected until evening.",
            count = 1
        ),
        Item(
            id = "m_sayyid",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ",
            transliteration = "Allahumma anta Rabbi la ilaha illa anta, khalaqtani wa ana 'abduka...",
            translation = "O Allah, You are my Lord. There is no god but You. You created me and I am Your servant. (Sayyid al-Istighfar)",
            source = "Sahih al-Bukhari 6306",
            count = 1
        ),
        Item(
            id = "m_asbahna",
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Asbahna wa asbaha-l-mulku lillah, wa-l-hamdu lillah...",
            translation = "We have entered the morning and the dominion belongs to Allah. All praise is for Allah.",
            source = "Sahih Muslim 2723",
            count = 1
        ),
        Item(
            id = "m_bismillah_3",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ",
            transliteration = "Bismillahi-lladhi la yadurru ma'asmihi shay'un fi-l-ardi wa la fi-s-sama'...",
            translation = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm.",
            source = "Sunan Abi Dawud 5088, Jami' at-Tirmidhi 3388 (graded sahih)",
            count = 3
        ),
        Item(
            id = "m_hasbi",
            arabic = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ، عَلَيْهِ تَوَكَّلْتُ، وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "Hasbiya-llahu la ilaha illa huwa, 'alayhi tawakkaltu, wa huwa Rabbu-l-'Arshi-l-'Azim",
            translation = "Allah is sufficient for me. There is no god but He. Upon Him I rely, and He is Lord of the Mighty Throne.",
            source = "Sunan Abi Dawud 5081 (graded hasan)",
            count = 7
        )
    )

    val evening = listOf(
        Item(
            id = "e_ayat_kursi",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            transliteration = "Allahu la ilaha illa huwa-l-Hayyu-l-Qayyum...",
            translation = "Allah, there is no deity except Him, the Ever-Living, the Sustainer. (Ayat al-Kursi)",
            source = "Qur'an 2:255. Reciting it in the evening protects until morning.",
            count = 1
        ),
        Item(
            id = "e_sayyid",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ",
            transliteration = "Allahumma anta Rabbi la ilaha illa anta...",
            translation = "O Allah, You are my Lord. There is no god but You. You created me and I am Your servant.",
            source = "Sahih al-Bukhari 6306",
            count = 1
        ),
        Item(
            id = "e_amsayna",
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Amsayna wa amsa-l-mulku lillah...",
            translation = "We have entered the evening and the dominion belongs to Allah. All praise is for Allah.",
            source = "Sahih Muslim 2723",
            count = 1
        ),
        Item(
            id = "e_bismillah_3",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ",
            transliteration = "Bismillahi-lladhi la yadurru ma'asmihi shay'...",
            translation = "In the name of Allah, with whose name nothing can cause harm.",
            source = "Sunan Abi Dawud 5088 (graded sahih)",
            count = 3
        )
    )
}
