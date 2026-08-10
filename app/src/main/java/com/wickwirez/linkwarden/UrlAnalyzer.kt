package com.wickwirez.linkwarden

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI

enum class Verdict { SAFE, CAUTION, DANGEROUS }

data class AnalysisResult(
    val originalUrl: String,
    val finalUrl: String,
    val hops: List<String>,
    val verdict: Verdict,
    val reasons: List<String>
)

object UrlAnalyzer {

    private val client = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    private val knownShorteners = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd",
        "buff.ly", "adf.ly", "shorte.st", "rebrand.ly", "cutt.ly"
    )

    private val brandTargets = listOf(
        "google.com", "paypal.com", "amazon.com", "microsoft.com",
        "apple.com", "bankofamerica.com", "chase.com", "wellsfargo.com",
        "facebook.com", "instagram.com", "netflix.com"
    )

    fun analyze(inputUrl: String): AnalysisResult {
        val normalized = if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
            "https://$inputUrl"
        } else inputUrl

        val hops = mutableListOf(normalized)
        var current = normalized
        var finalUrl = normalized

        for (i in 0 until 10) {
            var redirected = false
            try {
                val request = Request.Builder().url(current).head().build()
                val response = client.newCall(request).execute()
                val location = response.header("Location")
                val code = response.code
                response.close()
                if (location != null && code in 300..399) {
                    val resolved = URI(current).resolve(location).toString()
                    hops.add(resolved)
                    current = resolved
                    finalUrl = resolved
                    redirected = true
                } else {
                    finalUrl = current
                }
            } catch (e: Exception) {
                finalUrl = current
            }
            if (!redirected) break
        }

        val reasons = mutableListOf<String>()
        var riskScore = 0

        val uri = try { URI(finalUrl) } catch (e: Exception) { null }
        val host = uri?.host?.lowercase() ?: ""

        if (uri?.scheme != "https") {
            reasons.add("Not using HTTPS")
            riskScore += 2
        }

        if (host.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))) {
            reasons.add("Destination is a raw IP address, not a domain")
            riskScore += 3
        }

        if (host.startsWith("xn--") || host.contains(".xn--")) {
            reasons.add("Domain uses punycode encoding (possible homograph attack)")
            riskScore += 3
        }

        val originalHost = try { URI(normalized).host?.lowercase() } catch (e: Exception) { null }
        if (originalHost != null && knownShorteners.contains(originalHost)) {
            reasons.add("Original link is a known URL shortener (destination: $finalUrl)")
            riskScore += 1
        }

        if (host.count { it == '.' } > 3) {
            reasons.add("Unusually deep subdomain structure")
            riskScore += 1
        }

        for (brand in brandTargets) {
            if (host != brand && !host.endsWith(brand)) {
                val distance = levenshtein(host.substringBeforeLast('.'), brand.substringBeforeLast('.'))
                if (distance in 1..2) {
                    reasons.add("Domain closely resembles $brand — possible lookalike")
                    riskScore += 4
                }
            }
        }

        if (hops.size > 4) {
            reasons.add("Long redirect chain (${hops.size} hops)")
            riskScore += 1
        }

        val verdict = when {
            riskScore >= 4 -> Verdict.DANGEROUS
            riskScore >= 1 -> Verdict.CAUTION
            else -> Verdict.SAFE
        }

        if (reasons.isEmpty()) reasons.add("No red flags detected")

        return AnalysisResult(normalized, finalUrl, hops, verdict, reasons)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + minOf(dp[i - 1][j - 1], dp[i - 1][j], dp[i][j - 1])
            }
        }
        return dp[a.length][b.length]
    }
}
