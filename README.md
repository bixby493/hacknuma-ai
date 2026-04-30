# RUHAN AI — Your Personal Jarvis-like Android Assistant

> **"Han Boss, bolo."**

Ruhan AI is an intelligent, voice-controlled personal AI assistant for Android — inspired by Jarvis. It speaks **Hinglish** (Hindi + English mix), controls your phone via voice commands, and uses cutting-edge AI models for conversation, vision, and speech.

**Version:** 1.0.0  
**Min SDK:** 26 (Android 8)  
**Target SDK:** 34  
**Language:** Kotlin 100%  
**Architecture:** MVVM + Clean Architecture  
**UI:** Jetpack Compose + Material Design 3

---

## Features

- **AI-Powered Conversations** — Powered by Groq (LLaMA 3.3 70B) for fast, intelligent responses
- **Screen Analysis** — Uses Gemini 1.5 Flash to understand and describe what's on your screen
- **Natural Voice** — HuggingFace TTS for Hindi/Hinglish voice output, with Android TTS fallback
- **Phone Control** — Make calls, send SMS, open apps, control settings — all by voice
- **Wake Word Detection** — Say "Hello Ruhan" or "Ruhan sun" to activate
- **Floating Button** — Always-accessible orb that floats over other apps
- **Smart Reminders** — Set reminders with natural language
- **Emergency Mode** — Instantly contact your emergency number
- **Offline Mode** — Basic commands work without internet
- **Conversation Memory** — Remembers your recent conversation context
- **Beautiful UI** — Dark AMOLED theme with animated glowing orb

---

## Voice Commands

### Phone Calls
| Command | Action |
|---------|--------|
| `"Ruhan [name] ko call kar"` | Find contact and call |
| `"Ruhan phone kar [name] ko"` | Same as above |

### SMS Messages
| Command | Action |
|---------|--------|
| `"Ruhan [name] ko message bhej — [text]"` | Send SMS to contact |
| `"Ruhan [name] ko text bhej — [text]"` | Same as above |

### WhatsApp
| Command | Action |
|---------|--------|
| `"Ruhan WhatsApp pe [name] ko bhej — [message]"` | Open WhatsApp with pre-filled message |

### App Control
| Command | Action |
|---------|--------|
| `"Ruhan YouTube khol"` | Open YouTube |
| `"Ruhan WhatsApp khol"` | Open WhatsApp |
| `"Ruhan camera khol"` | Open Camera |
| `"Ruhan [any app name] khol"` | Open any installed app |

### Phone Settings
| Command | Action |
|---------|--------|
| `"Ruhan WiFi on/off kar"` | Toggle WiFi |
| `"Ruhan Bluetooth on/off kar"` | Toggle Bluetooth |
| `"Ruhan brightness badha/kam kar"` | Adjust brightness |
| `"Ruhan volume badha/kam kar"` | Adjust volume |
| `"Ruhan Do Not Disturb on/off kar"` | Toggle DND |
| `"Ruhan flashlight on/off kar"` | Toggle flashlight/torch |
| `"Ruhan airplane mode on kar"` | Open airplane mode settings |

### Phone Info
| Command | Action |
|---------|--------|
| `"Ruhan battery kitni hai?"` | Check battery level |
| `"Ruhan time kya hai?"` | Get current time |
| `"Ruhan aaj ka date kya hai?"` | Get current date |
| `"Ruhan network kaisa hai?"` | Check network/WiFi status |

### Screen Reading
| Command | Action |
|---------|--------|
| `"Ruhan yeh kya hai?"` | Capture and analyze current screen |
| `"Ruhan screen dekh"` | Same as above |

### Reminders
| Command | Action |
|---------|--------|
| `"Ruhan kal subah 8 baje yaad dila [task]"` | Set reminder for 8 AM |
| `"Ruhan remind kar [task]"` | Set reminder (1 hour default) |

### Web Search
| Command | Action |
|---------|--------|
| `"Ruhan [topic] search kar"` | Search the web (uses Tavily API) |
| `"Ruhan latest news"` | Get latest news |

### Emergency
| Command | Action |
|---------|--------|
| `"Ruhan help"` | Send emergency SMS + call emergency contact |
| `"Ruhan emergency"` | Same as above |

### General Conversation
| Command | Action |
|---------|--------|
| Any question or conversation | Ruhan responds using Groq AI |
| `"Maine pehle kya kaha tha?"` | Shows conversation history |

---

## API Key Setup

Ruhan AI requires API keys to function. All keys are stored securely using **EncryptedSharedPreferences**.

### 1. Groq API Key (Required — Main AI Brain)
- **Model:** llama-3.3-70b-versatile
- **Get your key:** [https://console.groq.com](https://console.groq.com)
- Sign up → Create API Key → Copy and paste in Ruhan Settings

### 2. Gemini API Key (Recommended — Vision & Screen Analysis)
- **Model:** gemini-1.5-flash
- **Get your key:** [https://aistudio.google.com/apikey](https://aistudio.google.com/apikey)
- Sign in with Google → Create API Key → Copy and paste in Ruhan Settings

### 3. HuggingFace API Token (Optional — Natural Hindi Voice)
- **Model:** facebook/mms-tts-hin
- **Get your token:** [https://huggingface.co/settings/tokens](https://huggingface.co/settings/tokens)
- Sign up → Settings → Access Tokens → New Token → Copy and paste
- **Note:** If not provided, Ruhan will use Android's built-in TextToSpeech

### 4. Tavily API Key (Optional — Web Search)
- **Get your key:** [https://app.tavily.com](https://app.tavily.com)
- Sign up → Get API Key → Copy and paste in Ruhan Settings
- **Note:** If not provided, Ruhan will use Groq AI to answer questions from its knowledge

---

## Permissions Guide

Ruhan AI requests the following permissions to function fully:

| Permission | Purpose |
|-----------|---------|
| **RECORD_AUDIO** | Voice input and wake word detection |
| **CALL_PHONE** | Making phone calls via voice command |
| **READ_CONTACTS** | Finding contacts by name |
| **SEND_SMS** | Sending text messages |
| **READ_PHONE_STATE** | Detecting incoming calls |
| **ANSWER_PHONE_CALLS** | Answering/rejecting calls via voice |
| **POST_NOTIFICATIONS** | Reminders and foreground service |
| **FOREGROUND_SERVICE** | Background listening and monitoring |
| **SYSTEM_ALERT_WINDOW** | Floating Ruhan button overlay |
| **WRITE_SETTINGS** | Brightness control |
| **BLUETOOTH_CONNECT** | Bluetooth toggle |
| **ACCESS_FINE_LOCATION** | Emergency location sharing |
| **CAMERA** | Camera access for screen capture |
| **INTERNET** | API communication |

**Note:** You can deny any permission — Ruhan will work with reduced functionality and inform you when a feature requires a missing permission.

---

## How to Enable Always-On Mode

1. Open Ruhan AI → Settings (gear icon)
2. Toggle **"Always Listening"** ON
3. Grant **Microphone** and **Notification** permissions when prompted
4. Ruhan will start a foreground service with a persistent notification
5. Say **"Hello Ruhan"** or **"Ruhan sun"** from any screen to activate
6. The service automatically restarts on device reboot

### Floating Button
1. Settings → Toggle **"Floating Button"** ON
2. Grant **"Display over other apps"** permission when prompted
3. A small glowing orb will appear on your screen
4. **Tap** to open Ruhan, **drag** to move it around

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 100% |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Dagger Hilt |
| Async | Coroutines + StateFlow |
| Database | Room (conversation history) |
| Settings | EncryptedSharedPreferences |
| Networking | Retrofit + OkHttp |
| Scheduling | AlarmManager |
| Voice Input | Android SpeechRecognizer |
| Voice Output | HuggingFace TTS + Android TTS fallback |
| Navigation | Navigation Compose |
| Permissions | Accompanist Permissions |

---

## Project Structure

```
app/src/main/kotlin/com/ruhan/ai/assistant/
├── RuhanApp.kt                          # Application class (Hilt + Notification channels)
├── MainActivity.kt                      # Entry point with Navigation Compose
├── di/
│   ├── AppModule.kt                     # Room, Preferences DI
│   └── NetworkModule.kt                 # Retrofit instances for 4 APIs
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt              # Room database
│   │   ├── ConversationDao.kt          # DAO for chat history
│   │   └── ConversationEntity.kt       # Message data class
│   ├── remote/
│   │   ├── GroqApiService.kt           # Groq LLaMA API
│   │   ├── GeminiApiService.kt         # Google Gemini API
│   │   ├── HuggingFaceApiService.kt    # HF TTS API
│   │   └── TavilyApiService.kt         # Tavily Search API
│   └── repository/
│       ├── AIRepository.kt             # AI logic (chat, vision, search)
│       ├── PhoneRepository.kt          # Phone actions + reminders
│       └── ConversationRepository.kt   # Chat persistence
├── domain/usecase/
│   ├── ProcessCommandUseCase.kt        # Main command parser + router
│   ├── MakeCallUseCase.kt              # Call a contact
│   ├── SendSmsUseCase.kt               # Send SMS
│   ├── ControlSettingsUseCase.kt       # Phone settings control
│   └── AnalyzeScreenUseCase.kt         # Screen capture + Gemini analysis
├── presentation/
│   ├── main/
│   │   ├── RuhanScreen.kt              # Main UI with orb, chat, controls
│   │   └── RuhanViewModel.kt           # Main screen state management
│   ├── settings/
│   │   ├── SettingsScreen.kt           # Full settings UI
│   │   └── SettingsViewModel.kt        # Settings state management
│   └── components/
│       ├── RuhanOrb.kt                 # Animated glowing orb
│       ├── ConversationBubble.kt       # Chat message bubbles
│       ├── WaveformVisualizer.kt       # Audio waveform animation
│       └── FloatingRuhanButton.kt      # Floating overlay button
├── service/
│   ├── RuhanForegroundService.kt       # Background service + wake word
│   ├── FloatingButtonService.kt        # Floating button overlay
│   ├── WakeWordDetector.kt             # Wake word detection engine
│   ├── CallMonitorReceiver.kt          # Incoming call detection
│   ├── BootReceiver.kt                 # Auto-start on boot
│   └── ReminderReceiver.kt            # Reminder notifications
└── util/
    ├── PreferencesManager.kt           # Encrypted settings + API keys
    ├── VoiceManager.kt                 # TTS engine (HF + Android fallback)
    ├── ContactsHelper.kt              # Contact search
    ├── PhoneController.kt             # Phone hardware control
    └── ScreenshotHelper.kt            # Screen capture via PixelCopy
```

---

## Build & Install

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Build
```bash
# Clone the repo
git clone <repo-url>
cd ruhan-ai

# Build debug APK
./gradlew assembleDebug

# APK location
ls app/build/outputs/apk/debug/app-debug.apk
```

### Install on device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## First Launch

1. Install and open Ruhan AI
2. Ruhan greets you: *"Namaste Boss. Main Ruhan hoon — aapka personal AI assistant."*
3. Go to **Settings** → Enter your **Groq API Key** (minimum required)
4. Optionally add Gemini, HuggingFace, and Tavily keys
5. Tap the **Test** button next to each key to verify
6. Return to main screen and start talking!

---

## Settings

| Setting | Description | Default |
|---------|-------------|---------|
| Boss ka Naam | What Ruhan calls you | "Boss" |
| Wake Word | Custom activation phrase | "hello ruhan" |
| Always Listening | Background wake word detection | Off |
| Floating Button | Overlay button on all apps | Off |
| Voice Speed | TTS speed (0.5x – 2.0x) | 1.0x |
| Emergency Contact | Phone number for emergencies | Not set |
| Language | Hinglish / Hindi / English | Hinglish |
| Theme | AMOLED / Dark / Light | AMOLED |

---

## License

MIT License — see [LICENSE](LICENSE) for details.
