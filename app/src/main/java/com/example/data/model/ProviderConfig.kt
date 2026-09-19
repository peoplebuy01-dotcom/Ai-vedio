package com.example.data.model

enum class AIProvider(val displayName: String, val description: String, val supportsVideo: Boolean) {
    GEMINI_VEO("Google AI (Gemini & Veo)", "Fast multimodal story analysis, character generation, & Veo video synthesis", true),
    OPENAI("OpenAI (GPT-4o & Sora)", "High coherence narrative expansion & visual prompt crafting", true),
    ELEVENLABS("ElevenLabs Studio", "Hyper-realistic voice cloning & emotional multilingual narration", false),
    GOOGLE_TTS("Google Cloud TTS", "Ultra low-latency multilingual speech synthesis with Hindi & Gujarati", false),
    LOCAL_NEURAL("NEXORA Neural Engine", "High-speed built-in cinematic video synthesizer & offline preview", true)
}

data class ProviderSettings(
    val activeProvider: AIProvider = AIProvider.GEMINI_VEO,
    val geminiApiKey: String = "",
    val openAiApiKey: String = "",
    val elevenLabsApiKey: String = "",
    val googleTtsApiKey: String = "",
    val resolution: String = "1080p (Full HD)",
    val frameRate: Int = 30,
    val subtitleFontSize: Int = 18,
    val subtitleStyle: String = "Karaoke Glow",
    val autoRetryFailedScenes: Boolean = true,
    val enableCostSavingCache: Boolean = true
)
