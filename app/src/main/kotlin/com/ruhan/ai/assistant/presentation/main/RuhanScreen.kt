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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhan.ai.assistant.presentation.components.ConversationBubble
import com.ruhan.ai.assistant.presentation.components.RuhanOrb
import com.ruhan.ai.assistant.presentation.components.WaveformVisualizer

private val CyanColor = Color(0xFF00E5FF)
private val DarkBg = Color(0xFF000000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuhanScreen(
    viewModel: RuhanViewModel,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.conversations.size) {
        if (uiState.conversations.isNotEmpty()) {
            listState.animateScrollToItem(uiState.conversations.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "R U H A N",
                            color = CyanColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Taiyaar hoon Boss",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = CyanColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )

            // API Status Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                StatusChip("Groq", uiState.groqStatus)
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip("Gemini", uiState.geminiStatus)
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip("HF", uiState.hfStatus)
                Spacer(modifier = Modifier.width(6.dp))
                StatusChip("Tavily", uiState.tavilyStatus)
            }

            // Orb Section
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
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    if (uiState.currentTranscript.isNotBlank()) {
                        Text(
                            text = "\"${uiState.currentTranscript}\"",
                            color = CyanColor.copy(alpha = 0.7f),
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

            // Waveform
            AnimatedVisibility(
                visible = uiState.isListening,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                WaveformVisualizer(
                    isActive = uiState.isListening,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 32.dp)
                )
            }

            // Conversation History
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
                        timestamp = message.timestamp
                    )
                }
            }

            // Keyboard Input
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
                        placeholder = { Text("Boss, kya likhna hai?", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanColor,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = CyanColor
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.submitTextInput() }),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.submitTextInput() }) {
                        Icon(Icons.Default.Send, "Send", tint = CyanColor)
                    }
                }
            }

            // Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Voice Button
                IconButton(
                    onClick = {
                        if (uiState.isLiveVoiceActive) viewModel.stopLiveVoice()
                        else viewModel.startLiveVoice()
                    }
                ) {
                    Icon(
                        if (uiState.isLiveVoiceActive) Icons.Default.Close else Icons.Default.GraphicEq,
                        "Live Voice",
                        tint = if (uiState.isLiveVoiceActive) Color.Green else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Mic Button
                IconButton(
                    onClick = {
                        if (uiState.isListening) viewModel.stopListening()
                        else viewModel.startListening()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (uiState.isListening) CyanColor else Color(0xFF1A1A1A),
                            CircleShape
                        )
                ) {
                    Icon(
                        if (uiState.isListening) Icons.Default.Close else Icons.Default.Mic,
                        "Microphone",
                        tint = if (uiState.isListening) DarkBg else CyanColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Keyboard Button
                IconButton(onClick = { viewModel.toggleKeyboard() }) {
                    Icon(
                        Icons.Default.Keyboard,
                        "Keyboard",
                        tint = if (uiState.showKeyboard) CyanColor else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Confirmation Dialog
        uiState.pendingConfirmation?.let { question ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelAction() },
                title = { Text("Ruhan", color = CyanColor) },
                text = { Text(question, color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmAction() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanColor)
                    ) { Text("Han", color = DarkBg) }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.cancelAction() }) {
                        Text("Nahi", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
private fun StatusChip(name: String, isActive: Boolean) {
    val color = if (isActive) Color.Green else Color.Red
    Row(
        modifier = Modifier
            .background(Color(0xFF111111), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, color = Color.Gray, fontSize = 10.sp)
    }
}
