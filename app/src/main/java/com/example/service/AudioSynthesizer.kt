package com.example.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var ambientJob: Job? = null
    private var sirenJob: Job? = null

    var isAmbientPlaying: Boolean = false
        private set

    var isSirenPlaying: Boolean = false
        private set

    fun startAmbientSynth(onStateChange: (Boolean) -> Unit = {}) {
        if (isAmbientPlaying) return
        stopSiren()
        isAmbientPlaying = true
        onStateChange(true)

        ambientJob = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack.play()
            val shortBuffer = ShortArray(bufferSize)
            var phase1 = 0.0
            var phase2 = 0.0
            val freq1 = 110.0 // Deep sci-fi fundamental (A2)
            val freq2 = 220.5 // Subtle harmonic with beating pulse

            try {
                while (isActive && isAmbientPlaying) {
                    for (i in shortBuffer.indices) {
                        val sample1 = sin(phase1) * 0.4
                        val sample2 = sin(phase2) * 0.2
                        val combined = (sample1 + sample2) * 0.4
                        shortBuffer[i] = (combined * Short.MAX_VALUE).toInt().toShort()

                        phase1 += 2.0 * Math.PI * freq1 / sampleRate
                        phase2 += 2.0 * Math.PI * freq2 / sampleRate
                        if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                        if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                    }
                    audioTrack.write(shortBuffer, 0, shortBuffer.size)
                }
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
                isAmbientPlaying = false
                onStateChange(false)
            }
        }
    }

    fun stopAmbientSynth(onStateChange: (Boolean) -> Unit = {}) {
        isAmbientPlaying = false
        ambientJob?.cancel()
        ambientJob = null
        onStateChange(false)
    }

    fun startSiren(onStateChange: (Boolean) -> Unit = {}) {
        if (isSirenPlaying) return
        stopAmbientSynth()
        isSirenPlaying = true
        onStateChange(true)

        sirenJob = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack.play()
            val buffer = ShortArray(bufferSize)
            var phase = 0.0
            var sampleCount = 0L

            try {
                while (isActive && isSirenPlaying) {
                    for (i in buffer.indices) {
                        // Modulate frequency between 600 Hz and 1100 Hz every 0.6 seconds
                        val cycle = (sampleCount % (sampleRate * 6 / 10)).toDouble() / (sampleRate * 6 / 10)
                        val currentFreq = 600.0 + 500.0 * sin(cycle * Math.PI)
                        val sample = sin(phase) * 0.95
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()

                        phase += 2.0 * Math.PI * currentFreq / sampleRate
                        if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                        sampleCount++
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
                isSirenPlaying = false
                onStateChange(false)
            }
        }
    }

    fun stopSiren(onStateChange: (Boolean) -> Unit = {}) {
        isSirenPlaying = false
        sirenJob?.cancel()
        sirenJob = null
        onStateChange(false)
    }

    fun playBeepConfirmation() {
        scope.launch {
            val sampleRate = 44100
            val durationMs = 120
            val totalSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(totalSamples)
            var phase = 0.0
            val freq = 880.0

            for (i in 0 until totalSamples) {
                val envelope = 1.0 - (i.toDouble() / totalSamples) // Smooth fade out
                val sample = sin(phase) * 0.5 * envelope
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                phase += 2.0 * Math.PI * freq / sampleRate
            }

            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
            } catch (_: Exception) {}
        }
    }

    fun release() {
        stopAmbientSynth()
        stopSiren()
    }
}
