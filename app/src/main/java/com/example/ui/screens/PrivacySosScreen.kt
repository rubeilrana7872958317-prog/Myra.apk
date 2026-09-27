package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.core.content.ContextCompat
import com.example.ui.MyraViewModel
import com.example.ui.theme.MyraAlertRed
import com.example.ui.theme.MyraBorder
import com.example.ui.theme.MyraCardBg
import com.example.ui.theme.MyraCardBgElevated
import com.example.ui.theme.MyraCyberCyan
import com.example.ui.theme.MyraMatrixGreen
import com.example.ui.theme.MyraNeonGreen
import com.example.ui.theme.MyraTextPrimary
import com.example.ui.theme.MyraTextSecondary

@Composable
fun PrivacySosScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()
    val language by viewModel.language.collectAsState()
    val personality by viewModel.personality.collectAsState()

    var emergencyPhone by remember { mutableStateOf("112") }

    // Runtime Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refreshTelemetry()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Emergency SOS Controls
        item {
            Text(
                text = "🛡️ EMERGENCY & SAFETY PROTOCOL",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MyraAlertRed
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MyraCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (telemetry.isSirenActive) MyraAlertRed else MyraBorder, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big SOS Glowing Trigger
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(if (telemetry.isSirenActive) Color(0xFF8A0B28) else Color(0xFF381017))
                            .border(3.dp, MyraAlertRed, CircleShape)
                            .clickable {
                                if (telemetry.isSirenActive) {
                                    viewModel.stopSosAlert()
                                } else {
                                    viewModel.triggerSosAlert()
                                }
                            }
                            .testTag("sos_trigger_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (telemetry.isSirenActive) Icons.Default.Stop else Icons.Default.Warning,
                                contentDescription = "SOS",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (telemetry.isSirenActive) "STOP" else "SOS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (telemetry.isSirenActive) "⚠️ SIREN & STROBE ACTIVE" else "TAP FOR IMMEDIATE EMERGENCY SIREN & BEACON",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.isSirenActive) MyraAlertRed else MyraTextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Dial & SMS Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emergencyPhone,
                            onValueChange = { emergencyPhone = it },
                            label = { Text("Emergency Contact") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MyraAlertRed,
                                unfocusedBorderColor = MyraBorder,
                                focusedTextColor = MyraTextPrimary,
                                unfocusedTextColor = MyraTextPrimary,
                                focusedContainerColor = MyraCardBgElevated,
                                unfocusedContainerColor = MyraCardBgElevated
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = { viewModel.automationManager.openDialer(emergencyPhone) },
                            colors = ButtonDefaults.buttonColors(containerColor = MyraAlertRed, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("emergency_call_btn")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call")
                        }

                        Button(
                            onClick = {
                                viewModel.automationManager.openSmsComposer(
                                    emergencyPhone,
                                    "EMERGENCY ALERT: I need immediate help. MYRA SOS activated."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1D2A), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("emergency_sms_btn")
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SMS")
                        }
                    }
                }
            }
        }

        // Section 2: Permission & Privacy System
        item {
            Text(
                text = "🔐 PERMISSION & PRIVACY AUDIT",
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
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PermissionStatusRow(
                        name = "Microphone (Audio Recognition)",
                        permission = Manifest.permission.RECORD_AUDIO,
                        context = context
                    )
                    PermissionStatusRow(
                        name = "Camera (Flashlight Torch)",
                        permission = Manifest.permission.CAMERA,
                        context = context
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.CAMERA
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MyraNeonGreen, contentColor = Color(0xFF031A0D)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Grant Required Permissions")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Privacy Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.clearChat() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MyraCardBgElevated, contentColor = MyraTextSecondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear Chat Log", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.clearMemories() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MyraCardBgElevated, contentColor = MyraAlertRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Wipe Neural Memory", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section 3: AI Personality & Language Settings
        item {
            Text(
                text = "🤖 AI PERSONALITY & LANGUAGE",
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
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Voice Assistant Language",
                        style = MaterialTheme.typography.labelMedium.copy(color = MyraTextSecondary, fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageSelectorButton(
                            title = "বাংলা (BN)",
                            isSelected = language == "bn",
                            onClick = { viewModel.setLanguage("bn") },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageSelectorButton(
                            title = "हिन्दी (HI)",
                            isSelected = language == "hi",
                            onClick = { viewModel.setLanguage("hi") },
                            modifier = Modifier.weight(1f)
                        )
                        LanguageSelectorButton(
                            title = "English (US)",
                            isSelected = language == "en",
                            onClick = { viewModel.setLanguage("en") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Persona Archetype",
                        style = MaterialTheme.typography.labelMedium.copy(color = MyraTextSecondary, fontWeight = FontWeight.Bold)
                    )

                    val personas = listOf("Tactical Cyberpunk", "Empathetic Assistant", "Jarvis Prime")
                    personas.forEach { personaName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (personality == personaName) Color(0xFF0C2417) else MyraCardBgElevated)
                                .border(1.dp, if (personality == personaName) MyraNeonGreen else MyraBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setPersonality(personaName) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = personaName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (personality == personaName) FontWeight.Bold else FontWeight.Normal,
                                    color = if (personality == personaName) MyraNeonGreen else MyraTextPrimary
                                )
                            )
                            if (personality == personaName) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MyraNeonGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionStatusRow(
    name: String,
    permission: String,
    context: Context
) {
    val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(color = MyraTextPrimary)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isGranted) Color(0xFF0F301C) else Color(0xFF38151B))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isGranted) "GRANTED" else "REQUIRED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) MyraMatrixGreen else MyraAlertRed
                )
            )
        }
    }
}

@Composable
fun LanguageSelectorButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MyraNeonGreen else MyraCardBgElevated,
            contentColor = if (isSelected) Color(0xFF021B0C) else MyraTextPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
