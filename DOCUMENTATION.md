# Technical Documentation & Architecture Guide

## System Overview

**Call Log to WA** is a native Android application built in Kotlin. It acts as an enhanced call log viewer designed to bridge phone calls and WhatsApp messaging.

```
┌─────────────────────────────────────────────────────────────┐
│                      Android System                         │
│   ┌────────────────────────┐   ┌────────────────────────┐   │
│   │ CallLog.Calls Provider │   │   ContactsContract     │   │
│   └───────────┬────────────┘   └───────────┬────────────┘   │
└───────────────┼────────────────────────────┼────────────────┘
                │                            │
                ▼                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    CallLogRepository                        │
│   • Reads raw numbers, call types, timestamps, durations    │
│   • Resolves names & photo URIs from Contacts Provider      │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                        PhoneUtils                           │
│   • Validates 10-digit Indian numbers (6,7,8,9)              │
│   • Formats to E.164 (91XXXXXXXXXX)                         │
│   • Filters out landlines & shortcodes                      │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     CallLogViewModel                        │
│   • StateFlow<List<CallEntry>>                              │
│   • Real-time search query filtering                        │
│   • Dispatches async WaNameFetcher coroutines               │
└───────────────┬────────────────────────────┬────────────────┘
                │                            │
                ▼                            ▼
┌──────────────────────────────┐ ┌───────────────────────────┐
│     CallLogAdapter / UI      │ │       WaNameFetcher       │
│ • Material 3 Card Layout     │ │ • Background HTTP GET     │
│ • Color-coded call types     │ │ • Parses <title> tag      │
│ • 1-tap WhatsApp Launch      │ │ • Displays WA Business    │
└───────────────┬──────────────┘ └───────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│                      WhatsApp Application                   │
│   • Intent: android.intent.action.VIEW                      │
│   • URI: https://wa.me/91XXXXXXXXXX                         │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Architecture

### 1. Data Layer

#### `CallEntry.kt`
Immutable data class representing a processed call record.

| Property | Type | Description |
| :--- | :--- | :--- |
| `id` | `Long` | Unique database ID from `CallLog.Calls._ID` |
| `number` | `String` | Raw phone number string from call log |
| `formattedNumber` | `String` | Nicely formatted display number (e.g. `+91 98765 43210`) |
| `waNumber` | `String?` | E.164 formatted number (`91XXXXXXXXXX`), or `null` if invalid |
| `contactName` | `String?` | Resolved contact display name from Android device contacts |
| `contactPhotoUri` | `String?` | Photo URI string if available in device contacts |
| `callType` | `Int` | `CallLog.Calls.INCOMING_TYPE`, `OUTGOING_TYPE`, or `MISSED_TYPE` |
| `date` | `Long` | Epoch timestamp in milliseconds |
| `duration` | `Long` | Call duration in seconds |
| `waBusinessName` | `String?` | Resolved business name from `wa.me` page title (if applicable) |

#### `CallLogRepository.kt`
Responsible for querying system content providers using `ContentResolver`:
1. Queries `CallLog.Calls.CONTENT_URI` sorted by date descending.
2. For each number, performs a lookup on `ContactsContract.PhoneLookup.CONTENT_FILTER_URI`.
3. Caches contact lookups in a local `Map<String, Pair<String?, String?>>` to eliminate redundant SQLite cross-queries.

---

### 2. Utility Layer

#### `PhoneUtils.kt`
Encapsulates Indian mobile number validation and formatting rules:

- **Cleaning**: `cleanNumber(raw)` removes all non-numeric characters except leading `+`.
- **Trunk Code Removal**: Strips leading `0` when length is 11 (e.g. `09876543210` -> `9876543210`).
- **Country Code Standardisation**: Normalises `+919876543210`, `919876543210`, and `9876543210` to `919876543210`.
- **Mobile Prefix Check**: Requires 10 digits starting with `6`, `7`, `8`, or `9`.
- **Exclusions**: Rejects 3-5 digit operator shortcodes (e.g., `198`, `121`, `1901`) and standard landline length patterns.

#### `WhatsAppLauncher.kt`
Handles intent resolution for WhatsApp:
1. Formats raw number using `PhoneUtils.formatForWhatsApp(rawNumber)`.
2. Constructs `https://wa.me/91XXXXXXXXXX`.
3. Attempts launching direct package `com.whatsapp`.
4. If WhatsApp is not installed, attempts `com.whatsapp.w4b` (WhatsApp Business).
5. If neither application is present, opens the system browser chooser.

---

### 3. Business Logic & Scraping Layer

#### `WaNameFetcher.kt`
Background scraper for WhatsApp Business profile titles:
- Uses `OkHttp` with a custom browser `User-Agent` string.
- Executes GET request to `https://wa.me/91XXXXXXXXXX`.
- Uses regex `Pattern.compile("<title>(.*?)</title>")` to extract HTML page title.
- Identifies titles matching `Chat on WhatsApp with [Name]` or `Chat with [Name] on WhatsApp`.
- Caches responses in a thread-safe `ConcurrentHashMap` to avoid repeated network traffic.

---

### 4. UI Layer

#### `CallLogViewModel.kt`
- Exposes `callLogs: StateFlow<List<CallEntry>>` and `isLoading: StateFlow<Boolean>`.
- Supports live search filtering on both `contactName` and `number`.
- Triggers non-blocking background coroutines to fetch WhatsApp Business names without stalling the UI thread.

#### `CallLogAdapter.kt`
- Implements `ListAdapter` with `DiffUtil` for optimized RecyclerView rendering.
- Dynamically generates two-character initials for contact avatars (e.g., `"JD"` for John Doe).
- Displays color-coded call status badges:
  - 🟢 **Green Arrow**: Incoming Call
  - 🔵 **Blue Arrow**: Outgoing Call
  - 🔴 **Red Arrow**: Missed / Rejected Call

---

## Permissions Breakdown

The application requests the following Android runtime permissions:

```xml
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
```

| Permission | Purpose | Mandatory / Optional |
| :--- | :--- | :--- |
| `READ_CALL_LOG` | To access incoming, outgoing, and missed call records | **Mandatory** |
| `READ_CONTACTS` | To resolve phone numbers to names in phonebook | **Mandatory** |
| `INTERNET` | To perform best-effort WA Business profile title lookup | Optional |

---

## Troubleshooting & FAQs

### Q: Why does the WhatsApp button not appear for some calls?
**A**: The WhatsApp button is automatically hidden for non-mobile numbers, such as 3-digit carrier shortcodes (`198`, `121`), customer service IVRs, or landlines, because WhatsApp requires a valid 10-digit mobile number.

### Q: What happens if WhatsApp is not installed on the device?
**A**: The app will automatically attempt to open WhatsApp Business (`com.whatsapp.w4b`). If that is also absent, it opens the web browser to `https://wa.me/{number}`.

### Q: Why are names taken from Phone Contacts instead of scraping `wa.me`?
**A**: `wa.me/{number}` is a redirection URL designed to open the WhatsApp client. WhatsApp does **not** render personal user profile names on `wa.me` pages for privacy and anti-scraping reasons. Resolving names from the device's Contacts Provider ensures 100% accuracy, zero delay, and offline availability.
