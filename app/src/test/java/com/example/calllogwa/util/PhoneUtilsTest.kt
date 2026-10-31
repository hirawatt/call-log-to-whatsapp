package com.example.calllogwa.util

import org.junit.Assert.*
import org.junit.Test

class PhoneUtilsTest {

    @Test
    fun testStandard10DigitIndianMobile() {
        assertEquals("919876543210", PhoneUtils.formatForWhatsApp("9876543210"))
        assertEquals("918765432109", PhoneUtils.formatForWhatsApp("8765432109"))
        assertEquals("917654321098", PhoneUtils.formatForWhatsApp("7654321098"))
        assertEquals("916543210987", PhoneUtils.formatForWhatsApp("6543210987"))
    }

    @Test
    fun testFormattedWithCountryCode() {
        assertEquals("919876543210", PhoneUtils.formatForWhatsApp("+919876543210"))
        assertEquals("919876543210", PhoneUtils.formatForWhatsApp("919876543210"))
        assertEquals("919876543210", PhoneUtils.formatForWhatsApp("+91 98765-43210"))
    }

    @Test
    fun testLeadingZeroTrunkPrefix() {
        assertEquals("919876543210", PhoneUtils.formatForWhatsApp("09876543210"))
    }

    @Test
    fun testInvalidOrShortCodes() {
        assertNull(PhoneUtils.formatForWhatsApp("198"))
        assertNull(PhoneUtils.formatForWhatsApp("121"))
        assertNull(PhoneUtils.formatForWhatsApp("5555"))
        assertNull(PhoneUtils.formatForWhatsApp("1234567890")) // starts with 1
        assertNull(PhoneUtils.formatForWhatsApp("0112345678")) // landline length
    }

    @Test
    fun testIsValidIndianMobile() {
        assertTrue(PhoneUtils.isValidIndianMobile("9876543210"))
        assertTrue(PhoneUtils.isValidIndianMobile("+919876543210"))
        assertFalse(PhoneUtils.isValidIndianMobile("198"))
    }
}
