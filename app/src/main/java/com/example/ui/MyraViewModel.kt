package com.example.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.MyraDatabase
import com.example.data.local.entities.ClipboardEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.FileEntity
import com.example.data.local.entities.MemoryEntity
import com.example.data.repository.MyraRepository
import com.example.service.AudioSynthesizer
import com.example.service.DeviceAutomationManager
import com.example.service.InstalledAppInfo
import com.example.service.MyraAiEngine
import com.example.service.VoiceAssistantManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VoiceState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

data class UiTelemetry(
    val batteryPercent: Int = 85,
    val freeRamMb: Long = 2048,
    val totalRamMb: Long = 4096,
    val isFlashlightActive: Boolean = false,
    val isAmbientActive: Boolean = false,
    val isSirenActive: Boolean = false,
    val currentVolume: Float = 0.5f
)

class MyraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MyraDatabase.getDatabase(application)
    val repository = MyraRepository(database.myraDao())
    val automationManager = DeviceAutomationManager(application)
    val audioSynthesizer = AudioSynthesizer()
    private val aiEngine = MyraAiEngine(repository, automationManager, audioSynthesizer)

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _telemetry = MutableStateFlow(UiTelemetry())
    val telemetry: StateFlow<UiTelemetry> = _telemetry.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _language = MutableStateFlow("bn") // Default to Bengali as requested
    val language: StateFlow<String> = _language.asStateFlow()

    private val _personality = MutableStateFlow("Tactical Cyberpunk")
    val personality: StateFlow<String> = _personality.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(0)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // Room DB Flow Streams
    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.memories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val files: StateFlow<List<FileEntity>> = repository.files
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clipboardHistory: StateFlow<List<ClipboardEntity>> = repository.clipboardHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var voiceAssistant: VoiceAssistantManager? = null

    init {
        initVoiceAssistant()
        refreshTelemetry()
        loadInstalledApps()
        seedInitialFilesAndMemories()
    }

    private fun initVoiceAssistant() {
        voiceAssistant = VoiceAssistantManager(
            context = getApplication(),
            onListeningStateChange = { isListening ->
                _voiceState.value = if (isListening) VoiceState.LISTENING else VoiceState.IDLE
            },
            onRmsChange = { rms ->
                _audioRms.value = rms
            },
            onResult = { recognizedText ->
                _transcript.value = recognizedText
                handleUserQuery(recognizedText, isVoice = true)
            },
            onError = { _ ->
                _voiceState.value = VoiceState.IDLE
                _audioRms.value = 0f
            },
            onSpeakingStateChange = { isSpeaking ->
                _voiceState.value = if (isSpeaking) VoiceState.SPEAKING else VoiceState.IDLE
                if (isSpeaking) _audioRms.value = 0.85f else _audioRms.value = 0f
            }
        )
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        voiceAssistant?.updateTtsLanguage(lang)
    }

    fun setPersonality(p: String) {
        _personality.value = p
    }

    fun startListening() {
        _transcript.value = ""
        voiceAssistant?.startListening(_language.value)
    }

    fun stopListening() {
        voiceAssistant?.stopListening()
    }

    fun stopSpeaking() {
        voiceAssistant?.stopSpeaking()
    }

    fun handleUserQuery(query: String, isVoice: Boolean = false) {
        if (query.isBlank()) return
        val currentLang = _language.value
        viewModelScope.launch {
            // Record user message
            repository.saveConversation("USER", query, if (isVoice) "VOICE" else "CHAT", currentLang)
            _voiceState.value = VoiceState.PROCESSING

            // Process query through AI Engine
            val result = aiEngine.processQuery(query, _personality.value, currentLang)

            // Record response
            repository.saveConversation("MYRA", result.replyText, result.actionType, result.detectedLanguage)
            _voiceState.value = VoiceState.IDLE

            // Speak response if voice interaction or auto-speak is preferred
            voiceAssistant?.speak(result.replyText, result.detectedLanguage)
            refreshTelemetry()
        }
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            val battery = automationManager.getBatteryLevel()
            val (avail, total) = automationManager.getMemoryStats()
            val vol = automationManager.getVolumePercent()
            _telemetry.value = UiTelemetry(
                batteryPercent = battery,
                freeRamMb = avail,
                totalRamMb = total,
                isFlashlightActive = automationManager.isFlashlightOn,
                isAmbientActive = audioSynthesizer.isAmbientPlaying,
                isSirenActive = audioSynthesizer.isSirenPlaying,
                currentVolume = vol
            )
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = automationManager.getInstalledApps()
        }
    }

    fun toggleFlashlight() {
        val state = automationManager.toggleFlashlight()
        refreshTelemetry()
    }

    fun toggleAmbientAudio() {
        if (audioSynthesizer.isAmbientPlaying) {
            audioSynthesizer.stopAmbientSynth()
        } else {
            audioSynthesizer.startAmbientSynth()
        }
        refreshTelemetry()
    }

    fun triggerSosAlert() {
        audioSynthesizer.startSiren()
        automationManager.startStrobe()
        automationManager.triggerVibration(1500)
        refreshTelemetry()
        viewModelScope.launch {
            repository.saveConversation(
                "MYRA",
                "⚠️ EMERGENCY SOS ACTIVATED! Siren and Beacon Beacon Active.",
                "SOS",
                _language.value
            )
        }
    }

    fun stopSosAlert() {
        audioSynthesizer.stopSiren()
        automationManager.stopStrobe()
        refreshTelemetry()
    }

    fun setVolume(percent: Float) {
        automationManager.setVolume(percent)
        refreshTelemetry()
    }

    fun copyToClipboard(text: String) {
        automationManager.copyToClipboard("MYRA", text)
        viewModelScope.launch {
            repository.saveClipboard(text)
        }
    }

    // In-app Countdown Timer
    fun startTimer(minutes: Int) {
        timerJob?.cancel()
        _timerRemainingSeconds.value = minutes * 60
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timerRemainingSeconds.value > 0) {
                delay(1000)
                _timerRemainingSeconds.value -= 1
            }
            _isTimerRunning.value = false
            audioSynthesizer.playBeepConfirmation()
            voiceAssistant?.speak("Timer completed!", _language.value)
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _timerRemainingSeconds.value = 0
    }

    // File Vault Operations
    fun saveFile(fileName: String, content: String, type: String = "TXT") {
        viewModelScope.launch {
            repository.saveFile(fileName, content, type)
        }
    }

    fun deleteFile(id: Int) {
        viewModelScope.launch {
            repository.deleteFile(id)
        }
    }

    // Memory Operations
    fun saveMemory(key: String, value: String, category: String = "GENERAL") {
        viewModelScope.launch {
            repository.saveMemory(key, value, category)
        }
    }

    fun deleteMemory(id: Int) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearConversations()
        }
    }

    private fun seedInitialFilesAndMemories() {
        viewModelScope.launch {
            val existing = repository.getAllMemoriesDirect()
            if (existing.isEmpty()) {
                repository.saveMemory("AI_IDENTITY", "MYRA High-Tech Cyber Assistant", "SYSTEM")
                repository.saveMemory("SUPPORTED_LANGUAGES", "Bengali, Hindi, English", "SYSTEM")
                repository.saveMemory("USER_STATUS", "Primary Commander", "USER_INFO")
                repository.saveFile("system_manifest.txt", "MYRA AI CORE V3.8\nSTATUS: ONLINE\nNEURAL MEMORY: ACTIVE\nAUDIO MATRIX: SYNCHRONIZED", "LOG")
                repository.saveFile("emergency_protocol.txt", "SOS SIREN: DUAL-TONE 600-1100Hz\nSTROBE FREQUENCY: 8.3Hz\nEMERGENCY DIAL: 112 / 999", "CONFIG")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioSynthesizer.release()
        automationManager.stopStrobe()
        voiceAssistant?.destroy()
    }
}
