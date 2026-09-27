package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UiTelemetry
import com.example.ui.theme.MyraAlertRed
import com.example.ui.theme.MyraBorder
import com.example.ui.theme.MyraCardBg
import com.example.ui.theme.MyraCyberCyan
import com.example.ui.theme.MyraMatrixGreen
import com.example.ui.theme.MyraNeonGreen

@Composable
fun TelemetryBar(
    telemetry: UiTelemetry,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MyraCardBg)
            .border(1.dp, MyraBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Battery Stats
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = if (telemetry.batteryPercent > 20) MyraNeonGreen else MyraAlertRed,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${telemetry.batteryPercent}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MyraNeonGreen
            )
        }

        // Memory Stats
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = "RAM",
                tint = MyraCyberCyan,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${telemetry.freeRamMb}M",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MyraCyberCyan
            )
        }

        // Active Features indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (telemetry.isFlashlightActive) {
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = "Flashlight Active",
                    tint = MyraMatrixGreen,
                    modifier = Modifier.size(15.dp)
                )
            }
            if (telemetry.isAmbientActive) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Ambient Synth Active",
                    tint = MyraCyberCyan,
                    modifier = Modifier.size(15.dp)
                )
            }
            if (telemetry.isSirenActive) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "SOS Siren Active",
                    tint = MyraAlertRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Language Pill
        val langLabel = when (currentLanguage) {
            "bn" -> "বাং (BN)"
            "hi" -> "हिन्द (HI)"
            else -> "ENG (US)"
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F2618))
                .border(1.dp, MyraNeonGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = langLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MyraNeonGreen
            )
        }
    }
}
