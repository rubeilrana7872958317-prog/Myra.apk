package com.example.service

import com.example.BuildConfig
import com.example.data.local.entities.MemoryEntity
import com.example.data.remote.Content
import com.example.data.remote.GeminiClient
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Part
import com.example.data.repository.MyraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class AiExecutionResult(
    val replyText: String,
    val actionType: String = "CHAT",
    val executedAction: String? = null,
    val detectedLanguage: String = "en"
)

class MyraAiEngine(
    private val repository: MyraRepository,
    private val automationManager: DeviceAutomationManager,
    private val audioSynthesizer: AudioSynthesizer
) {
    suspend fun processQuery(
        rawQuery: String,
        userPersonality: String = "Tactical Cyberpunk",
        defaultLanguage: String = "en"
    ): AiExecutionResult = withContext(Dispatchers.IO) {
        val query = rawQuery.trim()
        val lower = query.lowercase(Locale.ROOT)
        val detectedLang = detectLanguage(query, defaultLanguage)

        // 1. Direct Command & Automation Matchers
        // Flashlight / Torch
        if (lower.contains("flashlight") || lower.contains("torch") || lower.contains("টর্চ") || lower.contains("ফ্ল্যাশ") || lower.contains("টর্চলাইট")) {
            val turnOff = lower.contains("off") || lower.contains("বন্ধ") || lower.contains("নিভাও") || lower.contains("बंद")
            val newState = automationManager.toggleFlashlight(!turnOff)
            audioSynthesizer.playBeepConfirmation()
            val reply = when (detectedLang) {
                "bn" -> if (newState) "টর্চলাইট চালু করা হয়েছে।" else "টর্চলাইট বন্ধ করা হয়েছে।"
                "hi" -> if (newState) "टॉर्च चालू कर दी गई है।" else "टॉर्च बंद कर दी गई है।"
                else -> if (newState) "Flashlight activated." else "Flashlight deactivated."
            }
            return@withContext AiExecutionResult(reply, "AUTOMATION", "FLASHLIGHT", detectedLang)
        }

        // Emergency SOS / Siren
        if (lower == "sos" || lower.contains("emergency") || lower.contains("জরুরি") || lower.contains("বাঁচাও") || lower.contains("সাহায্য") || lower.contains("खतरा") || lower.contains("मदद")) {
            audioSynthesizer.startSiren()
            automationManager.startStrobe()
            automationManager.triggerVibration(1000)
            val reply = when (detectedLang) {
                "bn" -> "⚠️ সতর্কবার্তা! জরুরি এসওএস সাইরেন ও স্ট্রোব সক্রিয় করা হয়েছে।"
                "hi" -> "⚠️ आपातकालीन एसओएस सायरन और स्ट्रोब सक्रिय कर दिया गया है!"
                else -> "⚠️ CRITICAL ALERT! Emergency SOS Siren & Strobe Beacon Activated."
            }
            return@withContext AiExecutionResult(reply, "SOS", "EMERGENCY_ALARM", detectedLang)
        }

        // Stop Siren
        if (lower.contains("stop siren") || lower.contains("সাইরেন বন্ধ") || lower.contains("সাউন্ড বন্ধ") || lower.contains("अलार्म बंद")) {
            audioSynthesizer.stopSiren()
            automationManager.stopStrobe()
            val reply = when (detectedLang) {
                "bn" -> "জরুরি সাইরেন নিষ্ক্রিয় করা হয়েছে।"
                "hi" -> "आपातकालीन सायरन बंद कर दिया गया है।"
                else -> "Emergency siren deactivated."
            }
            return@withContext AiExecutionResult(reply, "SOS", "STOP_SIREN", detectedLang)
        }

        // Music / Cyber Ambient Synth
        if (lower.contains("play music") || lower.contains("play sound") || lower.contains("গান বাজাও") || lower.contains("গান চালাও") || lower.contains("গান শুরু") || lower.contains("संगीत बजाओ")) {
            audioSynthesizer.startAmbientSynth()
            val reply = when (detectedLang) {
                "bn" -> "🎵 সাইবার অ্যাম্বিয়েন্ট সিন্থ প্লেয়ার চালু করা হয়েছে।"
                "hi" -> "🎵 साइबर एंबिएंट संगीत शुरू किया गया है।"
                else -> "🎵 Cyber Synth Ambient Audio Matrix Online."
            }
            return@withContext AiExecutionResult(reply, "MEDIA", "PLAY_AMBIENT", detectedLang)
        }

        if (lower.contains("stop music") || lower.contains("pause music") || lower.contains("গান বন্ধ") || lower.contains("মিউজিক অফ") || lower.contains("संगीत बंद")) {
            audioSynthesizer.stopAmbientSynth()
            val reply = when (detectedLang) {
                "bn" -> "মিউজিক বন্ধ করা হয়েছে।"
                "hi" -> "संगीत बंद कर दिया गया है।"
                else -> "Ambient audio stopped."
            }
            return@withContext AiExecutionResult(reply, "MEDIA", "STOP_AMBIENT", detectedLang)
        }

        // Battery / Telemetry
        if (lower.contains("battery") || lower.contains("ব্যাটারি") || lower.contains("চার্জ") || lower.contains("बैटरी")) {
            val level = automationManager.getBatteryLevel()
            val (availMb, totalMb) = automationManager.getMemoryStats()
            audioSynthesizer.playBeepConfirmation()
            val reply = when (detectedLang) {
                "bn" -> "🔋 ব্যাটারি চার্জ: $level%। ফ্রি র্যাম: $availMb MB / $totalMb MB। সিস্টেম স্ট্যাটাস: অপ্টিমাল।"
                "hi" -> "🔋 बैटरी स्तर: $level%। उपलब्ध रैम: $availMb MB। सिस्टम स्थिति: सामान्य।"
                else -> "🔋 Telemetry Status: Battery at $level%. Free Memory: $availMb MB / $totalMb MB. Core: Online."
            }
            return@withContext AiExecutionResult(reply, "TELEMETRY", "BATTERY_CHECK", detectedLang)
        }

        // App Launching ("open camera", "open youtube", "ইউটিউব খোলো", "कैमरा खोलो")
        if (lower.startsWith("open ") || lower.contains("খোলো") || lower.contains("खोलो") || lower.contains("launch ")) {
            val targetApp = lower
                .replace("open ", "")
                .replace("launch ", "")
                .replace("খোলো", "")
                .replace("खोलो", "")
                .trim()
            if (targetApp.isNotEmpty()) {
                val launchResult = automationManager.launchAppByName(targetApp)
                audioSynthesizer.playBeepConfirmation()
                return@withContext AiExecutionResult(launchResult, "APP_LAUNCH", targetApp, detectedLang)
            }
        }

        // WhatsApp Direct ("whatsapp to 017... hello", "হোয়াটসঅ্যাপ")
        if (lower.contains("whatsapp") || lower.contains("হোয়াটসঅ্যাপ") || lower.contains("व्हाट्सएप")) {
            val phoneRegex = Regex("[0-9+]{8,15}")
            val match = phoneRegex.find(query)
            if (match != null) {
                val phone = match.value
                val msg = query.substringAfter(phone).trim()
                automationManager.openWhatsAppChat(phone, msg)
                val reply = when (detectedLang) {
                    "bn" -> "হোয়াটসঅ্যাপ চ্যাট খোলা হচ্ছে: $phone"
                    "hi" -> "व्हाट्सएप चैट खोली जा रही है: $phone"
                    else -> "Opening WhatsApp conversation with $phone"
                }
                return@withContext AiExecutionResult(reply, "CONNECT", "WHATSAPP", detectedLang)
            }
        }

        // Call Dialer ("call 911", "ফোন করো 01...")
        if (lower.startsWith("call ") || lower.startsWith("dial ") || lower.contains("ফোন করো") || lower.contains("कॉल करो")) {
            val phoneRegex = Regex("[0-9+]{3,15}")
            val match = phoneRegex.find(query)
            if (match != null) {
                val phone = match.value
                automationManager.openDialer(phone)
                val reply = when (detectedLang) {
                    "bn" -> "ডায়ালার খোলা হচ্ছে: $phone"
                    "hi" -> "डायल किया जा रहा है: $phone"
                    else -> "Opening dialer for $phone"
                }
                return@withContext AiExecutionResult(reply, "CONNECT", "CALL", detectedLang)
            }
        }

        // Google / Web Search ("search ...", "google ...", "খোঁজ করো")
        if (lower.startsWith("search ") || lower.startsWith("google ") || lower.startsWith("খোঁজ করো ") || lower.startsWith("खोजो ")) {
            val searchQuery = query
                .removePrefix("search ")
                .removePrefix("Search ")
                .removePrefix("google ")
                .removePrefix("Google ")
                .removePrefix("খোঁজ করো ")
                .removePrefix("खोजो ")
                .trim()
            automationManager.openWebSearch(searchQuery)
            val reply = when (detectedLang) {
                "bn" -> "গুগল সার্চ খোলা হচ্ছে: '$searchQuery'"
                "hi" -> "गूगल सर्च खोला जा रहा है: '$searchQuery'"
                else -> "Dispatching web search for: '$searchQuery'"
            }
            return@withContext AiExecutionResult(reply, "WEB_SEARCH", searchQuery, detectedLang)
        }

        // Timer setting ("timer 5 minutes", "৫ মিনিটের টাইমার")
        if (lower.contains("timer") || lower.contains("টাইমার") || lower.contains("टाइमर")) {
            val numRegex = Regex("\\d+")
            val num = numRegex.find(query)?.value?.toIntOrNull() ?: 5
            val seconds = num * 60
            automationManager.setSystemTimer(seconds, "MYRA $num Min Timer")
            audioSynthesizer.playBeepConfirmation()
            val reply = when (detectedLang) {
                "bn" -> "$num মিনিটের টাইমার নির্ধারণ করা হয়েছে।"
                "hi" -> "$num मिनट का टाइमर सेट किया गया है।"
                else -> "Timer initialized for $num minutes ($seconds seconds)."
            }
            return@withContext AiExecutionResult(reply, "TIMER", "SET_TIMER", detectedLang)
        }

        // Memory Store ("remember that ...", "মনে রাখো ...", "याद रखो ...", "my name is ...")
        if (lower.startsWith("remember ") || lower.contains("মনে রাখো") || lower.contains("याद रखो") || lower.startsWith("my name is ") || lower.contains("আমার নাম ")) {
            val fact = query
                .replace("remember that ", "", ignoreCase = true)
                .replace("remember ", "", ignoreCase = true)
                .replace("মনে রাখো ", "")
                .replace("याद रखो ", "")
                .trim()
            repository.saveMemory("FACT_${System.currentTimeMillis()}", fact, "USER_NOTE")
            audioSynthesizer.playBeepConfirmation()
            val reply = when (detectedLang) {
                "bn" -> "মায়রা মেমোরিতে সংরক্ষিত হয়েছে: '$fact'"
                "hi" -> "मायरा की स्मृति में सहेज लिया गया है: '$fact'"
                else -> "Memory stored into MYRA Neural Database: '$fact'"
            }
            return@withContext AiExecutionResult(reply, "MEMORY", "STORE_MEMORY", detectedLang)
        }

        // Memory Recall ("what do you remember", "what is my name", "আমার নাম কি")
        if (lower.contains("my name") || lower.contains("আমার নাম") || lower.contains("मेरा नाम") || lower.contains("memories") || lower.contains("মেমোরি")) {
            val memories = repository.getAllMemoriesDirect()
            if (memories.isNotEmpty()) {
                val listStr = memories.take(3).joinToString("; ") { it.value }
                val reply = when (detectedLang) {
                    "bn" -> "আমার মেমোরিতে পাওয়া তথ্য: $listStr"
                    "hi" -> "मेरी स्मृति में जानकारी: $listStr"
                    else -> "Retrieved from MYRA memory bank: $listStr"
                }
                return@withContext AiExecutionResult(reply, "MEMORY", "RECALL_MEMORY", detectedLang)
            }
        }

        // 2. Intelligent AI Reasoning (Online Gemini API or Offline High-Tech Persona Fallback)
        val memories = repository.getAllMemoriesDirect()
        val memoryContext = if (memories.isNotEmpty()) {
            "User Facts in Memory: " + memories.take(5).joinToString(", ") { "${it.key}: ${it.value}" }
        } else ""

        val systemPrompt = "You are MYRA, a futuristic high-tech AI Voice Assistant with a green cyberpunk aesthetic. " +
                "Personality: $userPersonality. " +
                "You assist with device automation, scheduling, knowledge, and system operations. " +
                "Answer concisely in the same language as the user query (Bengali if query is Bengali, Hindi if query is Hindi, English if query is English). " +
                memoryContext

        val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (geminiKey.isNotBlank() && !geminiKey.contains("MY_GEMINI_API_KEY") && geminiKey != "null") {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = query)))
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )
                val response = GeminiClient.service.generateContent(geminiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return@withContext AiExecutionResult(text.trim(), "AI_REASONING", "GEMINI_API", detectedLang)
                }
            } catch (_: Exception) {
                // Network error or invalid key -> proceed to offline high-tech fallback
            }
        }

        // 3. Fallback High-Tech Multilingual AI Core Response
        val fallbackReply = generateFallbackResponse(query, detectedLang, userPersonality, memories)
        AiExecutionResult(fallbackReply, "AI_CORE", "LOCAL_CORE", detectedLang)
    }

    private fun detectLanguage(text: String, defaultLang: String): String {
        for (char in text) {
            val code = char.code
            // Bengali Unicode range: 0x0980 to 0x09FF
            if (code in 0x0980..0x09FF) return "bn"
            // Devanagari (Hindi) Unicode range: 0x0900 to 0x097F
            if (code in 0x0900..0x097F) return "hi"
        }
        return defaultLang
    }

    private fun generateFallbackResponse(
        query: String,
        lang: String,
        personality: String,
        memories: List<MemoryEntity>
    ): String {
        val lower = query.lowercase(Locale.ROOT)

        if (lower.contains("who are you") || lower.contains("কে তুমি") || lower.contains("तुम कौन हो") || lower.contains("your name") || lower.contains("মায়রা কে")) {
            return when (lang) {
                "bn" -> "আমি মায়রা (MYRA) — আপনার ভবিষ্যৎমুখী এআই ভয়েস অ্যাসিস্ট্যান্ট। আমি ভয়েস কমান্ড, ডিভাইস নিয়ন্ত্রণ, অ্যাপ লঞ্চ, নিরাপত্তা সাইরেন এবং ফাইল পরিচালনা করতে পারি।"
                "hi" -> "मैं मायरा (MYRA) हूँ — आपकी भविष्यवादी एआई वॉयस असिस्टेंट। मैं आवाज पहचान, डिवाइस नियंत्रण, आपातकालीन एसओएस और त्वरित कार्यों में सक्षम हूँ।"
                else -> "I am MYRA — Multilingual Cybernetic Voice & Automation Assistant. Systems calibrated, neural memory operational, ready for your commands."
            }
        }

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("নমস্কার") || lower.contains("সালাম") || lower.contains("হ্যালো") || lower.contains("नमस्ते")) {
            return when (lang) {
                "bn" -> "হ্যালো! মায়রা সিস্টেম সক্রিয়। বলুন, আপনাকে কীভাবে সাহায্য করতে পারি?"
                "hi" -> "नमस्ते! मायरा सिस्टम ऑनलाइन है। बताइए, मैं आपकी क्या सहायता कर सकती हूँ?"
                else -> "Greetings, Commander. MYRA neural interface is online and synchronized. What is your objective?"
            }
        }

        if (lower.contains("weather") || lower.contains("আবহাওয়া") || lower.contains("मौसम")) {
            return when (lang) {
                "bn" -> "আজকের আবহাওয়ার তথ্য সংগ্রহের জন্য সরাসরি ওয়েব সার্চ বা জিপিএস ট্র্যাকার ব্যবহার করতে পারেন।"
                "hi" -> "वर्तमान मौसम की जांच के लिए आप वेब सर्च खोल सकते हैं।"
                else -> "Atmospheric sensors calibrated. For local live forecast, say 'Search weather today' to load real-time meteorological data."
            }
        }

        // Generic intelligent response
        return when (lang) {
            "bn" -> "আপনার নির্দেশ গ্রহণ করা হয়েছে: '$query'। মায়রা সিস্টেম আপনার সেবায় প্রস্তুত। আরও বিস্তারিত জানতে ভয়েস বা টেক্সটে কমান্ড দিন।"
            "hi" -> "आपका इनपुट प्राप्त हुआ: '$query'। मायरा सभी प्रणालियों के साथ तैयार है। आदेश दें।"
            else -> "Command acknowledged: '$query'. MYRA core diagnostics optimal. For specific actions, use voice prompts, app triggers, or connect tools."
        }
    }
}
