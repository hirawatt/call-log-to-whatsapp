package com.example.calllogwa.util

object PhoneUtils {

    /**
     * Cleans raw input phone string by removing spaces, dashes, brackets, etc.
     */
    fun cleanNumber(raw: String): String {
        return raw.replace(Regex("[^0-9+]"), "")
    }

    /**
     * Formats an Indian mobile phone number for WhatsApp URL (wa.me/{number}).
     * Output format: 91XXXXXXXXXX (without leading '+')
     * Returns null if the number is not a valid 10-digit Indian mobile number or short code.
     */
    fun formatForWhatsApp(raw: String): String? {
        val cleaned = cleanNumber(raw)
        if (cleaned.isEmpty()) return null

        var digitsOnly = if (cleaned.startsWith("+")) cleaned.substring(1) else cleaned

        // Handle case where country code +91 or 91 is present
        if (digitsOnly.startsWith("91") && digitsOnly.length == 12) {
            val mobilePart = digitsOnly.substring(2)
            if (isValid10DigitIndianMobile(mobilePart)) {
                return digitsOnly
            }
        }

        // Handle leading 0 (trunk prefix in India, e.g., 09876543210)
        if (digitsOnly.startsWith("0") && digitsOnly.length == 11) {
            digitsOnly = digitsOnly.substring(1)
        }

        // Standard 10-digit Indian mobile number
        if (digitsOnly.length == 10 && isValid10DigitIndianMobile(digitsOnly)) {
            return "91$digitsOnly"
        }

        return null
    }

    /**
     * Checks if a 10-digit string is a valid Indian mobile number (starts with 6, 7, 8, or 9).
     */
    private fun isValid10DigitIndianMobile(number: String): Boolean {
        if (number.length != 10) return false
        val firstChar = number.firstOrNull() ?: return false
        return firstChar in '6'..'9'
    }

    /**
     * Checks if the given raw phone number can be converted to a valid WhatsApp contact.
     */
    fun isValidIndianMobile(raw: String): Boolean {
        return formatForWhatsApp(raw) != null
    }

    /**
     * Formats a raw number nicely for display (e.g. +91 98765 43210).
     */
    fun formatDisplayNumber(raw: String): String {
        val waNumber = formatForWhatsApp(raw)
        return if (waNumber != null && waNumber.length == 12) {
            "+${waNumber.substring(0, 2)} ${waNumber.substring(2, 7)} ${waNumber.substring(7)}"
        } else {
            raw
        }
    }
}
