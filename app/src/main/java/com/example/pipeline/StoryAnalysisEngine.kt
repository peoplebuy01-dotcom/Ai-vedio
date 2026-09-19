package com.example.pipeline

import com.example.data.model.*
import kotlin.math.roundToInt

data class StoryAnalysisResult(
    val expandedTitle: String,
    val storySummary: String,
    val genre: String,
    val mood: String,
    val timePeriod: String,
    val primaryLocation: String,
    val keyObjects: List<String>,
    val musicRequirement: String,
    val targetScenesCount: Int,
    val calculatedSceneDuration: Int,
    val characterPrototypes: List<CharacterPrototype>,
    val actBreakdown: List<String>
)

data class CharacterPrototype(
    val name: String,
    val role: String,
    val age: String,
    val voiceType: String,
    val visualKeyword: String
)

object StoryAnalysisEngine {

    fun calculateScenesCount(durationMinutes: Int): Int {
        // Average scene is ~9 seconds
        // 1 min = ~7 scenes
        // 3 min = ~20 scenes
        // 5 min = ~34 scenes
        // 10 min = ~66 scenes
        // 15 min = ~100 scenes
        return when (durationMinutes) {
            1 -> 7
            3 -> 20
            5 -> 34
            10 -> 65
            15 -> 98
            else -> ((durationMinutes * 60) / 9.2).roundToInt().coerceIn(5, 120)
        }
    }

    fun analyzeStory(
        prompt: String,
        topic: String,
        durationMinutes: Int,
        style: VideoStyle,
        language: Language
    ): StoryAnalysisResult {
        val totalScenes = calculateScenesCount(durationMinutes)
        val cleanPrompt = if (prompt.isBlank()) topic.ifBlank { "Journey into the Unknown" } else prompt

        // Extract or synthesize narrative parameters
        val isSciFi = cleanPrompt.contains("space", true) || cleanPrompt.contains("future", true) || cleanPrompt.contains("ai", true) || cleanPrompt.contains("cyber", true)
        val isHorror = cleanPrompt.contains("ghost", true) || cleanPrompt.contains("dark", true) || cleanPrompt.contains("horror", true) || style == VideoStyle.HORROR
        val isDoc = cleanPrompt.contains("history", true) || cleanPrompt.contains("nature", true) || cleanPrompt.contains("documentary", true) || style == VideoStyle.DOCUMENTARY
        val isAction = cleanPrompt.contains("fight", true) || cleanPrompt.contains("mission", true) || cleanPrompt.contains("chase", true)

        val mood = when {
            isHorror -> "Suspenseful, eerie, chilling, atmospheric"
            isSciFi -> "Futuristic, awe-inspiring, mysterious, philosophical"
            isDoc -> "Reflective, educational, grand, inspiring"
            isAction -> "High-stakes, adrenaline-fueled, energetic, dramatic"
            else -> "Cinematic, emotional, deep, captivating"
        }

        val genre = when {
            isHorror -> "Supernatural Horror"
            isSciFi -> "Speculative Sci-Fi"
            isDoc -> "Documentary Odyssey"
            isAction -> "Cinematic Action Thriller"
            else -> "Epic Cinematic Drama"
        }

        val timePeriod = when {
            isSciFi -> "Neo-Terra Era, Year 2184"
            isHorror -> "An Overcast Autumn Midnight"
            isDoc -> "The Living Anthropocene"
            else -> "Present Day / Mythic Epoch"
        }

        val primaryLocation = when {
            isSciFi -> "Orbital Gateway NEXUS-7 & Neon Undercity"
            isHorror -> "The Whispering Pines Manor & Misty Ravine"
            isDoc -> "Ancient Glacier Valleys & Subterranean Caverns"
            else -> "The Skyline Meridian & The Hidden Observatory"
        }

        val keyObjects = when {
            isSciFi -> listOf("Quantum Core Datapad", "Holographic Neural Key", "Electromagnetic Beacon")
            isHorror -> listOf("Antique Brass Pocket Watch", "Sealed Obsidian Diary", "Flickering Lantern")
            else -> listOf("Ancient Astral Compass", "The Monolith Relic", "Silver Frequency Transmitter")
        }

        val musicRequirement = when {
            isHorror -> "Dark ambient drone with sudden dissonance and slow strings"
            isSciFi -> "Synthesized analog arpeggios layered with grand orchestral brass"
            isDoc -> "Organic acoustic guitar, ambient cello, and soft natural atmospheres"
            isAction -> "Pulsing industrial percussion, aggressive hybrid brass, and driving bassline"
            else -> "Cinematic emotional symphony with piano crescendo and strings"
        }

        // Generate Characters for Character Bible
        val characterPrototypes = when {
            isSciFi -> listOf(
                CharacterPrototype("Commander Kaelen Vance", "Protagonist / Chief Pilot", "34", "Deep resonant baritone, resolute", "Obsidian flight exoskeleton with cyan HUD visor"),
                CharacterPrototype("Dr. Lyra Chen", "Lead Quantum Physicist", "29", "Analytical, articulate, empathetic", "Smart-polymer silver lab tunic with optic ocular implants"),
                CharacterPrototype("AI Sentinel AETHEL", "Synthetic Companion", "Ageless", "Calm and ethereal harmonic modulation", "Holographic cerulean geometric avatar")
            )
            isHorror -> listOf(
                CharacterPrototype("Evelyn Ross", "Investigative Archivist", "31", "Soft, cautious, breathy tenor", "Wool charcoal coat, brass spectacle frames, leather satchel"),
                CharacterPrototype("Arthur Pendelton", "Caretaker of Whispering Pines", "62", "Gravelly weathered bass, trembling", "Torn tweed jacket, mud-stained boots, lantern in hand"),
                CharacterPrototype("The Shadow Presence", "Supernatural Entity", "Timeless", "Whispering echo of multiple voices", "Silhouetted vaporous mist with faint crimson eyes")
            )
            else -> listOf(
                CharacterPrototype("Aria Valen", "Pioneering Protagonist", "28", "Warm, determined, expressive", "Tactical sapphire expedition parka with magnetic utility harness"),
                CharacterPrototype("Marcus Drake", "Veteran Navigator", "45", "Deep steady cadence, weathered", "Rugged leather aeronaut jacket, bronze chronometer"),
                CharacterPrototype("The Chronicler", "Narrator / Historian", "55", "Authoritative, poetic, solemn", "Tailored slate-grey ensemble with golden insignia")
            )
        }

        val title = if (topic.isNotBlank()) topic else cleanPrompt.take(35).trim() + " — Cinematic Journey"

        val acts = listOf(
            "Act 1: Introduction of world, initial disquiet, and awakening",
            "Act 2: The escalating threshold, confrontation with the unknown, and revelation",
            "Act 3: The climactic crucible, resolution, and poignant cinematic finale"
        )

        return StoryAnalysisResult(
            expandedTitle = title,
            storySummary = "An epic $totalScenes-scene cinematic narrative exploring: '$cleanPrompt'. Engineered with character consistency, immersive soundscapes, and high-impact pacing.",
            genre = genre,
            mood = mood,
            timePeriod = timePeriod,
            primaryLocation = primaryLocation,
            keyObjects = keyObjects,
            musicRequirement = musicRequirement,
            targetScenesCount = totalScenes,
            calculatedSceneDuration = 9,
            characterPrototypes = characterPrototypes,
            actBreakdown = acts
        )
    }
}
