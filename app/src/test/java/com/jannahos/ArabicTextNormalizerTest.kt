package com.jannahos

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicTextNormalizerTest {
    @Test
    fun removesHarakatAndQuranicAnnotationMarks() {
        assertEquals("بسم الله الرحمن الرحيم", ArabicTextNormalizer.normalize("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"))
    }

    @Test
    fun foldsAlefAndHamzaVariants() {
        assertEquals("امن", ArabicTextNormalizer.normalize("آمَنَ"))
        assertEquals("ان", ArabicTextNormalizer.normalize("إِنَّ"))
    }

    @Test
    fun removesTatweelAndNormalizesWhitespace() {
        assertEquals("الله اكبر", ArabicTextNormalizer.normalize("اللَّــهُ   أَكْبَرُ"))
    }

    @Test
    fun preservesOriginalArabicLetters() {
        assertEquals("محمد", ArabicTextNormalizer.normalize("مُحَمَّدٌ"))
    }
}
