package com.example.calllogwa.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppLauncher {

    /**
     * Opens WhatsApp chat for the specified raw phone number.
     */
    fun openWhatsAppChat(context: Context, rawNumber: String) {
        val waNumber = PhoneUtils.formatForWhatsApp(rawNumber)
        if (waNumber == null) {
            Toast.makeText(context, "Invalid Indian mobile number for WhatsApp", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://wa.me/$waNumber"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            // Set package to WhatsApp if available for faster direct launch
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Try WhatsApp Business package
            val waBusinessIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp.w4b")
            }
            try {
                context.startActivity(waBusinessIntent)
            } catch (e2: Exception) {
                // Fallback to browser or system chooser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(browserIntent)
                } catch (e3: Exception) {
                    Toast.makeText(context, "Could not open WhatsApp link", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
