package com.example.service

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable? = null
)

class DeviceAutomationManager(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val scope = CoroutineScope(Dispatchers.Default)

    private var cameraId: String? = null
    var isFlashlightOn: Boolean = false
        private set

    private var strobeJob: Job? = null

    init {
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (_: Exception) {}
    }

    // --- Flashlight ---
    fun toggleFlashlight(enable: Boolean? = null): Boolean {
        stopStrobe()
        val target = enable ?: !isFlashlightOn
        return try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, target)
                isFlashlightOn = target
                target
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun startStrobe() {
        stopStrobe()
        strobeJob = scope.launch {
            val id = cameraId ?: return@launch
            var state = false
            try {
                while (isActive) {
                    state = !state
                    try {
                        cameraManager?.setTorchMode(id, state)
                        isFlashlightOn = state
                    } catch (_: Exception) {}
                    delay(120)
                }
            } finally {
                try {
                    cameraManager?.setTorchMode(id, false)
                    isFlashlightOn = false
                } catch (_: Exception) {}
            }
        }
    }

    fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        if (isFlashlightOn) {
            try {
                cameraId?.let { cameraManager?.setTorchMode(it, false) }
            } catch (_: Exception) {}
            isFlashlightOn = false
        }
    }

    // --- Audio Controls ---
    fun setVolume(percent: Float) {
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val target = (percent.coerceIn(0f, 1f) * max).toInt()
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
    }

    fun getVolumePercent(): Float {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        return (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    }

    fun adjustVolume(direction: Int) {
        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    // --- Vibration ---
    fun triggerVibration(durationMs: Long = 100) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    // --- Telemetry ---
    fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
    }

    fun getMemoryStats(): Pair<Long, Long> {
        // Returns (availableMB, totalMB)
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        val availMB = memInfo.availMem / (1024 * 1024)
        val totalMB = memInfo.totalMem / (1024 * 1024)
        return Pair(availMB, totalMB)
    }

    // --- Installed Apps ---
    fun getInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val appList = mutableListOf<InstalledAppInfo>()
        for (info in resolveInfos) {
            val appName = info.loadLabel(pm).toString()
            val packageName = info.activityInfo.packageName
            val icon = info.loadIcon(pm)
            appList.add(InstalledAppInfo(appName, packageName, icon))
        }
        return appList.sortedBy { it.appName.lowercase() }
    }

    fun launchAppByPackage(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun launchAppByName(name: String): String {
        val lower = name.lowercase().trim()
        val apps = getInstalledApps()
        val match = apps.firstOrNull { it.appName.lowercase().contains(lower) }
        return if (match != null) {
            val launched = launchAppByPackage(match.packageName)
            if (launched) "Opening ${match.appName}" else "Failed to open ${match.appName}"
        } else {
            "Application '$name' not found on device"
        }
    }

    // --- WhatsApp ---
    fun openWhatsAppChat(phone: String, message: String = ""): Boolean {
        return try {
            val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
            val url = if (message.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
            } else {
                "https://api.whatsapp.com/send?phone=$cleanPhone"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    // --- Phone & SMS ---
    fun openDialer(phone: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openSmsComposer(phone: String, body: String = ""): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(phone)}")).apply {
                putExtra("sms_body", body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    // --- Web Search ---
    fun openWebSearch(query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    // --- Alarm & Timer ---
    fun setSystemAlarm(hour: Int, minute: Int, message: String = "MYRA Alarm"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun setSystemTimer(seconds: Int, message: String = "MYRA Timer"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    // --- Clipboard ---
    fun copyToClipboard(label: String, text: String): Boolean {
        return try {
            val clip = ClipData.newPlainText(label, text)
            clipboardManager?.setPrimaryClip(clip)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getClipboardText(): String? {
        val clip = clipboardManager?.primaryClip
        return if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).text?.toString()
        } else {
            null
        }
    }

    // --- System Settings ---
    fun openSettings(action: String = Settings.ACTION_SETTINGS): Boolean {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
