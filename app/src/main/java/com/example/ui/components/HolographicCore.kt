package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VoiceState
import com.example.ui.theme.MyraCyberCyan
import com.example.ui.theme.MyraMatrixGreen
import com.example.ui.theme.MyraNeonGreen
import kotlin.math.sin

@Composable
fun HolographicCore(
    voiceState: VoiceState,
    audioRms: Float,
    onCoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")

    // Rotation angle for outer cyber ring
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse scale for core glow
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = when (voiceState) {
        VoiceState.LISTENING -> MyraMatrixGreen
        VoiceState.PROCESSING -> MyraCyberCyan
        VoiceState.SPEAKING -> MyraNeonGreen
        VoiceState.IDLE -> MyraNeonGreen.copy(alpha = 0.8f)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .testTag("myra_holographic_core"),
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas: Cybernetic Rings and Radar Scan
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.minDimension / 2) - 8.dp.toPx()

                // Outer Dashed Orbital Ring
                drawCircle(
                    color = coreColor.copy(alpha = 0.25f),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), ringRotation)
                    )
                )

                // Middle Thin Glow Ring
                drawCircle(
                    color = MyraCyberCyan.copy(alpha = 0.35f),
                    radius = radius * 0.82f,
                    center = center,
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), -ringRotation * 1.5f)
                    )
                )

                // Inner Pulse Wave
                val waveRadius = radius * 0.65f * (if (voiceState == VoiceState.LISTENING) (0.9f + audioRms * 0.3f) else pulseGlow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(coreColor.copy(alpha = 0.3f), Color.Transparent),
                        center = center,
                        radius = waveRadius
                    ),
                    radius = waveRadius,
                    center = center
                )
            }

            // Interactive Center Orb
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                coreColor.copy(alpha = 0.85f),
                                Color(0xFF041A0E)
                            )
                        )
                    )
                    .border(2.dp, coreColor, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onCoreClick
                    )
                    .testTag("voice_trigger_orb"),
                contentAlignment = Alignment.Center
            ) {
                when (voiceState) {
                    VoiceState.LISTENING -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Listening",
                            tint = Color(0xFF021B0C),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    VoiceState.SPEAKING -> {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Speaking",
                            tint = Color(0xFF021B0C),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    VoiceState.PROCESSING -> {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Processing",
                            tint = Color(0xFF021B0C),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    VoiceState.IDLE -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Tap to speak",
                            tint = Color(0xFF021B0C),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Futuristic 16-Bar Audio Waveform Visualizer
        AudioWaveformVisualizer(
            voiceState = voiceState,
            audioRms = audioRms,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(28.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // State indicator text
        val statusLabel = when (voiceState) {
            VoiceState.LISTENING -> "MYRA LISTENING • সংবেদনশীল"
            VoiceState.PROCESSING -> "NEURAL PROCESSING • বিশ্লেষণ চলছে"
            VoiceState.SPEAKING -> "MYRA RESPONDING • কণ্ঠবার্তা"
            VoiceState.IDLE -> "MYRA CORE ONLINE • নির্দেশ দিন"
        }

        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = coreColor
        )
    }
}

@Composable
fun AudioWaveformVisualizer(
    voiceState: VoiceState,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 18
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val baseHeight = when (voiceState) {
                VoiceState.LISTENING -> {
                    val sineFactor = (sin(phase + i * 0.45f) + 1f) / 2f
                    (0.2f + (audioRms * 0.8f * sineFactor)).coerceIn(0.15f, 1f)
                }
                VoiceState.SPEAKING -> {
                    val sineFactor = (sin(phase * 1.5f + i * 0.6f) + 1f) / 2f
                    (0.3f + 0.65f * sineFactor).coerceIn(0.2f, 1f)
                }
                VoiceState.PROCESSING -> {
                    val wave = (sin(phase * 2f + i * 0.3f) + 1f) / 2f
                    (0.15f + 0.35f * wave)
                }
                VoiceState.IDLE -> {
                    val ambientWave = (sin(phase * 0.5f + i * 0.3f) + 1f) / 2f
                    (0.1f + 0.15f * ambientWave)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .drawWithCache {
                        val barHeight = size.height * baseHeight
                        val top = (size.height - barHeight) / 2f
                        onDrawBehind {
                            drawRoundRect(
                                color = if (i % 2 == 0) MyraNeonGreen else MyraCyberCyan,
                                topLeft = Offset(size.width * 0.2f, top),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.6f, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
            )
        }
    }
}
