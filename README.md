# RUHAN AI — Premium Android AI Assistant

**Version 2.0 PREMIUM** | **Package:** `com.ruhan.ai.assistant` | **Min SDK 26 (Android 8)** | **Target SDK 36 (Android 16)**

> *"Namaste Boss. Main Ruhan hoon — aapka personal AI assistant."*

Ruhan is a Jarvis-like AI assistant for Android with voice control, phone automation, screen analysis, deep research, memory system, and 15 premium features.

---

## Quick Setup

1. Clone and build:
```bash
git clone https://github.com/bixby493/hacknuma-ai.git
cd hacknuma-ai
echo "sdk.dir=/path/to/android/sdk" > local.properties
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

2. Open the app → Settings → Add API Keys:
   - **Groq** (Main AI Brain): [console.groq.com](https://console.groq.com)
   - **Gemini** (Vision + Live Voice): [aistudio.google.com/apikey](https://aistudio.google.com/apikey)
   - **HuggingFace** (TTS): [huggingface.co/settings/tokens](https://huggingface.co/settings/tokens)
   - **Tavily** (Web Search): [app.tavily.com](https://app.tavily.com)

3. Grant permissions when prompted (microphone, contacts, phone, SMS, location)

4. Enable Accessibility Service for Ghost Control:
   - Settings → Accessibility → Ruhan AI Ghost Control → Enable

---

## Voice Commands (50+)

### Phone Calls
| Command | Action |
|---------|--------|
| "Ruhan [name] ko call kar" | Call a contact |
| "Ruhan Bhai ko call kar" | Call by nickname |
| "Utha lo" | Answer incoming call |
| "Kaat do" | Reject incoming call |

### SMS & WhatsApp
| Command | Action |
|---------|--------|
| "Ruhan [name] ko message bhej — [text]" | Send SMS |
| "Ruhan WhatsApp pe [name] ko bhej — [message]" | Send WhatsApp |

### App Control
| Command | Action |
|---------|--------|
| "Ruhan YouTube khol" | Open YouTube |
| "Ruhan WhatsApp khol" | Open WhatsApp |
| "Ruhan camera khol" | Open Camera |
| "Ruhan [any app] khol" | Open any installed app |

### Settings Control
| Command | Action |
|---------|--------|
| "Ruhan WiFi on kar" | Toggle WiFi |
| "Ruhan Bluetooth on kar" | Toggle Bluetooth |
| "Ruhan flashlight on kar" | Toggle flashlight |
| "Ruhan Do Not Disturb on kar" | Toggle DND |
| "Ruhan airplane mode on kar" | Open airplane settings |
| "Ruhan hotspot on kar" | Open hotspot settings |
| "Ruhan dark mode on kar" | Toggle dark mode |
| "Ruhan silent mode on kar" | Set vibrate mode |
| "Ruhan mobile data on kar" | Open data settings |
| "Ruhan NFC on kar" | Open NFC settings |
| "Ruhan location off kar" | Open location settings |
| "Ruhan battery saver on kar" | Open battery saver |

### Display & Volume
| Command | Action |
|---------|--------|
| "Ruhan brightness 50% karo" | Set brightness |
| "Ruhan brightness badha" | Max brightness |
| "Ruhan brightness kam kar" | Low brightness |
| "Ruhan volume 70% karo" | Set volume |
| "Ruhan volume max karo" | Max volume |
| "Ruhan volume mute karo" | Mute volume |

### Screen & Vision
| Command | Action |
|---------|--------|
| "Ruhan yeh kya hai?" | Analyze current screen |
| "Ruhan yeh screen dekh" | Screenshot + Gemini analysis |
| "Ruhan yeh kya likha hai?" | Extract text from screen |

### Phone Info
| Command | Action |
|---------|--------|
| "Ruhan battery kitni hai?" | Check battery level |
| "Ruhan time kya hai?" | Current time |
| "Ruhan date kya hai?" | Current date |
| "Ruhan network kaisa hai?" | Network status + speed |

### Memory System
| Command | Action |
|---------|--------|
| "Ruhan yaad rakho ki [fact]" | Store a memory |
| "Ruhan [name] matlab [real name]" | Set nickname |
| "Ruhan tumhe kya pata hai?" | Show all memories |
| "Mera [X] ka number [Y] hai" | Auto-remember facts |

### Research & Search
| Command | Action |
|---------|--------|
| "Ruhan [topic] search karo" | Web search + summary |
| "Ruhan [topic] par research karo" | Deep research report |
| "Ruhan [topic] kya hai?" | AI-powered answer |

### Notes
| Command | Action |
|---------|--------|
| "Ruhan note karo — [text]" | Save a note |
| "Ruhan mere notes dikha" | Show all notes |

### Location
| Command | Action |
|---------|--------|
| "Ruhan meri location [name] ko bhejo" | Share location |
| "Ruhan [place] kaise jaun?" | Navigate to place |

### Reminders
| Command | Action |
|---------|--------|
| "Ruhan 30 minute baad yaad dila [task]" | Set reminder |
| "Ruhan kal subah yaad dila [task]" | Morning reminder |
| "Ruhan 2 ghante baad yaad dila [task]" | Hour-based reminder |

### Emergency
| Command | Action |
|---------|--------|
| "Ruhan help" | Emergency mode |
| "Ruhan emergency" | SMS + call emergency contact |

### Live Voice
| Command | Action |
|---------|--------|
| "Ruhan live voice shuru karo" | Start Gemini Live conversation |
| Tap live voice button | Real-time audio with Gemini |

### System Diagnostics
| Command | Action |
|---------|--------|
| "Ruhan phone ka health report do" | CPU, RAM, battery, device info |
| "Ruhan WiFi scan karo" | Nearby WiFi networks |
| "Ruhan system diagnostics" | Full system report |

### Email
| Command | Action |
|---------|--------|
| "Ruhan emails check karo" | Check Gmail |
| "Ruhan [name] ko email bhejo" | Send email |

---

## Premium Features (15)

### 1. RUHAN Live Voice
Real-time voice conversation via Gemini Live API WebSocket. Stream audio in and out with zero delay. Interrupt Ruhan mid-sentence.

### 2. Screen Peeler
MediaProjection API captures screen → Gemini Vision analyzes content. Identifies text, UI elements, form fields, and explains what's on screen.

### 3. Ghost Control
AccessibilityService enables Ruhan to interact with other apps — tap buttons, type text, scroll, navigate settings, and automate multi-step actions.

### 4. Deep Research
Tavily API searches 10+ sources → Groq generates comprehensive report with executive summary, key findings, and detailed analysis. Saved to Room DB.

### 5. Memory System
Short-term (last 20 messages), long-term (important facts), nicknames, and preferences. Auto-triggers on "yaad rakho", "matlab", "mera X hai".

### 6. Smart Drop Zone
Share any content to Ruhan from other apps via Android's share sheet — images, PDFs, text, links are automatically analyzed.

### 7. Workflow Automation
WorkManager-based scheduled command chains. Set daily routines like "subah 7 baje weather batao, Gmail check karo, news batao".

### 8. RAG Oracle
Add documents (PDF, text, chat exports) → semantic search finds relevant content → Groq generates context-aware answers.

### 9. Hacker Mode
Network analysis (WiFi scan, signal strength), system diagnostics (CPU, RAM, storage, battery health), app intelligence (permissions, data usage).

### 10. Wormhole
WiFi P2P based peer-to-peer connection for file transfer between Ruhan-enabled devices without internet.

### 11. Live Location
GPS location → Google Maps link → Share via WhatsApp/SMS. Navigation support with ETA.

### 12. Gmail Manager
Send emails via Gmail intent. Email composition with recipient, subject, and body via voice commands.

### 13. Notes Manager
Room DB notes with auto-categorization (work/personal/ideas/shopping). Search, view, and export notes.

### 14. Premium Lock
Biometric authentication (fingerprint/face), PIN with break-in detection (silent selfie on wrong attempts), fake crash screen.

### 15. Complete Settings Control
Voice control for display (brightness, dark mode, screen timeout), sound (volume, ringtone, silent/vibrate), network (WiFi, Bluetooth, NFC, hotspot, data, location), battery (battery saver), and notifications (DND).

---

## Architecture

```
MVVM + Clean Architecture
├── voice/           RuhanSpeechManager, RuhanVoiceEngine, GeminiLiveVoice
├── brain/           RuhanBrain, CommandParser, MemoryManager, WorkflowEngine
├── phone/           SettingsController
├── accessibility/   RuhanAccessibilityService (Ghost Control)
├── screen/          ScreenCapture, ScreenAnalyzer
├── research/        DeepResearch, RagOracle
├── premium/         GmailManager, LocationManager, NotesManager, WormholeManager
├── security/        BiometricManager
├── data/
│   ├── local/       Room DB (conversations, memories, notes, workflows, research, documents)
│   ├── remote/      Groq, Gemini, HuggingFace, Tavily API services
│   └── repository/  AIRepository, PhoneRepository, ConversationRepository
├── di/              Hilt DI modules
├── presentation/    Jetpack Compose UI + ViewModels
├── service/         ForegroundService, WakeWordDetector, Receivers
└── util/            PreferencesManager, ContactsHelper, PhoneController
```

## Tech Stack

- **Language:** 100% Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **DI:** Dagger Hilt
- **Database:** Room (6 tables)
- **Network:** Retrofit + OkHttp
- **Async:** Coroutines + StateFlow
- **Security:** EncryptedSharedPreferences + Biometric
- **Scheduling:** WorkManager + AlarmManager
- **Voice:** Android SpeechRecognizer + TextToSpeech + Gemini Live WebSocket
- **Accessibility:** AccessibilityService for Ghost Control
- **Screen:** MediaProjection API

## Permissions

The app requests permissions as needed:
- `RECORD_AUDIO` — Voice input
- `CALL_PHONE`, `READ_CONTACTS`, `SEND_SMS` — Phone control
- `ACCESS_FINE_LOCATION` — Location sharing
- `CAMERA` — Break-in detection photo
- `FOREGROUND_SERVICE` — Background listening
- `SYSTEM_ALERT_WINDOW` — Floating button
- `WRITE_SETTINGS` — Brightness/display control
- `BLUETOOTH_CONNECT`, `NFC`, `CHANGE_WIFI_STATE` — Hardware control
- `BIND_ACCESSIBILITY_SERVICE` — Ghost Control

## Build

```bash
./gradlew assembleDebug    # Debug APK (~19MB)
./gradlew assembleRelease  # Release APK (requires signing config)
```

---

**Built with Kotlin + Jetpack Compose | MVVM + Clean Architecture | Dagger Hilt**
