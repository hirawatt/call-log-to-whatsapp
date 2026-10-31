package com.example.calllogwa.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import com.example.calllogwa.util.PhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallLogRepository(private val context: Context) {

    /**
     * Reads the call log entries from ContentResolver and resolves contact names & photos.
     * Returns a list of CallEntry objects sorted by date (newest first).
     */
    suspend fun getCallLogs(limit: Int = 200): List<CallEntry> = withContext(Dispatchers.IO) {
        val callList = mutableListOf<CallEntry>()
        val contentResolver: ContentResolver = context.contentResolver

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC"

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            if (cursor != null && cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val cachedNameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)

                var count = 0
                val contactCache = mutableMapOf<String, Pair<String?, String?>>()

                do {
                    val id = if (idIndex != -1) cursor.getLong(idIndex) else 0L
                    val rawNumber = if (numberIndex != -1) cursor.getString(numberIndex) ?: "" else ""
                    val cachedName = if (cachedNameIndex != -1) cursor.getString(cachedNameIndex) else null
                    val type = if (typeIndex != -1) cursor.getInt(typeIndex) else CallLog.Calls.INCOMING_TYPE
                    val date = if (dateIndex != -1) cursor.getLong(dateIndex) else 0L
                    val duration = if (durationIndex != -1) cursor.getLong(durationIndex) else 0L

                    if (rawNumber.isNotBlank()) {
                        // Resolve contact details from ContactsProvider if cachedName is absent
                        var resolvedName = cachedName
                        var photoUriStr: String? = null

                        if (contactCache.containsKey(rawNumber)) {
                            val cachedData = contactCache[rawNumber]!!
                            resolvedName = cachedData.first ?: resolvedName
                            photoUriStr = cachedData.second
                        } else {
                            val (contactName, photoUri) = resolveContactDetails(contentResolver, rawNumber)
                            if (!contactName.isNullOrBlank()) {
                                resolvedName = contactName
                            }
                            photoUriStr = photoUri
                            contactCache[rawNumber] = Pair(resolvedName, photoUriStr)
                        }

                        val formatted = PhoneUtils.formatDisplayNumber(rawNumber)
                        val waNum = PhoneUtils.formatForWhatsApp(rawNumber)

                        callList.add(
                            CallEntry(
                                id = id,
                                number = rawNumber,
                                formattedNumber = formatted,
                                waNumber = waNum,
                                contactName = resolvedName,
                                contactPhotoUri = photoUriStr,
                                callType = type,
                                date = date,
                                duration = duration
                            )
                        )
                        count++
                    }
                } while (cursor.moveToNext() && count < limit)
            }
        } catch (e: SecurityException) {
            // Permission not granted
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return@withContext callList
    }

    /**
     * Look up contact name and photo URI from ContactsContract for a phone number.
     */
    private fun resolveContactDetails(
        contentResolver: ContentResolver,
        phoneNumber: String
    ): Pair<String?, String?> {
        var name: String? = null
        var photoUri: String? = null
        var cursor: Cursor? = null

        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_URI
            )

            cursor = contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)

                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
                if (photoIndex != -1) {
                    photoUri = cursor.getString(photoIndex)
                }
            }
        } catch (e: Exception) {
            // Fallback silently if contacts permission is missing or lookup fails
        } finally {
            cursor?.close()
        }

        return Pair(name, photoUri)
    }
}
