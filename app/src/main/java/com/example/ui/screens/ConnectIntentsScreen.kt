package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MyraViewModel
import com.example.ui.theme.MyraBorder
import com.example.ui.theme.MyraCardBg
import com.example.ui.theme.MyraCardBgElevated
import com.example.ui.theme.MyraCyberCyan
import com.example.ui.theme.MyraMatrixGreen
import com.example.ui.theme.MyraNeonGreen
import com.example.ui.theme.MyraTextPrimary
import com.example.ui.theme.MyraTextSecondary

@Composable
fun ConnectIntentsScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardHistory by viewModel.clipboardHistory.collectAsState()

    var whatsAppPhone by remember { mutableStateOf("") }
    var whatsAppMsg by remember { mutableStateOf("") }

    var callPhone by remember { mutableStateOf("") }
    var smsPhone by remember { mutableStateOf("") }
    var smsBody by remember { mutableStateOf("") }

    var webSearchQuery by remember { mutableStateOf("") }
    var newClipText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Google / Web Search
        item {
            Text(
                text = "🌐 WEB SEARCH & INTELLIGENCE",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MyraNeonGreen
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MyraCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MyraBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = webSearchQuery,
                            onValueChange = { webSearchQuery = it },
                            placeholder = { Text("Search Google / Web...", color = MyraTextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MyraCyberCyan) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("web_search_field"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyraCyberCyan,
                                unfocusedBorderColor = MyraBorder,
                                focusedTextColor = MyraTextPrimary,
                                unfocusedTextColor = MyraTextPrimary,
                                focusedContainerColor = MyraCardBgElevated,
                                unfocusedContainerColor = MyraCardBgElevated
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (webSearchQuery.isNotBlank()) {
                                    viewModel.automationManager.openWebSearch(webSearchQuery)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MyraCyberCyan, contentColor = Color(0xFF003039)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_search_btn")
                        ) {
                            Text("Search")
                        }
                    }
                }
            }
        }

        // Section 2: WhatsApp & Direct Communications
        item {
            Text(
                text = "💬 WHATSAPP & COMMUNICATIONS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MyraMatrixGreen
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MyraCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MyraBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "WhatsApp Direct Chat (No saving needed)",
                        style = MaterialTheme.typography.labelMedium.copy(color = MyraMatrixGreen, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = whatsAppPhone,
                        onValueChange = { whatsAppPhone = it },
                        placeholder = { Text("Phone with country code (e.g. +88017...)", color = MyraTextSecondary) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = MyraMatrixGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_phone_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MyraMatrixGreen,
                            unfocusedBorderColor = MyraBorder,
                            focusedTextColor = MyraTextPrimary,
                            unfocusedTextColor = MyraTextPrimary,
                            focusedContainerColor = MyraCardBgElevated,
                            unfocusedContainerColor = MyraCardBgElevated
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = whatsAppMsg,
                            onValueChange = { whatsAppMsg = it },
                            placeholder = { Text("Optional message...", color = MyraTextSecondary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyraMatrixGreen,
                                unfocusedBorderColor = MyraBorder,
                                focusedTextColor = MyraTextPrimary,
                                unfocusedTextColor = MyraTextPrimary,
                                focusedContainerColor = MyraCardBgElevated,
                                unfocusedContainerColor = MyraCardBgElevated
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (whatsAppPhone.isNotBlank()) {
                                    viewModel.automationManager.openWhatsAppChat(whatsAppPhone, whatsAppMsg)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MyraMatrixGreen, contentColor = Color(0xFF021B0C)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("launch_whatsapp_btn")
                        ) {
                            Text("Open")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Call & SMS Quick Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = callPhone,
                            onValueChange = { callPhone = it },
                            placeholder = { Text("Dial number...", color = MyraTextSecondary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyraNeonGreen,
                                unfocusedBorderColor = MyraBorder,
                                focusedTextColor = MyraTextPrimary,
                                unfocusedTextColor = MyraTextPrimary,
                                focusedContainerColor = MyraCardBgElevated,
                                unfocusedContainerColor = MyraCardBgElevated
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (callPhone.isNotBlank()) {
                                    viewModel.automationManager.openDialer(callPhone)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MyraNeonGreen, contentColor = Color(0xFF031A0D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("call_btn")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dial")
                        }
                    }
                }
            }
        }

        // Section 3: Deep Links & Android System Intents
        item {
            Text(
                text = "🔗 DEEP LINKS & SYSTEM INTENTS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MyraCyberCyan
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MyraCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MyraBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IntentChip(
                            icon = Icons.Default.Map,
                            label = "Google Maps",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=restaurants+nearby")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        )

                        IntentChip(
                            icon = Icons.Default.VideoLibrary,
                            label = "YouTube",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        )

                        IntentChip(
                            icon = Icons.Default.Wifi,
                            label = "Wi-Fi Hub",
                            onClick = { viewModel.automationManager.openSettings(Settings.ACTION_WIFI_SETTINGS) }
                        )

                        IntentChip(
                            icon = Icons.Default.Link,
                            label = "Bluetooth",
                            onClick = { viewModel.automationManager.openSettings(Settings.ACTION_BLUETOOTH_SETTINGS) }
                        )
                    }
                }
            }
        }

        // Section 4: Clipboard Manager & History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📋 CLIPBOARD VAULT",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MyraNeonGreen
                    )
                )
                Text(
                    text = "${clipboardHistory.size} Items",
                    style = MaterialTheme.typography.labelSmall.copy(color = MyraTextSecondary)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Quick Copy Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newClipText,
                    onValueChange = { newClipText = it },
                    placeholder = { Text("Copy text to clipboard...", color = MyraTextSecondary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MyraNeonGreen,
                        unfocusedBorderColor = MyraBorder,
                        focusedTextColor = MyraTextPrimary,
                        unfocusedTextColor = MyraTextPrimary,
                        focusedContainerColor = MyraCardBg,
                        unfocusedContainerColor = MyraCardBg
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (newClipText.isNotBlank()) {
                            viewModel.copyToClipboard(newClipText)
                            newClipText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MyraNeonGreen, contentColor = Color(0xFF031A0D)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
            }
        }

        // Clipboard History Items
        if (clipboardHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MyraCardBg)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Clipboard history empty. Copy something to save!", color = MyraTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            items(clipboardHistory.take(10)) { item ->
                ClipboardItemRow(
                    content = item.content,
                    onCopyAgain = { viewModel.copyToClipboard(item.content) },
                    onDelete = {
                        kotlinx.coroutines.runBlocking {
                            viewModel.repository.deleteClipboard(item.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IntentChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MyraCardBgElevated)
            .border(1.dp, MyraBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MyraCyberCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MyraTextPrimary))
        }
    }
}

@Composable
fun ClipboardItemRow(
    content: String,
    onCopyAgain: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MyraCardBg)
            .border(1.dp, MyraBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = MyraTextPrimary
            ),
            modifier = Modifier.weight(1f),
            maxLines = 2
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCopyAgain, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MyraNeonGreen, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF6666), modifier = Modifier.size(16.dp))
            }
        }
    }
}
