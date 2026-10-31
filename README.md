# 📞 Call Log to WA — Mobile App for Android (Kotlin)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![MinSDK](https://img.shields.io/badge/MinSDK-26%20(Android%208.0)-blue.svg)](https://developer.android.com/about/versions/oreo)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-34%20(Android%2014)-brightgreen.svg)](https://developer.android.com/about/versions/14)

A native Android application designed to access recent call logs, resolve contact names from device contacts, format Indian phone numbers, and provide a **1-tap WhatsApp chat shortcut** for every valid mobile number.

---

## ✨ Features

- 📱 **Recent Call Logs**: View incoming, outgoing, and missed calls with exact date, timestamp, and duration.
- 👤 **Contact Name Resolution**: Integrates with Android's `ContactsContract` to display contact names and circular avatar initials.
- 🇮🇳 **Smart Indian Number Formatting**:
  - Automatically handles 10-digit mobile numbers starting with `6`, `7`, `8`, or `9`.
  - Normalises `+91`, `91`, leading trunk `0`, spaces, and hyphens into E.164 (`91XXXXXXXXXX`).
  - Hides the WhatsApp button for shortcodes (`198`, `121`) and landline numbers.
- 💬 **1-Tap WhatsApp Shortcut**: Tap the WhatsApp button on any call entry to launch WhatsApp chat (`wa.me/91XXXXXXXXXX`) directly.
- 🏷️ **WhatsApp Business Name Fetch**: Best-effort background resolution for WhatsApp Business accounts to display verified business badges (`WA Business: [Name]`).
- 🔍 **Real-time Search**: Search call history instantly by contact name or phone number.
- 🔄 **Pull-to-Refresh & Permissions**: Smooth UX with permission handling for `READ_CALL_LOG` and `READ_CONTACTS`.

---

## 🛠️ Architecture & Tech Stack

- **Gradle**: 8.13 (compatible with Java 17 – 25 / JBR 25)
- **AGP**: 8.7.3
- **Kotlin**: 2.0.21
- **Language**: Kotlin 2.0 / Java 17+, ViewBinding, CardView, RecyclerView, SwipeRefreshLayout
- **Asynchronous Processing**: Kotlin Coroutines & `StateFlow`
- **Network**: OkHttp 4 for background business profile title parsing

---

## 📁 Project Structure

```
call-log-to-wa-apk/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/calllogwa/
│   │   │   │   ├── data/          # CallEntry, CallLogRepository, WaNameFetcher
│   │   │   │   ├── ui/            # MainActivity, CallLogAdapter, CallLogViewModel
│   │   │   │   └── util/          # PhoneUtils, WhatsAppLauncher
│   │   │   └── res/               # Layouts, vector drawables, colors, themes
│   │   └── test/java/com/example/calllogwa/util/PhoneUtilsTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── README.md
└── DOCUMENTATION.md               # Technical architecture & API reference
```

---

## 🚀 How to Export & Install APK

### Method 1: Export APK using Android Studio (Recommended)

1. Open **Android Studio** -> Click **Open** -> Select `/Users/vh/Desktop/call-log-to-wa-apk`.
2. Wait for Gradle Sync to complete.
3. In the top menu bar, go to **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**.
4. Click **locate** in the popup notification to open the compiled APK:  
   `app/build/outputs/apk/debug/app-debug.apk`

### Method 2: Build via Terminal

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=$HOME/Library/Android/sdk

cd /Users/vh/Desktop/call-log-to-wa-apk
./gradlew assembleDebug
```

The APK file will be output to:
`/Users/vh/Desktop/call-log-to-wa-apk/app/build/outputs/apk/debug/app-debug.apk`

### 📲 Installing on Your Phone
1. Transfer `app-debug.apk` to your Android device via USB, Google Drive, or WhatsApp.
2. Tap the `.apk` file on your phone.
3. If prompted by Android security (*Allow from this source*), grant permission and tap **Install**.
4. Open **Call Log to WA** and grant **Call Logs** and **Contacts** permissions!

---

## 🧪 Unit Testing

Run unit tests for Indian phone number parsing and edge case validation:

```bash
./gradlew test
```

Test cases covered in `PhoneUtilsTest.kt`:
- 10-digit Indian numbers starting with 6, 7, 8, 9
- `+91` and `0` trunk prefixes
- Removal of non-numeric formatting (spaces/dashes)
- Rejection of shortcodes and landlines

---

## 📖 Further Documentation

For full architectural breakdown, data flow diagrams, and permission details, read [DOCUMENTATION.md](file:///Users/vh/Desktop/call-log-to-wa-apk/DOCUMENTATION.md).
