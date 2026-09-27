package com.example.ui.screens

import android.provider.Settings
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
fun AutomationAppsScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val timerRemaining by viewModel.timerRemainingSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val language by viewModel.language.collectAsState()

    var appSearchQuery by remember { mutableStateOf("") }
    var volumeSlider by remember { mutableFloatStateOf(telemetry.currentVolume) }

    val filteredApps = remember(installedApps, appSearchQuery) {
        if (appSearchQuery.isBlank()) {
            installedApps.take(24) // Top 24 apps
        } else {
            installedApps.filter { it.appName.contains(appSearchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Device & Screen Automation Controls
        item {
            Text(
                text = "⚡ DEVICE & SCREEN AUTOMATION",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MyraNeonGreen
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MyraCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MyraBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Flashlight and Vibration Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleFlashlight() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("flashlight_toggle_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (telemetry.isFlashlightActive) MyraMatrixGreen else MyraCardBgElevated,
                                contentColor = if (telemetry.isFlashlightActive) Color(0xFF021B0C) else MyraNeonGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FlashlightOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (telemetry.isFlashlightActive) "Torch ON" else "Torch OFF", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.automationManager.triggerVibration(250) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vibrate_test_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MyraCardBgElevated,
                                contentColor = MyraCyberCyan
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Haptic Pulse", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Volume Control Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Vol Down",
                            tint = MyraTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Slider(
                            value = volumeSlider,
                            onValueChange = {
                                volumeSlider = it
                                viewModel.setVolume(it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("volume_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MyraNeonGreen,
                                activeTrackColor = MyraNeonGreen,
                                inactiveTrackColor = MyraBorder
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Vol Up",
                            tint = MyraNeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Screen Automation & Settings Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionChip(
                            icon = Icons.Default.BrightnessMedium,
                            label = "Display",
                            onClick = { viewModel.automationManager.openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                        )
                        QuickActionChip(
                            icon = Icons.Default.TouchApp,
                            label = "Accessibility",
                            onClick = { viewModel.automationManager.openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
                        )
                        QuickActionChip(
                            icon = Icons.Default.Smartphone,
                            label = "System",
                            onClick = { viewModel.automationManager.openSettings() }
                        )
                    }
                }
            }
        }

        // Section 2: Cyber Ambient Music & Synthesizer
        item {
            Text(
                text = "🎵 CYBER AMBIENT SOUND MATRIX",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MyraCyberCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MyraCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MyraBorder, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (telemetry.isAmbientActive) "Playing: Neural Cyber Pulse" else "Ambient Synth: Standby",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (telemetry.isAmbientActive) MyraCyberCyan else MyraTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Binaural 110Hz sci-fi tone synthesized on-device",
                            style = MaterialTheme.typography.bodySmall.copy(color = MyraTextSecondary, fontSize = 11.sp)
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleAmbientAudio() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (telemetry.isAmbientActive) MyraCyberCyan else MyraCardBgElevated,
                            contentColor = if (telemetry.isAmbientActive) Color(0xFF003038) else MyraCyberCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("ambient_audio_btn")
                    ) {
                        Icon(
                            imageVector = if (telemetry.isAmbientActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Ambient",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (telemetry.isAmbientActive) "Pause" else "Play")
                    }
                }
            }
        }

        // Section 3: Alarm & Interactive Countdown Timer
        item {
            Text(
                text = "🔔 ALARM & COUNTDOWN TIMER",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MyraNeonGreen
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MyraCardBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MyraBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Timer Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val mins = timerRemaining / 60
                            val secs = timerRemaining % 60
                            val timeStr = String.format("%02d:%02d", mins, secs)
                            Text(
                                text = if (isTimerRunning) "TIMER: $timeStr" else "TIMER: 00:00 (Ready)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTimerRunning) MyraMatrixGreen else MyraTextPrimary
                                )
                            )
                            Text(
                                text = if (isTimerRunning) "Active Countdown" else "Select duration below",
                                style = MaterialTheme.typography.labelSmall.copy(color = MyraTextSecondary)
                            )
                        }

                        if (isTimerRunning) {
                            Button(
                                onClick = { viewModel.stopTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1818), contentColor = Color(0xFFFF6666)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timer Preset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimerPresetChip(label = "1 Min", onClick = { viewModel.startTimer(1) })
                        TimerPresetChip(label = "5 Min", onClick = { viewModel.startTimer(5) })
                        TimerPresetChip(label = "15 Min", onClick = { viewModel.startTimer(15) })
                        TimerPresetChip(label = "Alarm", icon = Icons.Default.Alarm, onClick = {
                            viewModel.automationManager.setSystemAlarm(8, 0, "MYRA Morning Brief")
                        })
                    }
                }
            }
        }

        // Section 4: Installed Apps Launcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📱 INSTALLED APPS LAUNCHER",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MyraNeonGreen
                    )
                )
                Text(
                    text = "${filteredApps.size} Apps",
                    style = MaterialTheme.typography.labelSmall.copy(color = MyraTextSecondary)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // App Search Field
            OutlinedTextField(
                value = appSearchQuery,
                onValueChange = { appSearchQuery = it },
                placeholder = { Text("Search installed apps...", color = MyraTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MyraNeonGreen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_field"),
                shape = RoundedCornerShape(10.dp),
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
        }

        // App Launch Grid
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MyraCardBg)
                    .border(1.dp, MyraBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (filteredApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No applications found", color = MyraTextSecondary)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredApps) { app ->
                            AppLaunchCard(
                                appName = app.appName,
                                onClick = { viewModel.automationManager.launchAppByPackage(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppLaunchCard(
    appName: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MyraCardBgElevated)
            .border(1.dp, MyraBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("app_${appName.take(6)}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F2618)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = appName,
                    tint = MyraNeonGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = MyraTextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MyraCardBgElevated)
            .border(1.dp, MyraBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MyraCyberCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MyraTextPrimary))
        }
    }
}

@Composable
fun TimerPresetChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.HourglassBottom,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MyraCardBgElevated)
            .border(1.dp, MyraNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MyraNeonGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MyraNeonGreen, fontWeight = FontWeight.Bold))
        }
    }
}
