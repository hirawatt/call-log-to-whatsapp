package com.example.calllogwa.data

data class CallEntry(
    val id: Long,
    val number: String,
    val formattedNumber: String,
    val waNumber: String?,
    val contactName: String?,
    val contactPhotoUri: String?,
    val callType: Int,
    val date: Long,
    val duration: Long,
    val waBusinessName: String? = null
) {
    /**
     * Display title: contact name if known, else formatted number.
     */
    val displayTitle: String
        get() = contactName?.ifBlank { null } ?: formattedNumber

    val hasWhatsApp: Boolean
        get() = waNumber != null
}
