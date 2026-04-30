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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cyanColor = Color(0xFF00E5FF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = cyanColor
                )
            }
            Text(
                text = "Settings",
                color = cyanColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boss Name
        SectionTitle("Personalization")
        SettingsTextField(
            label = "Boss ka Naam",
            value = uiState.bossName,
            onValueChange = { viewModel.updateBossName(it) }
        )

        SettingsTextField(
            label = "Wake Word",
            value = uiState.wakeWord,
            onValueChange = { viewModel.updateWakeWord(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Toggles
        SettingsToggle(
            label = "Always Listening",
            checked = uiState.alwaysListening,
            onCheckedChange = { viewModel.toggleAlwaysListening(it) }
        )

        SettingsToggle(
            label = "Floating Button",
            checked = uiState.floatingButton,
            onCheckedChange = { viewModel.toggleFloatingButton(it) }
        )

        // Voice Speed
        Text(
            text = "Voice Speed: ${String.format("%.1f", uiState.voiceSpeed)}x",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
        Slider(
            value = uiState.voiceSpeed,
            onValueChange = { viewModel.updateVoiceSpeed(it) },
            valueRange = 0.5f..2.0f,
            steps = 5,
            colors = SliderDefaults.colors(
                thumbColor = cyanColor,
                activeTrackColor = cyanColor,
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        SettingsDivider()

        // Emergency Contact
        SectionTitle("Emergency")
        SettingsTextField(
            label = "Emergency Contact Number",
            value = uiState.emergencyContact,
            onValueChange = { viewModel.updateEmergencyContact(it) }
        )

        SettingsDivider()

        // API Keys
        SectionTitle("API Keys")

        ApiKeyField(
            label = "Groq API Key",
            value = uiState.groqApiKey,
            onValueChange = { viewModel.updateGroqKey(it) },
            onTest = { viewModel.testGroqKey() },
            status = uiState.groqKeyStatus,
            linkUrl = "https://console.groq.com",
            linkText = "Get key from console.groq.com"
        )

        ApiKeyField(
            label = "Gemini API Key",
            value = uiState.geminiApiKey,
            onValueChange = { viewModel.updateGeminiKey(it) },
            onTest = { viewModel.testGeminiKey() },
            status = uiState.geminiKeyStatus,
            linkUrl = "https://aistudio.google.com/apikey",
            linkText = "Get key from aistudio.google.com"
        )

        ApiKeyField(
            label = "HuggingFace API Key",
            value = uiState.huggingFaceApiKey,
            onValueChange = { viewModel.updateHuggingFaceKey(it) },
            onTest = null,
            status = uiState.hfKeyStatus,
            linkUrl = "https://huggingface.co/settings/tokens",
            linkText = "Get token from huggingface.co"
        )

        ApiKeyField(
            label = "Tavily API Key (Optional)",
            value = uiState.tavilyApiKey,
            onValueChange = { viewModel.updateTavilyKey(it) },
            onTest = null,
            status = KeyTestStatus.IDLE,
            linkUrl = "https://app.tavily.com",
            linkText = "Get key from app.tavily.com"
        )

        SettingsDivider()

        // Language
        SectionTitle("Language")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LanguageChip("Hinglish", uiState.language == "hinglish") {
                viewModel.updateLanguage("hinglish")
            }
            LanguageChip("Hindi", uiState.language == "hindi") {
                viewModel.updateLanguage("hindi")
            }
            LanguageChip("English", uiState.language == "english") {
                viewModel.updateLanguage("english")
            }
        }

        SettingsDivider()

        // Theme
        SectionTitle("Theme")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LanguageChip("AMOLED", uiState.theme == "amoled") {
                viewModel.updateTheme("amoled")
            }
            LanguageChip("Dark", uiState.theme == "dark") {
                viewModel.updateTheme("dark")
            }
            LanguageChip("Light", uiState.theme == "light") {
                viewModel.updateTheme("light")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App info
        Text(
            text = "Ruhan AI v1.0.0",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "\"Han Boss, bolo.\"",
            color = cyanColor.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFF00E5FF),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF00E5FF),
            unfocusedBorderColor = Color(0xFF333333),
            cursorColor = Color(0xFF00E5FF)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E5FF),
                checkedTrackColor = Color(0xFF003344),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    @Suppress("DEPRECATION")
    Divider(
        modifier = Modifier.padding(vertical = 12.dp),
        color = Color(0xFF222222)
    )
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onTest: (() -> Unit)?,
    status: KeyTestStatus,
    linkUrl: String,
    linkText: String
) {
    val context = LocalContext.current
    val cyanColor = Color(0xFF00E5FF)

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = cyanColor,
                unfocusedBorderColor = Color(0xFF333333),
                cursorColor = cyanColor
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                when (status) {
                    KeyTestStatus.TESTING -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = cyanColor,
                        strokeWidth = 2.dp
                    )
                    KeyTestStatus.SUCCESS -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Valid",
                        tint = Color(0xFF00FF88)
                    )
                    KeyTestStatus.FAILED -> Icon(
                        Icons.Default.Error,
                        contentDescription = "Invalid",
                        tint = Color.Red
                    )
                    KeyTestStatus.IDLE -> {}
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = cyanColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = linkText,
                    color = cyanColor,
                    fontSize = 12.sp
                )
            }

            if (onTest != null && value.isNotBlank()) {
                Button(
                    onClick = onTest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A2E),
                        contentColor = cyanColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Test", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (selected) Color.Black else Color(0xFF00E5FF),
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .background(
                color = if (selected) Color(0xFF00E5FF) else Color(0xFF1A1A2E),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    )
}
