package com.ruhan.ai.assistant.presentation.main

import android.Manifest
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ruhan.ai.assistant.presentation.components.ConversationBubble
import com.ruhan.ai.assistant.presentation.components.OrbState
import com.ruhan.ai.assistant.presentation.components.RuhanOrb
import com.ruhan.ai.assistant.presentation.components.WaveformVisualizer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RuhanScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: RuhanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val listState = rememberLazyListState()

    LaunchedEffect(uiState.conversations.size) {
        if (uiState.conversations.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RUHAN",
                    color = Color(0xFF00E5FF),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }

            // Orb section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RuhanOrb(
                        state = uiState.orbState,
                        modifier = Modifier
                            .size(200.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        if (micPermission.status.isGranted) {
                                            if (uiState.isListening) {
                                                viewModel.stopListening()
                                            } else {
                                                viewModel.startListening()
                                            }
                                        } else {
                                            micPermission.launchPermissionRequest()
                                        }
                                    }
                                )
                            }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status text
                    Text(
                        text = uiState.statusText,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    // Live transcript
                    if (uiState.currentTranscript.isNotBlank()) {
                        Text(
                            text = uiState.currentTranscript,
                            color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                        )
                    }

                    // Waveform
                    AnimatedVisibility(
                        visible = uiState.orbState == OrbState.LISTENING,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        WaveformVisualizer(
                            isActive = uiState.isListening,
                            modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Conversation history
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .padding(bottom = 8.dp),
                reverseLayout = true
            ) {
                items(
                    items = uiState.conversations,
                    key = { it.id }
                ) { message ->
                    ConversationBubble(
                        message = message.message,
                        isUser = message.isUser,
                        timestamp = message.timestamp
                    )
                }
            }

            // Text input area
            AnimatedVisibility(
                visible = uiState.showKeyboard,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.textInput,
                        onValueChange = { viewModel.updateTextInput(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text("Type your message...", color = Color.Gray)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF333333),
                            cursorColor = Color(0xFF00E5FF)
                        ),
                        textStyle = TextStyle(fontSize = 15.sp),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = { viewModel.sendTextMessage() }
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = { viewModel.sendTextMessage() },
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }

            // Bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic button
                FloatingActionButton(
                    onClick = {
                        if (micPermission.status.isGranted) {
                            if (uiState.isListening) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListening()
                            }
                        } else {
                            micPermission.launchPermissionRequest()
                        }
                    },
                    containerColor = if (uiState.isListening) Color(0xFF00E5FF) else Color(0xFF1A1A2E),
                    contentColor = if (uiState.isListening) Color.Black else Color(0xFF00E5FF),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Microphone",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Keyboard button
                FloatingActionButton(
                    onClick = { viewModel.toggleKeyboard() },
                    containerColor = if (uiState.showKeyboard) Color(0xFF00E5FF) else Color(0xFF1A1A2E),
                    contentColor = if (uiState.showKeyboard) Color.Black else Color(0xFF00E5FF),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Keyboard,
                        contentDescription = "Keyboard",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
