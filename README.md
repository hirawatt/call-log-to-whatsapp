<div align="center">

# 📞 Call Log to WhatsApp

### Turn a sales call into a WhatsApp conversation — in one tap.

[![Download APK](https://img.shields.io/badge/Download_APK-v1.0.0-25D366?style=for-the-badge&logo=whatsapp&logoColor=white)](https://github.com/hirawatt/call-log-to-whatsapp/releases/latest)
[![MIT License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)
[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)

**Built for founders, sales teams, and anyone who lives on the phone.**

Review recent calls, find the person you just spoke to, and continue the conversation on WhatsApp — without copying numbers or hunting through chats.

</div>

---

## Why this app?

After a sales call, the fastest way to send a brochure, price list, or follow-up is usually WhatsApp. But getting there is clumsy: open the dialer, copy a number, switch to WhatsApp, search for the contact, and hope you picked the right one.

**Call Log to WhatsApp removes that friction.**

> **Call → open app → tap WhatsApp → send the info.**

It is a focused utility, not another CRM. Your call history and contacts stay on your device.

---

## ✨ Features

| | Feature | What it does |
| :---: | :--- | :--- |
| 📱 | **Recent call log** | Shows incoming, outgoing, and missed calls with time, date, and duration. |
| 👤 | **Contact names** | Resolves saved contact names and shows initial-based avatars. |
| 💬 | **One-tap WhatsApp** | Opens a WhatsApp chat with the number in the correct international format. |
| 🇮🇳 | **Indian number handling** | Normalises `+91`, `91`, `0`, spaces, and dashes; hides WhatsApp for shortcodes and landlines. |
| 🏷️ | **WhatsApp Business detection** | Best-effort lookup of verified WhatsApp Business names from `wa.me`. |
| 🔍 | **Instant search** | Filter call history by contact name or phone number as you type. |
| 🔄 | **Pull to refresh** | Reloads the call log and contact names on demand. |
| 🔐 | **On-device only** | No account, no analytics, no backend, and no uploaded contact lists. |

---

## 📲 Download & install

1. Go to **[Releases](https://github.com/hirawatt/call-log-to-whatsapp/releases/latest)** and download `CallLogToWhatsApp-v1.0.0.apk`.
2. Open the APK on your Android phone.
3. If prompted, allow **Install unknown apps** for your browser or file manager.
4. Open **Call Log to WA** and grant the **Call logs** and **Contacts** permissions.
5. Tap the WhatsApp button next to any mobile number.

**Requirements:** Android 8.0 (API 26) or newer, WhatsApp installed, and a device with call logs.

> [!WARNING]
> This app is distributed as a direct APK, not through Google Play. Android treats `READ_CALL_LOG` as a restricted permission and only allows it for default dialer apps on Play. Sideloading is intentional here.

---

## 🧭 How it works

```text
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Call log    │ ──▶ │  Contact lookup  │ ──▶ │  Number cleanup  │
│  (Android)   │     │  (Contacts)      │     │  (E.164 format)  │
└──────────────┘     └──────────────────┘     └────────┬─────────┘
                                                       │
                                                       ▼
                                              ┌──────────────────┐
                                              │  Tap WhatsApp    │
                                              │  wa.me/91…       │
                                              └──────────────────┘
```

The app reads the most recent calls, resolves contact names locally, and converts valid Indian mobile numbers into the `91XXXXXXXXXX` format WhatsApp expects. Invalid numbers such as shortcodes and landlines are shown without a WhatsApp action.

---

## 🔐 Privacy & permissions

| Permission | Why it is needed |
| :--- | :--- |
| `READ_CALL_LOG` | Read recent incoming, outgoing, and missed calls. |
| `READ_CONTACTS` | Resolve saved contact names and photos. |
| `INTERNET` | Fetch the public `wa.me` page to detect WhatsApp Business names. |

- Call logs and contacts are processed **on your device**.
- There is **no account, analytics SDK, ad SDK, or backend server**.
- Business-name detection sends the phone number to `wa.me` (operated by Meta) in a standard web request. That lookup is best-effort and can be disabled by removing the call in `WaNameFetcher`.
- This project is **not affiliated with, endorsed by, or sponsored by WhatsApp or Meta**.

---

## 🛠️ Build from source

### Requirements

- JDK 17 or newer
- Android SDK 34
- Android Studio or the included Gradle wrapper

```bash
git clone https://github.com/hirawatt/call-log-to-whatsapp.git
cd call-log-to-whatsapp

# Build a debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Build an unsigned release APK
./gradlew assembleRelease
```

The debug APK is written to `app/build/outputs/apk/debug/`. Release builds are unsigned; sign them with your own keystore before distribution.

### Tech stack

| Layer | Choice |
| :--- | :--- |
| Language | Kotlin 2.2.10 |
| UI | Android Views, ViewBinding, Material Components |
| Architecture | MVVM with `StateFlow` |
| Async | Kotlin Coroutines |
| Networking | OkHttp 4 |
| Build | Gradle 9.5.0, AGP 9.3.0 |
| SDK | `minSdk 26`, `targetSdk 34`, `compileSdk 34` |

---

## 📁 Project structure

```text
call-log-to-whatsapp/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/calllogwa/
│       │   │   ├── data/      # CallEntry, CallLogRepository, WaNameFetcher
│       │   │   ├── ui/        # MainActivity, CallLogAdapter, CallLogViewModel
│       │   │   └── util/      # PhoneUtils, WhatsAppLauncher
│       │   └── res/           # Layouts, drawables, themes
│       └── test/              # Phone number parsing tests
├── gradle/                    # Version catalog and wrapper
├── DOCUMENTATION.md           # Architecture and API notes
├── LICENSE
└── README.md
```

More detail on the data flow and components is available in [DOCUMENTATION.md](DOCUMENTATION.md).

---

## 🧪 Tests

The unit tests cover Indian phone-number parsing:

- 10-digit mobile numbers beginning with 6, 7, 8, or 9
- `+91`, `91`, and leading-`0` prefixes
- Spaces, dashes, and other formatting
- Rejection of shortcodes and landline numbers

```bash
./gradlew test
```

---

## 🗺️ Roadmap

- [ ] Message templates for common sales follow-ups
- [ ] Country-aware number formatting beyond India
- [ ] Optional dark theme
- [ ] F-Droid-style reproducible release builds

Have an idea? Open an issue.

---

## 🤝 Contributing

Contributions are welcome. Please keep pull requests focused and include tests for parsing or data-layer changes.

1. Fork the repository.
2. Create a branch: `git checkout -b feat/my-change`.
3. Run `./gradlew test`.
4. Open a pull request describing the change and the problem it solves.

---

## 📄 License

Released under the [MIT License](LICENSE).

Copyright © 2026 Vishal Hirawat.

---

<div align="center">

**If this saves you a few taps after every sales call, consider giving the repo a ⭐.**

Made for people who close deals on WhatsApp.

</div>
