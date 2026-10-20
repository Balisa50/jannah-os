package com.jannahos

object CountryMethodMapper {
    // Maps ISO country codes/names to AlAdhan method IDs
    private val methodMap = mapOf(
        "SA" to 4, "Saudi Arabia" to 4, // Umm al-Qura
        "EG" to 5, "Egypt" to 5, // Egyptian General Authority
        "TR" to 13, "Turkey" to 13, // Diyanet
        "AE" to 16, "United Arab Emirates" to 16, // Dubai
        "KW" to 9, "Kuwait" to 9,
        "QA" to 10, "Qatar" to 10,
        "SG" to 11, "Singapore" to 11,
        "MA" to 21, "Morocco" to 21,
        "IQ" to 3, "Iraq" to 3,
        "IR" to 7, "Iran" to 7, // Tehran
        "PK" to 1, "Pakistan" to 1, // Karachi
        "IN" to 1, "India" to 1, // Karachi
        "BD" to 1, "Bangladesh" to 1, // Karachi
        "US" to 2, "United States" to 2, // ISNA
        "CA" to 2, "Canada" to 2, // ISNA
        "GB" to 3, "United Kingdom" to 3, // MWL
        "FR" to 3, "France" to 3, // MWL
        "DE" to 3, "Germany" to 3, // MWL
        "AU" to 3, "Australia" to 3, // MWL
        "MY" to 11, "Malaysia" to 11, // JAKIM (similar)
        "ID" to 20, "Indonesia" to 20 // Kemenag
    )

    fun getMethodForCountry(country: String?): Int {
        if (country == null) return 3 // Default to MWL
        val key = country.uppercase()
        return methodMap[key] ?: methodMap.entries.firstOrNull { key.contains(it.key.uppercase()) }?.value ?: 3
    }
}
