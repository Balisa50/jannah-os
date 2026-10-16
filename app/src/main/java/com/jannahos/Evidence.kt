package com.jannahos

// Every religious claim in the app must be traceable to this register.
// Nothing gets shown to the user without an entry here.
// Where scholars differ, we say so. We do not pick a side for the user.

object Evidence {

    data class Source(
        val id: String,
        val arabic: String,
        val transliteration: String,
        val english: String,
        val reference: String,
        val grade: String,
        val caveat: String = ""
    )

    val sources: Map<String, Source> = listOf(

        // --- Quran ---
        Source(
            id = "quran_2_186",
            arabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            transliteration = "Wa idha sa'alaka 'ibadi 'anni fa-inni qarib, ujibu da'wata-d-da'i idha da'an",
            english = "And when My servants ask you concerning Me, indeed I am near. I respond to the invocation of the supplicant when he calls upon Me.",
            reference = "Qur'an 2:186",
            grade = "Qur'an"
        ),
        Source(
            id = "quran_39_53",
            arabic = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا",
            transliteration = "Qul ya 'ibadiya-lladhina asrafu 'ala anfusihim la taqnatu min rahmati-llah, inna-llaha yaghfiru-dh-dhunuba jami'a",
            english = "Say: O My servants who have transgressed against themselves, do not despair of the mercy of Allah. Indeed, Allah forgives all sins.",
            reference = "Qur'an 39:53",
            grade = "Qur'an"
        ),
        Source(
            id = "quran_17_79",
            arabic = "وَمِنَ اللَّيْلِ فَتَهَجَّدْ بِهِ نَافِلَةً لَّكَ عَسَىٰ أَن يَبْعَثَكَ رَبُّكَ مَقَامًا مَّحْمُودًا",
            transliteration = "Wa mina-l-layli fa-tahajjad bihi nafilatan laka 'asa an yab'athaka rabbuka maqaman mahmuda",
            english = "And from the night, arise from sleep for prayer as an additional worship for you; it may be that your Lord will raise you to a praised station.",
            reference = "Qur'an 17:79",
            grade = "Qur'an",
            caveat = "The command here is addressed to the Prophet ﷺ. Scholars differ on whether it is obligatory for him alone or a general recommendation. We present it as a recommendation."
        ),
        Source(
            id = "quran_51_17_18",
            arabic = "كَانُوا قَلِيلًا مِّنَ اللَّيْلِ مَا يَهْجَعُونَ ۝ وَبِالْأَسْحَارِ هُمْ يَسْتَغْفِرُونَ",
            transliteration = "Kanu qalilan mina-l-layli ma yahja'un, wa bi-l-ashari hum yastaghfirun",
            english = "They used to sleep but little of the night, and in the hours before dawn they would seek forgiveness.",
            reference = "Qur'an 51:17-18",
            grade = "Qur'an"
        ),

        // --- Tahajjud ---
        Source(
            id = "bukhari_1145",
            arabic = "يَنْزِلُ رَبُّنَا تَبَارَكَ وَتَعَالَى كُلَّ لَيْلَةٍ إِلَى السَّمَاءِ الدُّنْيَا حِينَ يَبْقَى ثُلُثُ اللَّيْلِ الآخِرُ",
            transliteration = "Yanzilu rabbuna tabaraka wa ta'ala kulla laylatin ila-s-sama'i-d-dunya hina yabqa thuluthu-l-layli-l-akhir",
            english = "Our Lord, Blessed and Most High, descends every night to the lowest heaven when the last third of the night remains.",
            reference = "Sahih al-Bukhari 1145, Sahih Muslim 758",
            grade = "Sahih (authentic)",
            caveat = "The descent is affirmed in the text. Scholars differ on the modality, how it occurs, but agree it is real. The app does not attempt to explain this."
        ),
        Source(
            id = "muslim_769_full",
            arabic = "اللَّهُمَّ لَكَ الْحَمْدُ أَنْتَ نُورُ السَّمَاوَاتِ وَالْأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الْحَمْدُ أَنْتَ قَيِّمُ السَّمَاوَاتِ وَالْأَرْضِ وَمَنْ فِيهِنَّ",
            transliteration = "Allahumma laka-l-hamd, anta nuru-s-samawati wa-l-ardi wa man fihinna, wa laka-l-hamd, anta qayyimu-s-samawati wa-l-ardi wa man fihinna",
            english = "O Allah, to You belongs all praise. You are the Light of the heavens, the earth, and all they contain. To You belongs all praise. You are the Sustainer of the heavens, the earth, and all they contain.",
            reference = "Sahih al-Bukhari 1120, Sahih Muslim 769",
            grade = "Sahih (authentic)"
        ),
        Source(
            id = "tirmidhi_2891_mulk",
            arabic = "مَنْ قَرَأَ تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ كُلَّ لَيْلَةٍ مَنَعَهُ اللَّهُ مِنْ عَذَابِ الْقَبْرِ",
            transliteration = "Man qara'a tabaraka-lladhi bi yadihi-l-mulku kulla laylatin mana'ahu-llahu min 'adhabi-l-qabr",
            english = "Whoever recites Surah Al-Mulk every night, Allah will protect him from the punishment of the grave.",
            reference = "Jami' at-Tirmidhi 2891",
            grade = "Hasan (graded hasan by at-Tirmidhi and others). Some scholars weaken it.",
            caveat = "Grade differs among scholars. Presented as a recommended practice, not an obligation. Do not fear if you miss it."
        ),
        Source(
            id = "bukhari_6307",
            arabic = "وَاللَّهِ إِنِّي لأَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ فِي الْيَوْمِ أَكْثَرَ مِنْ سَبْعِينَ مَرَّةً",
            transliteration = "Wallahi inni la-astaghfiru-llaha wa atubu ilayhi fi-l-yawmi akthara min sab'ina marra",
            english = "By Allah, I seek the forgiveness of Allah and turn to Him in repentance more than seventy times a day.",
            reference = "Sahih al-Bukhari 6307",
            grade = "Sahih (authentic)"
        ),
        Source(
            id = "muslim_2747_joy",
            arabic = "لَلَّهُ أَفْرَحُ بِتَوْبَةِ عَبْدِهِ حِينَ يَتُوبُ إِلَيْهِ مِنْ أَحَدِكُمْ يَجِدُ ضَالَّتَهُ بِالْفَلَاةِ",
            transliteration = "La-llahu afrahu bi-tawbati 'abdihi hina yatubu ilayhi min ahadikum yajidu dallatahu bi-l-falah",
            english = "Allah is more joyful at the repentance of His servant than any of you who finds his lost camel in the desert.",
            reference = "Sahih Muslim 2747",
            grade = "Sahih (authentic)"
        ),
        Source(
            id = "muslim_2759_night_hand",
            arabic = "إِنَّ اللَّهَ يَبْسُطُ يَدَهُ بِاللَّيْلِ لِيَتُوبَ مُسِيءُ النَّهَارِ، وَيَبْسُطُ يَدَهُ بِالنَّهَارِ لِيَتُوبَ مُسِيءُ اللَّيْلِ",
            transliteration = "Inna-llaha yabsutu yadahu bi-l-layli li-yatuba musi'u-n-nahar, wa yabsutu yadahu bi-n-nahari li-yatuba musi'u-l-layl",
            english = "Allah extends His hand at night so that the one who sinned during the day may repent, and He extends His hand during the day so that the one who sinned at night may repent.",
            reference = "Sahih Muslim 2759",
            grade = "Sahih (authentic)"
        ),
        Source(
            id = "tirmidhi_2499_repent",
            arabic = "كُلُّ ابْنِ آدَمَ خَطَّاءٌ، وَخَيْرُ الْخَطَّائِينَ التَّوَّابُونَ",
            transliteration = "Kullu-bni Adama khatta', wa khayru-l-khatta'ina-t-tawwabun",
            english = "Every son of Adam sins, and the best of those who sin are those who repent.",
            reference = "Sunan at-Tirmidhi 2499, Sunan Ibn Majah 4251",
            grade = "Hasan (graded hasan by al-Albani)",
            caveat = "The wording differs slightly across collections. The meaning is agreed upon."
        ),
        Source(
            id = "bukhari_1120_ibn_abbas",
            arabic = "اللَّهُمَّ لَكَ الْحَمْدُ أَنْتَ نُورُ السَّمَاوَاتِ وَالْأَرْضِ",
            transliteration = "Allahumma laka-l-hamd, anta nuru-s-samawati wa-l-ard",
            english = "O Allah, to You belongs all praise. You are the Light of the heavens and the earth.",
            reference = "Sahih al-Bukhari 1120",
            grade = "Sahih (authentic)",
            caveat = "Shortened excerpt from a longer supplication. The full version continues with many praises."
        ),
        Source(
            id = "istighfar_short",
            arabic = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullaha wa atubu ilayh",
            english = "I seek the forgiveness of Allah and turn to Him in repentance.",
            reference = "Sahih al-Bukhari 6307",
            grade = "Sahih (authentic)"
        )
    ).associateBy { it.id }

    fun get(id: String): Source? = sources[id]
}
