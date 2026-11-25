package com.jannahos.siraj.knowledge

/**
 * Fail-closed evidence checks for any future generated Islamic answer.
 *
 * This class does not decide whether a religious interpretation is correct.
 * It only blocks claims when the required source identity or provenance is
 * missing. Scholarly review remains a separate requirement.
 */
object EvidencePolicy {
    fun review(
        category: ClaimCategory,
        evidence: List<EvidenceExcerpt>
    ): EvidenceReview {
        val reasons = mutableListOf<String>()

        if (evidence.isEmpty()) {
            return EvidenceReview(
                EvidenceDecision.BLOCKED,
                listOf("No cited evidence was supplied.")
            )
        }

        evidence.forEachIndexed { index, excerpt ->
            val label = "Evidence ${index + 1}"
            if (excerpt.text.isBlank()) reasons += "$label has no quoted or extracted text."
            if (excerpt.language.isBlank()) reasons += "$label has no language identifier."

            when (val reference = excerpt.reference) {
                is IslamicReference.Quran -> {
                    if (reference.sourceId.isBlank()) reasons += "$label has no Quran source identifier."
                    if (reference.surah !in 1..114 || reference.ayah < 1) {
                        reasons += "$label has an invalid Quran reference."
                    }
                    if (!excerpt.language.equals("ar", ignoreCase = true) &&
                        (reference.translationId.isNullOrBlank() ||
                            reference.translationEdition.isNullOrBlank())
                    ) {
                        reasons += "$label quotes a translation without identifying its translator and edition."
                    }
                }

                is IslamicReference.Hadith -> {
                    if (reference.sourceId.isBlank() ||
                        reference.collection.isBlank() ||
                        reference.referenceNumber.isBlank()
                    ) {
                        reasons += "$label has incomplete hadith collection/reference details."
                    }
                }

                is IslamicReference.ScholarlyWork -> {
                    if (reference.sourceId.isBlank() ||
                        reference.author.isBlank() ||
                        reference.title.isBlank()
                    ) {
                        reasons += "$label has incomplete scholarly work details."
                    }
                }
            }
        }

        when (category) {
            ClaimCategory.GENERAL_INFORMATION -> Unit

            ClaimCategory.QURAN_EXPLANATION -> {
                if (evidence.none { it.reference is IslamicReference.Quran }) {
                    reasons += "A Quran explanation needs an exact Surah and ayah reference."
                }
                if (evidence.none { it.reference is IslamicReference.ScholarlyWork }) {
                    reasons += "Interpretive claims need an identifiable tafsir or scholarly source."
                }
            }

            ClaimCategory.HADITH_AUTHENTICITY -> {
                val hadithSources = evidence.mapNotNull { it.reference as? IslamicReference.Hadith }
                if (hadithSources.isEmpty()) {
                    reasons += "An authenticity claim needs a specific hadith collection and reference."
                } else if (hadithSources.none {
                        !it.grade.isNullOrBlank() && !it.gradingAuthority.isNullOrBlank()
                    }
                ) {
                    reasons += "No cited hadith includes both a grading and the authority that assigned it."
                }
            }

            ClaimCategory.FIQH_RULING -> {
                val scholarlySources = evidence.mapNotNull {
                    it.reference as? IslamicReference.ScholarlyWork
                }
                if (scholarlySources.none {
                        !it.edition.isNullOrBlank() &&
                            !it.volume.isNullOrBlank() &&
                            !it.page.isNullOrBlank()
                    }
                ) {
                    reasons += "A fiqh claim needs a traceable scholarly source with edition, volume and page."
                }

                val hadithSources = evidence.mapNotNull { it.reference as? IslamicReference.Hadith }
                if (hadithSources.any {
                        it.grade.isNullOrBlank() || it.gradingAuthority.isNullOrBlank()
                    }
                ) {
                    reasons += "A hadith used in a ruling has no complete grading provenance."
                }
            }
        }

        return if (reasons.isEmpty()) {
            EvidenceReview(EvidenceDecision.ACCEPTED, emptyList())
        } else {
            EvidenceReview(EvidenceDecision.BLOCKED, reasons.distinct())
        }
    }
}
