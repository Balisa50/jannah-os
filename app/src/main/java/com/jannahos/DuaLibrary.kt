package com.jannahos

// A curated library of daily duas, every one with a verified source.
// Grouped by moment. Do not add entries without a hadith reference.
object DuaLibrary {

    data class Dua(
        val id: String,
        val moment: String,
        val title: String,
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val source: String,
        val caveat: String = ""
    )

    val moments = listOf(
        "Waking", "Sleeping", "Eating", "Leaving home",
        "Entering home", "Travel", "Distress", "Illness",
        "Anxiety", "Forgiveness"
    )

    val duas: List<Dua> = listOf(
        // Waking
        Dua(
            id = "wake_1",
            moment = "Waking",
            title = "On waking up",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdulillahi-lladhi ahyana ba'da ma amatana wa ilayhi-n-nushur",
            translation = "Praise be to Allah who gave us life after He had caused us to die, and to Him is the return.",
            source = "Sahih al-Bukhari 6312"
        ),
        // Sleeping
        Dua(
            id = "sleep_1",
            moment = "Sleeping",
            title = "Before sleeping",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya",
            translation = "In Your name, O Allah, I die and I live.",
            source = "Sahih al-Bukhari 6324"
        ),
        Dua(
            id = "sleep_2",
            moment = "Sleeping",
            title = "When turning during the night",
            arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ الْوَاحِدُ الْقَهَّارُ، رَبُّ السَّمَاوَاتِ وَالْأَرْضِ وَمَا بَيْنَهُمَا، الْعَزِيزُ الْغَفَّارُ",
            transliteration = "La ilaha illa-llahu-l-Wahidu-l-Qahhar, Rabbu-s-samawati wa-l-ardi wa ma baynahuma, al-'Azizu-l-Ghaffar",
            translation = "There is no god but Allah, the One, the Prevailing, Lord of the heavens and the earth and all that is between them, the Almighty, the Forgiving.",
            source = "Sunan an-Nasa'i 7672 (graded sahih)"
        ),
        // Eating
        Dua(
            id = "eat_before",
            moment = "Eating",
            title = "Before eating",
            arabic = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah",
            translation = "In the name of Allah.",
            source = "Sahih al-Bukhari 5376",
            caveat = "If forgotten at the start, the Prophet ﷺ taught to say: Bismillahi awwalahu wa akhirahu."
        ),
        Dua(
            id = "eat_after",
            moment = "Eating",
            title = "After eating",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdulillahi-lladhi at'amani hadha wa razaqanihi min ghayri hawlin minni wa la quwwa",
            translation = "Praise be to Allah who fed me this and provided it for me without any strength or power on my part.",
            source = "Sunan Abi Dawud 4023, Jami' at-Tirmidhi 3458 (graded hasan)"
        ),
        // Leaving home
        Dua(
            id = "leave_home",
            moment = "Leaving home",
            title = "Leaving the house",
            arabic = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillah, tawakkaltu 'ala-llah, wa la hawla wa la quwwata illa billah",
            translation = "In the name of Allah, I place my trust in Allah. There is no might and no power except with Allah.",
            source = "Sunan Abi Dawud 5095, Jami' at-Tirmidhi 3426 (graded sahih)"
        ),
        // Entering home
        Dua(
            id = "enter_home",
            moment = "Entering home",
            title = "Entering the house",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ الْمَوْلَجِ وَخَيْرَ الْمَخْرَجِ، بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
            transliteration = "Allahumma inni as'aluka khayra-l-mawliji wa khayra-l-makhraji. Bismillahi walajna, wa bismillahi kharajna, wa 'ala-llahi Rabbina tawakkalna",
            translation = "O Allah, I ask You for the best entering and the best exiting. In the name of Allah we enter, and in the name of Allah we leave, and upon Allah our Lord we place our trust.",
            source = "Sunan Abi Dawud 5096 (graded hasan)"
        ),
        // Travel
        Dua(
            id = "travel_1",
            moment = "Travel",
            title = "Setting out on a journey",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhana-lladhi sakhkhara lana hadha wa ma kunna lahu muqrinin, wa inna ila Rabbina la-munqalibun",
            translation = "Exalted is He who has subjected this to us, and we could not have done it ourselves. And to our Lord we shall surely return.",
            source = "Surah Az-Zukhruf 43:13-14; Sahih Muslim 1342"
        ),
        // Distress
        Dua(
            id = "distress_1",
            moment = "Distress",
            title = "In distress or grief",
            arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ",
            transliteration = "La ilaha illa-llahu-l-'Azimu-l-Halim, la ilaha illa-llahu Rabbu-l-'Arshi-l-'Azim, la ilaha illa-llahu Rabbu-s-samawati wa Rabbu-l-ardi wa Rabbu-l-'Arshi-l-Karim",
            translation = "There is no god but Allah, the Mighty, the Forbearing. There is no god but Allah, Lord of the Mighty Throne. There is no god but Allah, Lord of the heavens and Lord of the earth, and Lord of the Noble Throne.",
            source = "Sahih al-Bukhari 6346, Sahih Muslim 2730"
        ),
        Dua(
            id = "distress_2",
            moment = "Distress",
            title = "Dua of Yunus عليه السلام",
            arabic = "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            transliteration = "La ilaha illa anta subhanaka inni kuntu mina-z-zalimin",
            translation = "There is no god but You. Exalted are You. Indeed, I have been of the wrongdoers.",
            source = "Surah Al-Anbiya 21:87"
        ),
        // Illness
        Dua(
            id = "illness_1",
            moment = "Illness",
            title = "When in pain",
            arabic = "أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ",
            transliteration = "A'udhu bi-llahi wa qudratihi min sharri ma ajidu wa uhadhir",
            translation = "I seek refuge in Allah and His power from the evil of what I feel and fear.",
            source = "Sahih Muslim 2202"
        ),
        Dua(
            id = "illness_2",
            moment = "Illness",
            title = "Dua for the sick",
            arabic = "اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَاسَ، اشْفِ أَنْتَ الشَّافِي، لَا شَافِيَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Rabba-n-nas, adhhibi-l-bas, ishfi anta-sh-Shafi, la shafiya illa anta",
            translation = "O Allah, Lord of mankind, remove the harm. Heal, for You are the Healer. There is no healer except You.",
            source = "Sahih al-Bukhari 5675"
        ),
        // Anxiety
        Dua(
            id = "anxiety_1",
            moment = "Anxiety",
            title = "When overwhelmed",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ وَقَهْرِ الرِّجَالِ",
            transliteration = "Allahumma inni a'udhu bika mina-l-hammi wa-l-hazan, wa-l-'ajzi wa-l-kasal, wa-l-bukhli wa-l-jubn, wa dala'i-d-dayni wa qahri-r-rijal",
            translation = "O Allah, I seek refuge in You from worry and grief, from incapacity and laziness, from miserliness and cowardice, from the burden of debt and from being overpowered by others.",
            source = "Sahih al-Bukhari 6369"
        ),
        // Forgiveness
        Dua(
            id = "forgive_1",
            moment = "Forgiveness",
            title = "Sayyid al-Istighfar — the master of asking forgiveness",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma anta Rabbi la ilaha illa anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika ma-stata'tu. A'udhu bika min sharri ma sana'tu. Abu'u laka bi ni'matika 'alayya, wa abu'u bi dhanbi fa-ghfir li fa-innahu la yaghfiru-dh-dhunuba illa anta",
            translation = "O Allah, You are my Lord. There is no god but You. You created me and I am Your servant. I keep Your covenant and promise as much as I am able. I seek refuge in You from the evil I have done. I acknowledge Your favour upon me, and I acknowledge my sin. Forgive me, for none forgives sins but You.",
            source = "Sahih al-Bukhari 6306",
            caveat = "The Prophet ﷺ said: whoever says this during the day with certainty and dies that day enters Paradise. Whoever says it at night and dies that night enters Paradise."
        ),
        Dua(
            id = "forgive_2",
            moment = "Forgiveness",
            title = "Seeking forgiveness abundantly",
            arabic = "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ، إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ",
            transliteration = "Rabbi-ghfir li wa tub 'alayya, innaka anta-t-Tawwabu-r-Rahim",
            translation = "My Lord, forgive me and accept my repentance. You are the Accepter of repentance, the Merciful.",
            source = "Sunan Abi Dawud 1516, Jami' at-Tirmidhi 3434 (graded sahih)"
        ),
        Dua(
            id = "forgive_3",
            moment = "Forgiveness",
            title = "Dua of Adam عليه السلام",
            arabic = "رَبَّنَا ظَلَمْنَا أَنْفُسَنَا وَإِنْ لَمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            transliteration = "Rabbana zalamna anfusana wa in lam taghfir lana wa tarhamna la-nakunanna mina-l-khasirin",
            translation = "Our Lord, we have wronged ourselves. If You do not forgive us and have mercy on us, we will surely be among the losers.",
            source = "Surah Al-A'raf 7:23"
        ),
        // Additional
        Dua(
            id = "morning_1",
            moment = "Waking",
            title = "Morning remembrance",
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Asbahna wa asbaha-l-mulku lillah, wa-l-hamdu lillah, la ilaha illa-llahu wahdahu la sharika lah",
            translation = "We have entered the morning and the dominion belongs to Allah. All praise is for Allah. There is no god but Allah, alone, without partner.",
            source = "Sahih Muslim 2723"
        ),
        Dua(
            id = "evening_1",
            moment = "Sleeping",
            title = "Evening remembrance",
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Amsayna wa amsa-l-mulku lillah, wa-l-hamdu lillah, la ilaha illa-llahu wahdahu la sharika lah",
            translation = "We have entered the evening and the dominion belongs to Allah. All praise is for Allah. There is no god but Allah, alone, without partner.",
            source = "Sahih Muslim 2723"
        )
    )
}
