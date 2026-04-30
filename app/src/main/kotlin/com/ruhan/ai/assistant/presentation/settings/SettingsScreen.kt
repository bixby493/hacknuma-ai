package com.ruhan.ai.assistant.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CyanColor = Color(0xFF00E5FF)
private val DarkBg = Color(0xFF000000)
private val CardBg = Color(0xFF111111)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        TopAppBar(
            title = { Text("Settings", color = CyanColor, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = CyanColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // SECTION 1 — Identity
            SectionHeader("Identity")
            SettingsTextField("Boss ka Naam", uiState.bossName, viewModel::updateBossName)
            SettingsTextField("Wake Word", uiState.wakeWord, viewModel::updateWakeWord)

            // SECTION 2 — API Keys
            SectionHeader("API Keys")

            ApiKeyField(
                label = "Groq API Key",
                value = uiState.groqApiKey,
                onChange = viewModel::updateGroqKey,
                status = uiState.groqKeyStatus,
                onTest = viewModel::testGroqKey,
                link = "https://console.groq.com"
            )

            ApiKeyField(
                label = "Gemini API Key",
                value = uiState.geminiApiKey,
                onChange = viewModel::updateGeminiKey,
                status = uiState.geminiKeyStatus,
                onTest = viewModel::testGeminiKey,
                link = "https://aistudio.google.com/apikey"
            )

            ApiKeyField(
                label = "HuggingFace Key",
                value = uiState.huggingFaceApiKey,
                onChange = viewModel::updateHuggingFaceKey,
                status = uiState.hfKeyStatus,
                onTest = viewModel::testHuggingFaceKey,
                link = "https://huggingface.co/settings/tokens"
            )

            ApiKeyField(
                label = "Tavily API Key",
                value = uiState.tavilyApiKey,
                onChange = viewModel::updateTavilyKey,
                status = uiState.tavilyKeyStatus,
                onTest = viewModel::testTavilyKey,
                link = "https://app.tavily.com"
            )

            // SECTION 3 — Voice
            SectionHeader("Voice")
            SettingsSwitch("Always Listening", uiState.alwaysListening, viewModel::toggleAlwaysListening)

            Text("Voice Speed: ${String.format("%.1f", uiState.voiceSpeed)}x", color = Color.Gray, fontSize = 13.sp)
            Slider(
                value = uiState.voiceSpeed,
                onValueChange = viewModel::updateVoiceSpeed,
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = CyanColor, activeTrackColor = CyanColor)
            )

            Text("Voice Pitch: ${String.format("%.2f", uiState.voicePitch)}", color = Color.Gray, fontSize = 13.sp)
            Slider(
                value = uiState.voicePitch,
                onValueChange = viewModel::updateVoicePitch,
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(thumbColor = CyanColor, activeTrackColor = CyanColor)
            )

            Text("Wake Sensitivity: ${String.format("%.1f", uiState.wakeSensitivity)}", color = Color.Gray, fontSize = 13.sp)
            Slider(
                value = uiState.wakeSensitivity,
                onValueChange = viewModel::updateWakeSensitivity,
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = CyanColor, activeTrackColor = CyanColor)
            )

            OutlinedButton(
                onClick = { viewModel.testVoice() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Voice", color = CyanColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION 4 — Automation
            SectionHeader("Automation")
            SettingsSwitch("Daily Briefing", uiState.dailyBriefingEnabled, viewModel::toggleDailyBriefing)
            SettingsSwitch("Floating Button", uiState.floatingButton, viewModel::toggleFloatingButton)

            // SECTION 5 — Privacy & Security
            SectionHeader("Privacy & Security")
            SettingsSwitch("Biometric Lock", uiState.biometricLockEnabled, viewModel::toggleBiometricLock)
            SettingsSwitch("Fake Crash Screen", uiState.fakeCrashEnabled, viewModel::toggleFakeCrash)
            SettingsSwitch("Break-in Photo", uiState.breakInPhotoEnabled, viewModel::toggleBreakInPhoto)
            SettingsSwitch("Memory Encryption", uiState.memoryEncryption, viewModel::toggleMemoryEncryption)
            SettingsTextField("Emergency Contact", uiState.emergencyContact, viewModel::updateEmergencyContact)

            // SECTION 6 — Premium
            SectionHeader("Premium")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("You are a Premium Boss!", color = CyanColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val features = listOf(
                    "Live Voice Conversation", "Screen Peeler", "Ghost Control",
                    "Deep Research", "Memory System", "Smart Drop Zone",
                    "Workflow Automation", "RAG Oracle", "Hacker Mode",
                    "Wormhole P2P", "Live Location", "Gmail Manager",
                    "Notes Manager", "Premium Lock", "Complete Settings"
                )
                features.forEach { feature ->
                    Text("  $feature", color = Color.Green, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title.uppercase(),
        color = CyanColor,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    @Suppress("DEPRECATION")
    androidx.compose.material3.Divider(color = Color(0xFF222222))
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingsTextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = CyanColor,
            unfocusedBorderColor = Color(0xFF333333),
            cursorColor = CyanColor
        ),
        singleLine = true
    )
}

@Composable
private fun SettingsSwitch(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanColor,
                checkedTrackColor = CyanColor.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    status: KeyTestStatus,
    onTest: () -> Unit,
    link: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label, color = Color.Gray) },
                modifier = Modifier.weight(1f),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CyanColor,
                    unfocusedBorderColor = Color(0xFF333333),
                    cursorColor = CyanColor
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onTest,
                enabled = value.isNotBlank() && status != KeyTestStatus.TESTING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (status) {
                        KeyTestStatus.SUCCESS -> Color.Green
                        KeyTestStatus.FAILED -> Color.Red
                        else -> CyanColor
                    }
                ),
                modifier = Modifier.height(48.dp)
            ) {
                when (status) {
                    KeyTestStatus.TESTING -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    KeyTestStatus.SUCCESS -> Icon(Icons.Default.Check, "Success", tint = Color.White)
                    KeyTestStatus.FAILED -> Icon(Icons.Default.Close, "Failed", tint = Color.White)
                    KeyTestStatus.IDLE -> Text("TEST", color = DarkBg, fontSize = 12.sp)
                }
            }
        }

        Text(
            text = "Get key: $link",
            color = CyanColor.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
