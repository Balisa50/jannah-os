package com.jannahos

import java.util.Calendar

// A curated set of authentic ahadith for daily reflection.
// Every entry is from an accepted collection with an exact reference.
// No fabricated virtues, no invented rewards.
object DailyHadith {

    data class Hadith(
        val arabic: String,
        val english: String,
        val source: String,
        val theme: String,
        val caveat: String = ""
    )

    val all: List<Hadith> = listOf(
        Hadith(
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            english = "Actions are only by intention, and every person will have only what they intended.",
            source = "Sahih al-Bukhari 1, Sahih Muslim 1907",
            theme = "Intention"
        ),
        Hadith(
            arabic = "أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ",
            english = "The most beloved deeds to Allah are those done consistently, even if they are small.",
            source = "Sahih al-Bukhari 6464, Sahih Muslim 783",
            theme = "Consistency"
        ),
        Hadith(
            arabic = "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
            english = "The Muslim is the one from whose tongue and hand the Muslims are safe.",
            source = "Sahih al-Bukhari 10, Sahih Muslim 40",
            theme = "Character"
        ),
        Hadith(
            arabic = "لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            english = "None of you truly believes until he loves for his brother what he loves for himself.",
            source = "Sahih al-Bukhari 13, Sahih Muslim 45",
            theme = "Faith"
        ),
        Hadith(
            arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            english = "Whoever believes in Allah and the Last Day, let him speak good or remain silent.",
            source = "Sahih al-Bukhari 6018, Sahih Muslim 47",
            theme = "Speech"
        ),
        Hadith(
            arabic = "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ",
            english = "Fear Allah wherever you are. Follow a bad deed with a good one and it will erase it. And treat people with good character.",
            source = "Jami' at-Tirmidhi 1987 (graded hasan)",
            theme = "Repentance"
        ),
        Hadith(
            arabic = "لَا تَغْضَبْ",
            english = "Do not become angry.",
            source = "Sahih al-Bukhari 6114",
            theme = "Anger"
        ),
        Hadith(
            arabic = "الطُّهُورُ شَطْرُ الإِيمَانِ",
            english = "Cleanliness is half of faith.",
            source = "Sahih Muslim 223",
            theme = "Purity"
        ),
        Hadith(
            arabic = "الْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ",
            english = "A good word is charity.",
            source = "Sahih al-Bukhari 2989, Sahih Muslim 1009",
            theme = "Kindness"
        ),
        Hadith(
            arabic = "الدَّالُّ عَلَى الْخَيْرِ كَفَاعِلِهِ",
            english = "The one who guides to good is like the one who does it.",
            source = "Jami' at-Tirmidhi 2670 (graded hasan)",
            theme = "Good deeds"
        ),
        Hadith(
            arabic = "مَنْ لَا يَرْحَمُ لَا يُرْحَمُ",
            english = "Whoever shows no mercy will be shown no mercy.",
            source = "Sahih al-Bukhari 5997, Sahih Muslim 2318",
            theme = "Mercy"
        ),
        Hadith(
            arabic = "إِنَّ اللَّهَ جَمِيلٌ يُحِبُّ الْجَمَالَ",
            english = "Allah is beautiful and He loves beauty.",
            source = "Sahih Muslim 91",
            theme = "Beauty"
        ),
        Hadith(
            arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            english = "The best of you are those who learn the Quran and teach it.",
            source = "Sahih al-Bukhari 5027",
            theme = "Quran"
        ),
        Hadith(
            arabic = "مَنْ صَلَّى الْعِشَاءَ فِي جَمَاعَةٍ فَكَأَنَّمَا قَامَ نِصْفَ اللَّيْلِ",
            english = "Whoever prays Isha in congregation is as if he prayed half the night.",
            source = "Sahih Muslim 656",
            theme = "Prayer"
        ),
        Hadith(
            arabic = "أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ، فَأَكْثِرُوا الدُّعَاءَ",
            english = "The servant is closest to his Lord while in prostration. So make much dua then.",
            source = "Sahih Muslim 482",
            theme = "Dua"
        ),
        Hadith(
            arabic = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
            english = "Your smile in the face of your brother is charity.",
            source = "Jami' at-Tirmidhi 1956 (graded hasan)",
            theme = "Kindness"
        ),
        Hadith(
            arabic = "مَنْ نَفَّسَ عَنْ مُؤْمِنٍ كُرْبَةً مِنْ كُرَبِ الدُّنْيَا نَفَّسَ اللَّهُ عَنْهُ كُرْبَةً مِنْ كُرَبِ يَوْمِ الْقِيَامَةِ",
            english = "Whoever relieves a believer of a hardship in this world, Allah will relieve him of a hardship on the Day of Judgement.",
            source = "Sahih Muslim 2699",
            theme = "Helping others"
        ),
        Hadith(
            arabic = "مَا نَقَصَتْ صَدَقَةٌ مِنْ مَالٍ",
            english = "Charity does not decrease wealth.",
            source = "Sahih Muslim 2588",
            theme = "Charity"
        ),
        Hadith(
            arabic = "الْجَنَّةُ تَحْتَ أَقْدَامِ الأُمَّهَاتِ",
            english = "Paradise is at the feet of mothers.",
            source = "Sunan an-Nasa'i 3104 (graded hasan by some scholars, weak by others)",
            theme = "Parents",
            caveat = "The wording is disputed. The meaning is supported by Qur'an 17:24 and Qur'an 31:14, both of which command kindness to parents."
        ),
        Hadith(
            arabic = "الدُّنْيَا سِجْنُ الْمُؤْمِنِ وَجَنَّةُ الْكَافِرِ",
            english = "The world is a prison for the believer and a paradise for the disbeliever.",
            source = "Sahih Muslim 2956",
            theme = "Perspective"
        ),
        Hadith(
            arabic = "لَنْ يُدْخِلَ أَحَدًا عَمَلُهُ الْجَنَّةَ. قَالُوا: وَلَا أَنْتَ يَا رَسُولَ اللَّهِ؟ قَالَ: وَلَا أَنَا، إِلَّا أَنْ يَتَغَمَّدَنِيَ اللَّهُ بِفَضْلٍ وَرَحْمَةٍ",
            english = "No one enters Paradise by his deeds alone. They said: Not even you, O Messenger of Allah? He said: Not even me, unless Allah envelops me in His grace and mercy.",
            source = "Sahih al-Bukhari 5673, Sahih Muslim 2816",
            theme = "Grace"
        )
    )

    fun today(): Hadith {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return all[dayOfYear % all.size]
    }
}
