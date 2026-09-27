package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ConversationEntity
import com.example.ui.MyraViewModel
import com.example.ui.VoiceState
import com.example.ui.components.HolographicCore
import com.example.ui.components.TelemetryBar
import com.example.ui.theme.MyraBorder
import com.example.ui.theme.MyraCardBg
import com.example.ui.theme.MyraCardBgElevated
import com.example.ui.theme.MyraCyberCyan
import com.example.ui.theme.MyraMatrixGreen
import com.example.ui.theme.MyraNeonGreen
import com.example.ui.theme.MyraTextPrimary
import com.example.ui.theme.MyraTextSecondary

@Composable
fun CoreHudScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val language by viewModel.language.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversations.size) {
        if (conversations.isNotEmpty()) {
            listState.animateScrollToItem(conversations.size - 1)
        }
    }

    val quickPrompts = when (language) {
        "bn" -> listOf(
            "ইউটিউব খোলো",
            "গান বাজাও",
            "টর্চলাইট অন",
            "৫ মিনিটের টাইমার",
            "ব্যাটারি কেমন আছে?",
            "জরুরি সাইরেন"
        )
        "hi" -> listOf(
            "यूट्यूब खोलो",
            "संगीत बजाओ",
            "टॉर्च चालू करो",
            "५ मिनट का टाइमर",
            "बैटरी की स्थिति",
            "आपातकालीन सायरन"
        )
        else -> listOf(
            "Open YouTube",
            "Play cyber music",
            "Toggle flashlight",
            "Set 5 min timer",
            "Battery status",
            "Emergency SOS"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Telemetry Status
        TelemetryBar(telemetry = telemetry, currentLanguage = language)

        Spacer(modifier = Modifier.height(10.dp))

        // Center Holographic Voice Core
        HolographicCore(
            voiceState = voiceState,
            audioRms = audioRms,
            onCoreClick = {
                if (voiceState == VoiceState.LISTENING) {
                    viewModel.stopListening()
                } else if (voiceState == VoiceState.SPEAKING) {
                    viewModel.stopSpeaking()
                } else {
                    viewModel.startListening()
                }
            }
        )

        // Live Voice Transcript (if active)
        if (transcript.isNotBlank() && voiceState == VoiceState.LISTENING) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MyraCardBgElevated)
                    .border(1.dp, MyraMatrixGreen, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🎙️ $transcript",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MyraMatrixGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Command Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MyraCardBg)
                        .border(1.dp, MyraNeonGreen.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.handleUserQuery(prompt, isVoice = false) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("quick_prompt_${prompt.take(6)}")
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MyraNeonGreen
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Terminal Chat Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MyraCardBg)
                .border(1.dp, MyraBorder, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            if (conversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MYRA CYBER TERMINAL",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MyraNeonGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == "bn") "ভয়েস বাটনে ট্যাপ করুন বা নিচে লিখুন" else "Tap voice core or enter command below",
                            style = MaterialTheme.typography.bodySmall.copy(color = MyraTextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(conversations) { msg ->
                        ConversationBubble(
                            conversation = msg,
                            onCopy = { viewModel.copyToClipboard(msg.message) },
                            onSpeak = { viewModel.handleUserQuery("Repeat: ${msg.message}") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Command Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = if (language == "bn") "মায়রা কে নির্দেশ দিন..." else "Enter command for MYRA...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MyraTextSecondary)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MyraNeonGreen,
                    unfocusedBorderColor = MyraBorder,
                    focusedTextColor = MyraTextPrimary,
                    unfocusedTextColor = MyraTextPrimary,
                    focusedContainerColor = MyraCardBg,
                    unfocusedContainerColor = MyraCardBg
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        val toSend = textInput
                        textInput = ""
                        viewModel.handleUserQuery(toSend, isVoice = false)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MyraNeonGreen)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color(0xFF031A0D)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Voice Listen/Stop Toggle Button
            IconButton(
                onClick = {
                    if (voiceState == VoiceState.LISTENING) {
                        viewModel.stopListening()
                    } else if (voiceState == VoiceState.SPEAKING) {
                        viewModel.stopSpeaking()
                    } else {
                        viewModel.startListening()
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (voiceState == VoiceState.LISTENING) MyraMatrixGreen else MyraCardBgElevated)
                    .border(1.dp, MyraNeonGreen, CircleShape)
                    .testTag("mic_toggle_button")
            ) {
                Icon(
                    imageVector = when (voiceState) {
                        VoiceState.LISTENING -> Icons.Default.MicOff
                        VoiceState.SPEAKING -> Icons.Default.Stop
                        else -> Icons.Default.Mic
                    },
                    contentDescription = "Voice Assistant",
                    tint = if (voiceState == VoiceState.LISTENING) Color(0xFF031A0D) else MyraNeonGreen
                )
            }
        }
    }
}

@Composable
fun ConversationBubble(
    conversation: ConversationEntity,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = conversation.sender == "USER"
    val bubbleColor = if (isUser) MyraCardBgElevated else Color(0xFF0B1F15)
    val borderColor = if (isUser) MyraCyberCyan.copy(alpha = 0.5f) else MyraNeonGreen.copy(alpha = 0.5f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isUser) "COMMANDER" else "MYRA AI",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = if (isUser) MyraCyberCyan else MyraNeonGreen
            )
            if (conversation.actionType != "CHAT") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF132D1F))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = conversation.actionType,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MyraMatrixGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = conversation.message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MyraTextPrimary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MyraTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
