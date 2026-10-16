package com.jannahos

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Zakat calculation. Nisab: 85g of gold OR 595g of silver (whichever you follow).
// Rate: 2.5% on wealth held above nisab for one lunar year (hawl).
object Zakat {

    // Nisab thresholds in grams.
    const val NISAB_GOLD_GRAMS = 85.0
    const val NISAB_SILVER_GRAMS = 595.0

    // Zakat rate.
    const val RATE = 0.025

    data class Prices(val goldPerGram: Double, val silverPerGram: Double, val fetched: Boolean)

    // Fetch spot prices per gram from goldprice.org's public JSON endpoint.
    // No key required. Returns null on failure.
    fun fetchPrices(): Prices? {
        return try {
            val conn = URL("https://data-asg.goldprice.org/dbXRates/USD").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "JannahOS/1.0")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val root = JSONObject(body)
            val item = root.getJSONArray("items").getJSONObject(0)
            val goldOz = item.getDouble("xauPrice")     // USD per troy ounce
            val silverOz = item.getDouble("xagPrice")   // USD per troy ounce
            val gramsPerOz = 31.1034768
            Prices(
                goldPerGram = goldOz / gramsPerOz,
                silverPerGram = silverOz / gramsPerOz,
                fetched = true
            )
        } catch (e: Exception) {
            null
        }
    }

    data class Assets(
        val cash: Double,
        val goldGrams: Double,
        val silverGrams: Double,
        val investments: Double,
        val businessInventory: Double,
        val receivables: Double,
        val debtsOwed: Double
    ) {
        val total: Double get() = cash + investments + businessInventory + receivables + debtsOwed
        val net: Double get() = (total - debtsOwed).coerceAtLeast(0.0)
    }

    data class Result(
        val totalAssets: Double,
        val deductibleDebts: Double,
        val net: Double,
        val nisabValue: Double,
        val method: String,
        val zakatDue: Double,
        val eligible: Boolean
    )

    fun calculate(a: Assets, prices: Prices, useSilver: Boolean = true): Result {
        val goldValue = a.goldGrams * prices.goldPerGram
        val silverValue = a.silverGrams * prices.silverPerGram
        val totalWealth = a.total + goldValue + silverValue
        val net = (totalWealth - a.debtsOwed).coerceAtLeast(0.0)

        val nisabValue = if (useSilver)
            NISAB_SILVER_GRAMS * prices.silverPerGram
        else
            NISAB_GOLD_GRAMS * prices.goldPerGram

        val eligible = net >= nisabValue
        val zakat = if (eligible) net * RATE else 0.0

        return Result(
            totalAssets = totalWealth,
            deductibleDebts = a.debtsOwed,
            net = net,
            nisabValue = nisabValue,
            method = if (useSilver) "Silver (595g)" else "Gold (85g)",
            zakatDue = zakat,
            eligible = eligible
        )
    }
}
