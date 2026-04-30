package com.ruhan.ai.assistant.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import com.ruhan.ai.assistant.presentation.theme.RuhanThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = RuhanThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        TopAppBar(
            title = { Text("Settings", color = colors.accent, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colors.accent)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // SECTION 1 — Identity
            SectionHeader("Identity", colors.accent)
            SettingsTextField("Boss ka Naam", uiState.bossName, viewModel::updateBossName, colors)
            SettingsTextField("Wake Word", uiState.wakeWord, viewModel::updateWakeWord, colors)

            // SECTION 2 — External API Endpoints (Command Center style)
            SectionHeader("External API Endpoints", colors.accent)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F0A), RoundedCornerShape(16.dp))
                    .border(1.dp, colors.accent.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ApiKeyField(
                    label = "GEMINI PRO CORE",
                    value = uiState.geminiApiKey,
                    onChange = viewModel::updateGeminiKey,
                    status = uiState.geminiKeyStatus,
                    onTest = viewModel::testGeminiKey,
                    link = "https://aistudio.google.com/apikey",
                    colors = colors
                )

                ApiKeyField(
                    label = "GROQ FAST INFERENCING",
                    value = uiState.groqApiKey,
                    onChange = viewModel::updateGroqKey,
                    status = uiState.groqKeyStatus,
                    onTest = viewModel::testGroqKey,
                    link = "https://console.groq.com",
                    colors = colors
                )

                ApiKeyField(
                    label = "HUGGING FACE VISION",
                    value = uiState.huggingFaceApiKey,
                    onChange = viewModel::updateHuggingFaceKey,
                    status = uiState.hfKeyStatus,
                    onTest = viewModel::testHuggingFaceKey,
                    link = "https://huggingface.co/settings/tokens",
                    colors = colors
                )

                ApiKeyField(
                    label = "TAVILY RESEARCH AGENT",
                    value = uiState.tavilyApiKey,
                    onChange = viewModel::updateTavilyKey,
                    status = uiState.tavilyKeyStatus,
                    onTest = viewModel::testTavilyKey,
                    link = "https://app.tavily.com",
                    colors = colors
                )

                ApiKeyField(
                    label = "NOTION SYNC ENGINE",
                    value = uiState.notionApiKey,
                    onChange = viewModel::updateNotionApiKey,
                    status = uiState.notionKeyStatus,
                    onTest = viewModel::testNotionKey,
                    link = "https://www.notion.so/my-integrations",
                    colors = colors
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "NOTION DATABASE ID",
                        color = colors.textSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = uiState.notionDatabaseId,
                        onValueChange = viewModel::updateNotionDatabaseId,
                        placeholder = { Text("Database ID paste karo...", color = Color(0xFF333833)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accent.copy(alpha = 0.3f),
                            unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.15f),
                            cursorColor = colors.accent
                        ),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF050805), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "[SECURITY NOTICE]: All API keys are encrypted and stored strictly in your local device. RUHAN does not transmit these keys to any centralized server.",
                        color = colors.textSecondary.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            // SECTION 3 — Voice
            SectionHeader("Voice", colors.accent)
            SettingsSwitch("Always Listening", uiState.alwaysListening, viewModel::toggleAlwaysListening, colors)

            // Voice Gender Selection
            Text("Voice Gender", color = colors.textSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val genders = listOf("male" to "Male", "female" to "Female")
                genders.forEach { (value, label) ->
                    val isSelected = uiState.voiceGender == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.card,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.accent else colors.inputBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.updateVoiceGender(value) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (isSelected) colors.accent else colors.textSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Voice Speed: ${String.format("%.1f", uiState.voiceSpeed)}x", color = colors.textSecondary, fontSize = 13.sp)
            Slider(
                value = uiState.voiceSpeed,
                onValueChange = viewModel::updateVoiceSpeed,
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
            )

            Text("Voice Pitch: ${String.format("%.2f", uiState.voicePitch)}", color = colors.textSecondary, fontSize = 13.sp)
            Slider(
                value = uiState.voicePitch,
                onValueChange = viewModel::updateVoicePitch,
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
            )

            Text("Wake Sensitivity: ${String.format("%.1f", uiState.wakeSensitivity)}", color = colors.textSecondary, fontSize = 13.sp)
            Slider(
                value = uiState.wakeSensitivity,
                onValueChange = viewModel::updateWakeSensitivity,
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
            )

            OutlinedButton(
                onClick = { viewModel.testVoice() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Voice", color = colors.accent)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION 4 — Theme
            SectionHeader("Theme", colors.accent)
            Text("Select Theme", color = colors.textSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val themes = listOf(
                    "hacker" to "HACKER",
                    "amoled" to "AMOLED",
                    "dark" to "DARK"
                )
                themes.forEach { (value, label) ->
                    val isSelected = uiState.theme == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.card,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.accent else colors.inputBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.updateTheme(value) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (isSelected) colors.accent else colors.textSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION 5 — Automation
            SectionHeader("Automation", colors.accent)
            SettingsSwitch("Daily Briefing", uiState.dailyBriefingEnabled, viewModel::toggleDailyBriefing, colors)
            SettingsSwitch("Floating Button", uiState.floatingButton, viewModel::toggleFloatingButton, colors)

            // SECTION 6 — Privacy & Security
            SectionHeader("Privacy & Security", colors.accent)
            SettingsSwitch("Biometric Lock", uiState.biometricLockEnabled, viewModel::toggleBiometricLock, colors)
            SettingsSwitch("Fake Crash Screen", uiState.fakeCrashEnabled, viewModel::toggleFakeCrash, colors)
            SettingsSwitch("Break-in Photo", uiState.breakInPhotoEnabled, viewModel::toggleBreakInPhoto, colors)
            SettingsSwitch("Memory Encryption", uiState.memoryEncryption, viewModel::toggleMemoryEncryption, colors)
            SettingsTextField("Emergency Contact", uiState.emergencyContact, viewModel::updateEmergencyContact, colors)

            // SECTION 7 — Premium
            SectionHeader("Premium", colors.accent)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.card, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("You are a Premium Boss!", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
private fun SectionHeader(
    title: String,
    accent: Color
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title.uppercase(),
        color = accent,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF1A3A1A))
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    colors: com.ruhan.ai.assistant.presentation.theme.RuhanColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = colors.textSecondary) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.inputBorder,
            cursorColor = colors.accent
        ),
        singleLine = true
    )
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: com.ruhan.ai.assistant.presentation.theme.RuhanColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textPrimary, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accent,
                checkedTrackColor = colors.accent.copy(alpha = 0.3f)
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
    link: String,
    colors: com.ruhan.ai.assistant.presentation.theme.RuhanColors
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = colors.textSecondary.copy(alpha = 0.7f),
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusDot = when (status) {
                    KeyTestStatus.SUCCESS -> Color.Green
                    KeyTestStatus.FAILED -> Color.Red
                    KeyTestStatus.TESTING -> Color.Yellow
                    KeyTestStatus.IDLE -> if (value.isNotBlank()) Color(0xFF335533) else Color(0xFF333333)
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusDot, CircleShape)
                )
                if (value.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "TEST",
                        color = colors.accent.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable { onTest() }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF050505), RoundedCornerShape(8.dp))
                .border(1.dp, colors.textSecondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                placeholder = { Text("Paste key here...", color = Color(0xFF333833), fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = colors.accent
                ),
                singleLine = true
            )
        }
        Text(
            text = link,
            color = colors.accent.copy(alpha = 0.4f),
            fontSize = 9.sp,
            modifier = Modifier
                .padding(top = 2.dp)
                .clickable {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
        )
    }
}
