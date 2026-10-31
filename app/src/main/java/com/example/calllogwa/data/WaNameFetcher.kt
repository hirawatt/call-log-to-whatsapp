package com.example.calllogwa.data

import com.example.calllogwa.util.PhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object WaNameFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Cache to prevent duplicate HTTP requests
    private val cache = ConcurrentHashMap<String, String>()

    private val TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE)

    /**
     * Best-effort fetch of WhatsApp Business name from https://wa.me/91XXXXXXXXXX
     * If the account is a WhatsApp Business profile, wa.me page title often contains "Chat with [Name] on WhatsApp".
     * Returns null if it's a regular user or cannot be scraped.
     */
    suspend fun fetchBusinessName(rawNumber: String): String? = withContext(Dispatchers.IO) {
        val waNumber = PhoneUtils.formatForWhatsApp(rawNumber) ?: return@withContext null

        if (cache.containsKey(waNumber)) {
            val cached = cache[waNumber]
            return@withContext if (cached.isNullOrBlank()) null else cached
        }

        try {
            val url = "https://wa.me/$waNumber"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    val matcher = TITLE_PATTERN.matcher(html)
                    if (matcher.find()) {
                        val title = matcher.group(1)?.trim() ?: ""
                        // Check if title has pattern like "Chat with [Name] on WhatsApp" or "Share on WhatsApp"
                        val extractedName = parseNameFromTitle(title)
                        if (!extractedName.isNullOrBlank()) {
                            cache[waNumber] = extractedName
                            return@withContext extractedName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore network errors, best effort
        }

        cache[waNumber] = "" // cache empty result
        return@withContext null
    }

    private fun parseNameFromTitle(title: String): String? {
        // Examples:
        // "Chat on WhatsApp with Swiggy" -> "Swiggy"
        // "Chat with Domino's Pizza on WhatsApp" -> "Domino's Pizza"
        // "Share on WhatsApp" -> null (generic)
        val regexWith = Regex("Chat (?:on WhatsApp with|with) (.*?) (?:on WhatsApp|on WA)", RegexOption.IGNORE_CASE)
        val match = regexWith.find(title)
        if (match != null) {
            val name = match.groupValues[1].trim()
            if (name.isNotBlank() && !name.equals("WhatsApp", ignoreCase = true)) {
                return name
            }
        }
        return null
    }
}
