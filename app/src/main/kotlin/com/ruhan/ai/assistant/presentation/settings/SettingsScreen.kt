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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhan.ai.assistant.presentation.theme.RuhanThemeColors

private val hackerGreen = Color(0xFF00FF41)
private val cardBg = Color(0xFF0A0F0A)
private val borderColor = Color(0xFF1A3A1A)
private val deepBg = Color(0xFF050805)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = RuhanThemeColors.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Command Center Header
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = hackerGreen)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = deepBg)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(deepBg)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(cardBg, RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = hackerGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Command Center",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(hackerGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "SYSTEM ONLINE",
                            color = hackerGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton("GENERAL", Icons.Default.Settings, selectedTab == 0) { selectedTab = 0 }
                TabButton("API KEYS", Icons.Default.Key, selectedTab == 1) { selectedTab = 1 }
                TabButton("SECURITY", Icons.Default.Security, selectedTab == 2) { selectedTab = 2 }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tab Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> GeneralTab(uiState, viewModel)
                1 -> ApiKeysTab(uiState, viewModel)
                2 -> SecurityTab(uiState, viewModel)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Color(0xFF1A2A1A) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                if (isSelected) 1.dp else 0.dp,
                if (isSelected) hackerGreen.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) hackerGreen else Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 1.sp
            )
        }
    }
}

// ═══════════════════ GENERAL TAB ═══════════════════

@Composable
private fun GeneralTab(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    // AI Personality Matrix
    CardSection(title = "AI Personality Matrix", trailing = "${uiState.bossName.length}/150 WORDS") {
        OutlinedTextField(
            value = uiState.bossName,
            onValueChange = viewModel::updateBossName,
            placeholder = {
                Text(
                    "Define who RUHAN is. Example: 'You are a sassy, highly technical assistant...'",
                    color = Color(0xFF333833),
                    fontSize = 12.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                cursorColor = hackerGreen,
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg
            )
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // User Designation + Voice Profile
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CardSection(
            title = "User Designation",
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = uiState.wakeWord,
                onValueChange = viewModel::updateWakeWord,
                placeholder = { Text("Hacknuma", color = Color(0xFF333833)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    cursorColor = hackerGreen,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                singleLine = true
            )
        }

        CardSection(
            title = "OS Voice Profile",
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val genders = listOf("female" to "FEMALE", "male" to "MALE")
                genders.forEach { (value, label) ->
                    val isSelected = uiState.voiceGender == value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) hackerGreen.copy(alpha = 0.15f) else Color(0xFF2A2A2A),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) hackerGreen.copy(alpha = 0.3f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateVoiceGender(value) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (isSelected) hackerGreen else Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Voice Settings
    CardSection(title = "Voice Configuration") {
        SliderSetting("Voice Speed", uiState.voiceSpeed, viewModel::updateVoiceSpeed, 0.5f, 2.0f)
        Spacer(modifier = Modifier.height(8.dp))
        SliderSetting("Voice Pitch", uiState.voicePitch, viewModel::updateVoicePitch, 0.5f, 1.5f)
        Spacer(modifier = Modifier.height(8.dp))
        SliderSetting("Wake Sensitivity", uiState.wakeSensitivity, viewModel::updateWakeSensitivity, 0.1f, 1.0f)

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.testVoice() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("TEST VOICE", color = hackerGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Theme
    CardSection(title = "Interface Theme") {
        val themes = listOf(
            "hacker" to "HACKER" to Color(0xFF00FF41),
            "amoled" to "AMOLED" to Color(0xFF00FF41),
            "dark" to "DARK" to Color(0xFF44BB44),
            "pink" to "PINK" to Color(0xFFFF1493),
            "blue" to "BLUE" to Color(0xFF1E90FF),
            "gray" to "GRAY" to Color(0xFF6C63FF),
            "white" to "WHITE" to Color(0xFF00C853)
        )
        // First row: 4 themes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            themes.take(4).forEach { (pair, themeAccent) ->
                val (value, label) = pair
                val isSelected = uiState.theme == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) themeAccent.copy(alpha = 0.15f) else Color(0xFF1A1A1A),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) themeAccent.copy(alpha = 0.5f) else borderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateTheme(value) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isSelected) themeAccent else Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Second row: 3 themes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            themes.drop(4).forEach { (pair, themeAccent) ->
                val (value, label) = pair
                val isSelected = uiState.theme == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) themeAccent.copy(alpha = 0.15f) else Color(0xFF1A1A1A),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) themeAccent.copy(alpha = 0.5f) else borderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateTheme(value) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isSelected) themeAccent else Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Automation
    CardSection(title = "Automation") {
        SwitchRow("Always Listening", uiState.alwaysListening, viewModel::toggleAlwaysListening)
        SwitchRow("Daily Briefing", uiState.dailyBriefingEnabled, viewModel::toggleDailyBriefing)
        SwitchRow("Floating Button", uiState.floatingButton, viewModel::toggleFloatingButton)
    }
}

// ═══════════════════ API KEYS TAB ═══════════════════

@Composable
private fun ApiKeysTab(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    CardSection(title = "Neural Uplink Keys") {
        ApiKeyField(
            label = "GEMINI PRO CORE",
            value = uiState.geminiApiKey,
            onChange = viewModel::updateGeminiKey,
            status = uiState.geminiKeyStatus,
            onTest = viewModel::testGeminiKey,
            link = "https://aistudio.google.com/apikey"
        )

        Spacer(modifier = Modifier.height(14.dp))

        ApiKeyField(
            label = "GROQ FAST INFERENCING",
            value = uiState.groqApiKey,
            onChange = viewModel::updateGroqKey,
            status = uiState.groqKeyStatus,
            onTest = viewModel::testGroqKey,
            link = "https://console.groq.com"
        )

        Spacer(modifier = Modifier.height(14.dp))

        ApiKeyField(
            label = "HUGGING FACE TTS",
            value = uiState.huggingFaceApiKey,
            onChange = viewModel::updateHuggingFaceKey,
            status = uiState.hfKeyStatus,
            onTest = viewModel::testHuggingFaceKey,
            link = "https://huggingface.co/settings/tokens"
        )

        Spacer(modifier = Modifier.height(14.dp))

        ApiKeyField(
            label = "TAVILY RESEARCH AGENT",
            value = uiState.tavilyApiKey,
            onChange = viewModel::updateTavilyKey,
            status = uiState.tavilyKeyStatus,
            onTest = viewModel::testTavilyKey,
            link = "https://app.tavily.com"
        )

        Spacer(modifier = Modifier.height(14.dp))

        ApiKeyField(
            label = "NOTION SYNC ENGINE",
            value = uiState.notionApiKey,
            onChange = viewModel::updateNotionApiKey,
            status = uiState.notionKeyStatus,
            onTest = viewModel::testNotionKey,
            link = "https://www.notion.so/my-integrations"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "NOTION DATABASE ID",
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.notionDatabaseId,
                onValueChange = viewModel::updateNotionDatabaseId,
                placeholder = { Text("Database ID paste karo...", color = Color(0xFF333833), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = hackerGreen.copy(alpha = 0.3f),
                    unfocusedBorderColor = borderColor,
                    cursorColor = hackerGreen,
                    focusedContainerColor = deepBg,
                    unfocusedContainerColor = deepBg
                ),
                singleLine = true
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Security Notice
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(deepBg, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "[SECURITY NOTICE]: All API keys are encrypted via AES-256-GCM and stored strictly in your device's secure storage. RUHAN does not transmit these keys to any centralized server.",
            color = Color.Gray,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 14.sp
        )
    }
}

// ═══════════════════ SECURITY TAB ═══════════════════

@Composable
private fun SecurityTab(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel
) {
    CardSection(title = "Access Control") {
        SwitchRow("Biometric Lock", uiState.biometricLockEnabled, viewModel::toggleBiometricLock)
        SwitchRow("Fake Crash Screen", uiState.fakeCrashEnabled, viewModel::toggleFakeCrash)
        SwitchRow("Break-in Photo Capture", uiState.breakInPhotoEnabled, viewModel::toggleBreakInPhoto)
        SwitchRow("Memory Encryption", uiState.memoryEncryption, viewModel::toggleMemoryEncryption)
    }

    Spacer(modifier = Modifier.height(12.dp))

    CardSection(title = "Emergency Protocol") {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "EMERGENCY CONTACT",
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.emergencyContact,
                onValueChange = viewModel::updateEmergencyContact,
                placeholder = { Text("+91...", color = Color(0xFF333833)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = hackerGreen.copy(alpha = 0.3f),
                    unfocusedBorderColor = borderColor,
                    cursorColor = hackerGreen,
                    focusedContainerColor = deepBg,
                    unfocusedContainerColor = deepBg
                ),
                singleLine = true
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    CardSection(title = "Encryption Status") {
        StatusRow("Data Encryption", "AES-256-GCM", hackerGreen)
        StatusRow("Key Storage", "Android Keystore", hackerGreen)
        StatusRow("Memory Vault", if (uiState.memoryEncryption) "ENCRYPTED" else "OPEN", if (uiState.memoryEncryption) hackerGreen else Color.Yellow)
        StatusRow("API Keys", "SECURED", hackerGreen)
    }
}

// ═══════════════════ SHARED COMPONENTS ═══════════════════

@Composable
private fun CardSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (trailing != null) {
                Text(
                    trailing,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = Color.Gray,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusDot = when (status) {
                    KeyTestStatus.SUCCESS -> hackerGreen
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
                        color = hackerGreen.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable { onTest() }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("Paste key here...", color = Color(0xFF333833), fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = hackerGreen.copy(alpha = 0.3f),
                unfocusedBorderColor = borderColor,
                cursorColor = hackerGreen,
                focusedContainerColor = deepBg,
                unfocusedContainerColor = deepBg
            ),
            singleLine = true
        )
        Text(
            text = link,
            color = hackerGreen.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
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

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.LightGray, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = hackerGreen,
                checkedTrackColor = hackerGreen.copy(alpha = 0.3f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
    min: Float,
    max: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = Color.LightGray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            String.format("%.1f", value),
            color = hackerGreen,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
    Slider(
        value = value,
        onValueChange = onChange,
        valueRange = min..max,
        colors = SliderDefaults.colors(
            thumbColor = hackerGreen,
            activeTrackColor = hackerGreen,
            inactiveTrackColor = Color(0xFF1A3A1A)
        )
    )
}

@Composable
private fun StatusRow(name: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                name,
                color = Color.LightGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            status,
            color = statusColor.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
