package com.jannahos

import android.content.Context
import java.util.Calendar

object DailyHadith {
    data class Hadith(val english: String, val source: String, val theme: String)

    // We use a small subset of high-quality short hadiths for the dashboard
    private val curatedHadiths = listOf(
        Hadith("Actions are judged by intentions, and every person will have what they intended.", "Sahih al-Bukhari 1", "Intention"),
        Hadith("The most beloved deeds to Allah are those that are consistent, even if they are small.", "Sahih al-Bukhari 6464", "Consistency"),
        Hadith("None of you truly believes until he loves for his brother what he loves for himself.", "Sahih al-Bukhari 13", "Brotherhood"),
        Hadith("The strong believer is better and more beloved to Allah than the weak believer.", "Sahih Muslim 2664", "Strength"),
        Hadith("Whoever believes in Allah and the Last Day, let him speak good or remain silent.", "Sahih al-Bukhari 6018", "Speech"),
        Hadith("Charity does not decrease wealth.", "Sahih Muslim 2588", "Charity"),
        Hadith("The best of you are those who have the best manners and character.", "Sahih al-Bukhari 6029", "Character"),
        Hadith("Do not consider any act of goodness insignificant, even if it is meeting your brother with a cheerful face.", "Sahih Muslim 2626", "Kindness"),
        Hadith("Whoever relieves a believer's distress in this world, Allah will relieve his distress on the Day of Resurrection.", "Sahih Muslim 2699", "Mercy"),
        Hadith("Fear Allah wherever you are, follow a bad deed with a good deed and it will erase it, and treat people with good character.", "Jami' at-Tirmidhi 1987", "Taqwa"),
        Hadith("A believer to another believer is like a building whose different parts enforce each other.", "Sahih al-Bukhari 481", "Unity"),
        Hadith("The most complete of the believers in faith are those with the best character.", "Jami' at-Tirmidhi 1162", "Faith"),
        Hadith("He is not a true believer who eats his fill while his neighbour is hungry.", "Al-Adab Al-Mufrad 112", "Neighbours"),
        Hadith("Allah does not look at your appearance or your wealth, but He looks at your hearts and your deeds.", "Sahih Muslim 2564", "Sincerity"),
        Hadith("Smiling in the face of your brother is charity.", "Jami' at-Tirmidhi 1956", "Smiling"),
        Hadith("The best of people are those who are most beneficial to people.", "Al-Mu'jam Al-Awsat 5937", "Benefit"),
        Hadith("Make things easy and do not make them difficult, give glad tidings and do not repel people.", "Sahih al-Bukhari 69", "Ease"),
        Hadith("Whoever seeks a path in search of knowledge, Allah will make easy for him a path to Paradise.", "Sahih Muslim 2699", "Knowledge"),
        Hadith("The one who is a master of his anger is the strong one, not the one who wrestles others.", "Sahih al-Bukhari 6114", "Anger"),
        Hadith("Be in this world as if you were a stranger or a traveler.", "Sahih al-Bukhari 6416", "Dunya"),
        Hadith("Allah is Beautiful and He loves beauty.", "Sahih Muslim 91", "Beauty")
    )

    fun today(ctx: Context? = null): Hadith {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        
        // Seeded random based on the date so it changes daily but stays constant today
        val seed = (year * 1000 + dayOfYear).toLong()
        val index = kotlin.random.Random(seed).nextInt(0, curatedHadiths.size)
        
        return curatedHadiths[index]
    }
}
