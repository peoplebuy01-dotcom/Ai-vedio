package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectStatus {
    DRAFT,
    ANALYZING,
    SCRIPTING,
    CHARACTERS,
    GENERATING_SCENES,
    VOICEOVER,
    MUSIC,
    SUBTITLES,
    RENDERING,
    COMPLETED,
    FAILED
}

enum class VideoStyle(val displayName: String, val promptModifier: String) {
    CINEMATIC("Cinematic", "hyper-cinematic 8k, anamorphic lens, shallow depth of field, blockbuster lighting, dramatic color grading"),
    REALISTIC("Realistic", "photorealistic 8k, natural daylight, uncompressed raw footage, hyper-detailed texture"),
    DOCUMENTARY("Documentary", "National Geographic documentary style, authentic archival capture, cinematic naturalism"),
    HORROR("Horror", "dark atmospheric shadows, moody suspenseful volumetric lighting, psychological thriller tone"),
    ANIME("Anime", "Makoto Shinkai anime aesthetic, vibrant atmospheric light rays, lush hand-drawn aesthetic"),
    SCI_FI_3D("3D Sci-Fi", "Unreal Engine 5 futuristic cyberpunk render, ray-traced neon reflections, volumetric haze")
}

enum class AspectRatio(val label: String, val ratio: Float, val isVertical: Boolean) {
    LANDSCAPE_16_9("16:9 Landscape", 16f / 9f, false),
    PORTRAIT_9_16("9:16 Vertical", 9f / 16f, true)
}

enum class VoiceGender {
    MALE,
    FEMALE
}

enum class Language(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    SPANISH("es", "Spanish", "Español"),
    JAPANESE("ja", "Japanese", "日本語")
}

enum class CameraMovement(val label: String) {
    PAN_LEFT("Pan Left"),
    PAN_RIGHT("Pan Right"),
    ZOOM_IN("Slow Zoom In"),
    ZOOM_OUT("Slow Zoom Out"),
    TILT_UP("Tilt Up"),
    TRACKING("Dynamic Tracking")
}

enum class TransitionType(val label: String) {
    CROSSFADE("Crossfade"),
    DISSOLVE("Dissolve"),
    DIP_TO_BLACK("Dip to Black"),
    ZOOM_IN("Zoom Transition"),
    SLIDE_LEFT("Slide Left")
}

enum class SceneStatus {
    PENDING,
    GENERATING,
    COMPLETED,
    FAILED
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val prompt: String,
    val topic: String,
    val durationMinutes: Int = 10,
    val language: String = Language.ENGLISH.name,
    val voiceGender: String = VoiceGender.MALE.name,
    val videoStyle: String = VideoStyle.CINEMATIC.name,
    val aspectRatio: String = AspectRatio.LANDSCAPE_16_9.name,
    val status: String = ProjectStatus.DRAFT.name,
    val progress: Int = 0,
    val currentStageText: String = "Ready to generate",
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val totalScenes: Int = 0,
    val completedScenes: Int = 0,
    val videoFilePath: String? = null,
    val thumbnailTitle: String? = null,
    val thumbnailSubtitle: String? = null,
    val thumbnailVisualSeed: Long = 1001L,
    val hasShorts: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val sceneIndex: Int,
    val durationSeconds: Int = 9,
    val visualDescription: String,
    val characterActions: String,
    val cameraMovement: String = CameraMovement.ZOOM_IN.name,
    val environment: String,
    val dialogueSpeaker: String? = null,
    val dialogueText: String? = null,
    val narrationText: String,
    val soundEffect: String,
    val backgroundMusicMood: String,
    val transition: String = TransitionType.CROSSFADE.name,
    val visualSeed: Long,
    val colorTonePrimary: Long = 0xFF0B192C,
    val colorToneSecondary: Long = 0xFF1E3E62,
    val status: String = SceneStatus.PENDING.name,
    val isCached: Boolean = false
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val role: String,
    val face: String,
    val hair: String,
    val clothes: String,
    val age: String,
    val bodyType: String,
    val skinTone: String,
    val accessories: String,
    val personality: String,
    val voiceCharacteristics: String,
    val avatarSeed: Long
)

data class GeneratedShort(
    val id: String,
    val projectId: Long,
    val title: String,
    val durationSeconds: Int, // 30 or 60
    val hookText: String,
    val selectedSceneIndices: List<Int>,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16
)
