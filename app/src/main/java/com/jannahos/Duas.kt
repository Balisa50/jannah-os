package com.jannahos

object Duas {

    data class Dua(
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val source: String
    )

    val tahajjudOpening = Dua(
        arabic = "اللَّهُمَّ لَكَ الْحَمْدُ أَنْتَ نُورُ السَّمَاوَاتِ وَالْأَرْضِ وَمَنْ فِيهِنَّ",
        transliteration = "Allahumma laka-l-hamd, anta nuru-s-samawati wa-l-ardi wa man fihinna",
        translation = "O Allah, to You belongs all praise. You are the Light of the heavens, the earth, and all they contain.",
        source = "Sahih al-Bukhari 1120, Sahih Muslim 769"
    )

    val lastThird = Dua(
        arabic = "مَنْ يَدْعُونِي فَأَسْتَجِيبَ لَهُ، مَنْ يَسْأَلُنِي فَأُعْطِيَهُ، مَنْ يَسْتَغْفِرُنِي فَأَغْفِرَ لَهُ",
        transliteration = "Man yad'uni fa-astajiba lah, man yas'aluni fa-u'tiyah, man yastaghfiruni fa-aghfirah",
        translation = "Who supplicates to Me, that I may answer? Who asks of Me, that I may give? Who seeks My forgiveness, that I may forgive?",
        source = "Sahih al-Bukhari 1145, Sahih Muslim 758"
    )

    val istighfar = Dua(
        arabic = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
        transliteration = "Astaghfirullaha wa atubu ilayh",
        translation = "I seek the forgiveness of Allah and turn to Him in repentance.",
        source = "Sahih al-Bukhari 6307, Sahih Muslim 6799"
    )
}
