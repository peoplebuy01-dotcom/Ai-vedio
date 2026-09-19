package com.example.pipeline

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.data.model.Language
import com.example.data.model.VoiceGender
import java.util.Locale

data class TimedWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

data class SceneSubtitleData(
    val sceneIndex: Int,
    val fullText: String,
    val speaker: String?,
    val words: List<TimedWord>,
    val durationSeconds: Int
)

class VoiceoverEngine(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            tts = TextToSpeech(context) { status ->
                isTtsReady = (status == TextToSpeech.SUCCESS)
            }
        } catch (_: Exception) {
            isTtsReady = false
        }
    }

    fun configureVoice(language: Language, gender: VoiceGender) {
        if (!isTtsReady || tts == null) return
        try {
            val locale = when (language) {
                Language.HINDI -> Locale("hi", "IN")
                Language.GUJARATI -> Locale("gu", "IN")
                Language.SPANISH -> Locale("es", "ES")
                Language.JAPANESE -> Locale.JAPANESE
                else -> Locale.US
            }
            tts?.language = locale
            tts?.setSpeechRate(if (gender == VoiceGender.MALE) 0.95f else 1.0f)
            tts?.setPitch(if (gender == VoiceGender.MALE) 0.85f else 1.15f)
        } catch (_: Exception) {}
    }

    fun speakNarration(text: String) {
        if (!isTtsReady || tts == null) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nexora_narration")
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }

    companion object {
        fun generateWordTimings(
            sceneIndex: Int,
            narration: String,
            speaker: String?,
            dialogue: String?,
            sceneDurationSec: Int
        ): SceneSubtitleData {
            val combinedText = if (dialogue != null) {
                "${speaker ?: "Character"}: \"$dialogue\" — $narration"
            } else {
                narration
            }

            val words = combinedText.split(Regex("\\s+")).filter { it.isNotBlank() }
            val totalMs = (sceneDurationSec * 1000L).coerceAtLeast(3000L)
            val msPerWord = (totalMs / (words.size.coerceAtLeast(1))).coerceAtLeast(150L)

            val timedWords = words.mapIndexed { index, word ->
                val start = index * msPerWord
                val end = (index + 1) * msPerWord
                TimedWord(word, start, end)
            }

            return SceneSubtitleData(
                sceneIndex = sceneIndex,
                fullText = combinedText,
                speaker = speaker,
                words = timedWords,
                durationSeconds = sceneDurationSec
            )
        }
    }
}
