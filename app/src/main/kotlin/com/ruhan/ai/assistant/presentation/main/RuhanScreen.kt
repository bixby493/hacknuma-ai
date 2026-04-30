package com.ruhan.ai.assistant.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhan.ai.assistant.presentation.components.ConversationBubble
import com.ruhan.ai.assistant.presentation.components.RuhanOrb
import com.ruhan.ai.assistant.presentation.components.WaveformVisualizer
import com.ruhan.ai.assistant.presentation.theme.RuhanThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuhanScreen(
    viewModel: RuhanViewModel,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val colors = RuhanThemeColors.current

    LaunchedEffect(uiState.conversations.size) {
        if (uiState.conversations.isNotEmpty()) {
            listState.animateScrollToItem(uiState.conversations.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 56.dp)) {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "R U H A N   A I",
                                color = colors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = 4.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.modelName,
                                color = colors.accent.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Taiyaar hoon Boss",
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = colors.accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                StatusChip("Groq", uiState.groqStatus, colors.chipBg)
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip("Gemini", uiState.geminiStatus, colors.chipBg)
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip("HF", uiState.hfStatus, colors.chipBg)
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip("Tavily", uiState.tavilyStatus, colors.chipBg)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RuhanOrb(
                        state = uiState.orbState,
                        modifier = Modifier.size(140.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.statusText,
                        color = colors.textSecondary,
                        fontSize = 14.sp
                    )
                    if (uiState.currentTranscript.isNotBlank()) {
                        Text(
                            text = "\"${uiState.currentTranscript}\"",
                            color = colors.accent.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (uiState.liveTranscript.isNotBlank()) {
                        Text(
                            text = uiState.liveTranscript,
                            color = Color.Green.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.isListening,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                WaveformVisualizer(
                    isActive = uiState.isListening,
                    color = colors.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 32.dp)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(uiState.conversations) { message ->
                    ConversationBubble(
                        message = message.message,
                        isUser = message.isUser,
                        timestamp = message.timestamp,
                        userBubbleColor = colors.userBubble,
                        ruhanBubbleColor = colors.ruhanBubble,
                        userTextColor = colors.textPrimary,
                        ruhanTextColor = colors.ruhanText
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.showKeyboard,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.textInput,
                        onValueChange = { viewModel.onTextInputChanged(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Boss, kya likhna hai?", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.inputBorder,
                            cursorColor = colors.accent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.submitTextInput() }),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.submitTextInput() }) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = colors.accent)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Screen Capture / Optics
                IconButton(
                    onClick = { viewModel.processInput("screen par kya hai analyze karo") }
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        "Screen Capture",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Live Voice
                IconButton(
                    onClick = {
                        if (uiState.isLiveVoiceActive) viewModel.stopLiveVoice()
                        else viewModel.startLiveVoice()
                    }
                ) {
                    Icon(
                        if (uiState.isLiveVoiceActive) Icons.Default.Close else Icons.Default.GraphicEq,
                        "Live Voice",
                        tint = if (uiState.isLiveVoiceActive) Color.Green else colors.textSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Main Mic Button
                IconButton(
                    onClick = {
                        if (uiState.isListening) viewModel.stopListening()
                        else viewModel.startListening()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (uiState.isListening) colors.accent else colors.card,
                            CircleShape
                        )
                ) {
                    Icon(
                        if (uiState.isListening) Icons.Default.Close else Icons.Default.Mic,
                        "Microphone",
                        tint = if (uiState.isListening) colors.background else colors.accent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Keyboard
                IconButton(onClick = { viewModel.toggleKeyboard() }) {
                    Icon(
                        Icons.Default.Keyboard,
                        "Keyboard",
                        tint = if (uiState.showKeyboard) colors.accent else colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Screen Share
                IconButton(
                    onClick = { viewModel.processInput("screen share karo") }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ScreenShare,
                        "Screen Share",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        uiState.pendingConfirmation?.let { question ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelAction() },
                title = { Text("Ruhan", color = colors.accent) },
                text = { Text(question, color = colors.textPrimary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmAction() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) { Text("Han", color = colors.background) }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.cancelAction() }) {
                        Text("Nahi", color = colors.textSecondary)
                    }
                },
                containerColor = colors.card
            )
        }
    }
}

@Composable
private fun StatusChip(name: String, isActive: Boolean, chipBg: Color) {
    val color = if (isActive) Color.Green else Color.Red
    val colors = RuhanThemeColors.current
    Row(
        modifier = Modifier
            .background(chipBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, color = colors.textSecondary, fontSize = 10.sp)
    }
}
