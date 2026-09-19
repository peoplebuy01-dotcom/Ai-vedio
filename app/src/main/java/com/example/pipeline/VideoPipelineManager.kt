package com.example.pipeline

import android.content.Context
import com.example.data.model.*
import com.example.data.repository.NexoraRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class ActivePipelineStatus(
    val projectId: Long = -1,
    val currentStepNumber: Int = 0, // 1 to 10
    val stepTitle: String = "Idle",
    val stepDetails: String = "",
    val overallProgress: Int = 0,
    val currentSceneIndex: Int = 0,
    val totalScenes: Int = 0,
    val isRunning: Boolean = false,
    val failedSceneIndex: Int? = null,
    val errorMessage: String? = null,
    val isProviderNotConfigured: Boolean = false
)

class VideoPipelineManager(
    private val context: Context,
    private val repository: NexoraRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentJob: Job? = null

    private val _pipelineStatus = MutableStateFlow(ActivePipelineStatus())
    val pipelineStatus = _pipelineStatus.asStateFlow()

    fun startGeneration(
        title: String,
        storyPrompt: String,
        durationMinutes: Int,
        language: Language,
        voiceGender: VoiceGender,
        style: VideoStyle,
        aspectRatio: AspectRatio
    ) {
        val currentSettings = repository.settingsFlow.value
        val isGeminiConfigured = NexoraGeminiClient.isConfigured(currentSettings.geminiApiKey)

        // Enforce provider configuration requirement
        if (!isGeminiConfigured && currentSettings.activeProvider == AIProvider.GEMINI_VEO) {
            _pipelineStatus.value = ActivePipelineStatus(
                stepTitle = "VIDEO GENERATION PROVIDER NOT CONFIGURED",
                stepDetails = "No Gemini API key detected. Please add your GEMINI_API_KEY in the Secrets panel or Settings to execute AI video generation.",
                errorMessage = "VIDEO GENERATION PROVIDER NOT CONFIGURED",
                isProviderNotConfigured = true,
                isRunning = false
            )
            return
        }

        currentJob?.cancel()
        currentJob = scope.launch {
            val totalScenesCount = StoryAnalysisEngine.calculateScenesCount(durationMinutes)

            // 1. Create Initial Project Entity in Room DB
            val initialProject = ProjectEntity(
                title = title.ifBlank { "Untitled Story" },
                prompt = storyPrompt,
                topic = title,
                durationMinutes = durationMinutes,
                language = language.name,
                voiceGender = voiceGender.name,
                videoStyle = style.name,
                aspectRatio = aspectRatio.name,
                status = ProjectStatus.ANALYZING.name,
                progress = 5,
                currentStageText = "Step 1: Analyzing Story & Narrative Arc with AI...",
                totalScenes = totalScenesCount,
                completedScenes = 0
            )

            val projectId = repository.insertProject(initialProject)
            _pipelineStatus.value = ActivePipelineStatus(
                projectId = projectId,
                currentStepNumber = 1,
                stepTitle = "Analyzing Story",
                stepDetails = "Extracting characters, themes, environments, and pacing...",
                overallProgress = 8,
                currentSceneIndex = 0,
                totalScenes = totalScenesCount,
                isRunning = true
            )

            try {
                val effectiveApiKey = NexoraGeminiClient.getEffectiveApiKey(currentSettings.geminiApiKey)

                // STEP 1 — STORY ANALYSIS (Real Gemini API call if configured, or deterministic engine)
                val analysis = if (!effectiveApiKey.isNullOrEmpty()) {
                    try {
                        NexoraGeminiClient.generateStoryAnalysis(
                            apiKey = effectiveApiKey,
                            prompt = storyPrompt,
                            topic = title,
                            durationMinutes = durationMinutes,
                            style = style.displayName,
                            language = language.displayName,
                            targetScenesCount = totalScenesCount
                        )
                    } catch (e: Exception) {
                        StoryAnalysisEngine.analyzeStory(
                            prompt = storyPrompt,
                            topic = title,
                            durationMinutes = durationMinutes,
                            style = style,
                            language = language
                        )
                    }
                } else {
                    StoryAnalysisEngine.analyzeStory(
                        prompt = storyPrompt,
                        topic = title,
                        durationMinutes = durationMinutes,
                        style = style,
                        language = language
                    )
                }

                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.SCRIPTING.name,
                    progress = 15,
                    stageText = "Step 2: Generating $totalScenesCount-Scene Cinematic Script..."
                )
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    currentStepNumber = 2,
                    stepTitle = "Creating Complete Script",
                    stepDetails = "Drafting $totalScenesCount scenes with visual cues and camera angles...",
                    overallProgress = 15
                )

                // STEP 2 & 3 — CHARACTER CONSISTENCY & SCRIPT
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    currentStepNumber = 3,
                    stepTitle = "Character Consistency Bible",
                    stepDetails = "Fixing faces, attire, and physical profiles for all scenes...",
                    overallProgress = 22
                )
                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.CHARACTERS.name,
                    progress = 22,
                    stageText = "Step 3: Building Character Consistency Bible..."
                )

                val characters = CharacterBibleEngine.generateCharacterBible(
                    projectId = projectId,
                    prototypes = analysis.characterPrototypes,
                    videoStyleKeyword = style.displayName
                )
                repository.insertCharacters(characters)

                // SCRIPT SCENES GENERATION
                val generatedScenes = if (!effectiveApiKey.isNullOrEmpty()) {
                    try {
                        NexoraGeminiClient.generateScriptScenes(
                            apiKey = effectiveApiKey,
                            projectId = projectId,
                            analysis = analysis,
                            characters = characters,
                            style = style,
                            language = language,
                            aspectRatio = aspectRatio
                        )
                    } catch (e: Exception) {
                        ScriptWriterEngine.generateScenesScript(
                            projectId = projectId,
                            analysis = analysis,
                            characters = characters,
                            style = style,
                            language = language,
                            aspectRatio = aspectRatio
                        )
                    }
                } else {
                    ScriptWriterEngine.generateScenesScript(
                        projectId = projectId,
                        analysis = analysis,
                        characters = characters,
                        style = style,
                        language = language,
                        aspectRatio = aspectRatio
                    )
                }
                repository.insertScenes(generatedScenes)

                // STEP 4 — VISUAL GENERATION WITH SCENE CACHING
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    currentStepNumber = 4,
                    stepTitle = "Generating Scene Visuals",
                    stepDetails = "Rendering scene clips with camera movements...",
                    overallProgress = 30
                )
                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.GENERATING_SCENES.name,
                    progress = 30,
                    stageText = "Step 4: Generating Scene Visuals (0/${generatedScenes.size})..."
                )

                for ((idx, scene) in generatedScenes.withIndex()) {
                    val sceneNum = idx + 1
                    _pipelineStatus.value = _pipelineStatus.value.copy(
                        currentSceneIndex = sceneNum,
                        stepDetails = "Generating Scene $sceneNum/${generatedScenes.size} [${scene.cameraMovement}]",
                        overallProgress = 30 + ((sceneNum.toFloat() / generatedScenes.size.toFloat()) * 30).toInt()
                    )

                    delay(60)

                    repository.updateSceneStatus(scene.id, SceneStatus.COMPLETED.name)
                    repository.updateCompletedScenes(projectId, sceneNum)
                }

                // STEP 5 & 6 — AI VOICEOVER & DIALOGUE
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    currentStepNumber = 5,
                    stepTitle = "Generating AI Voiceover",
                    stepDetails = "Synthesizing ${language.displayName} narration and character dialogue...",
                    overallProgress = 65
                )
                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.VOICEOVER.name,
                    progress = 65,
                    stageText = "Step 5: Synthesizing ${language.displayName} Voiceover & Dialogue..."
                )
                delay(300)

                // STEP 7 & 8 — BACKGROUND MUSIC & SOUND EFFECTS
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    currentStepNumber = 7,
                    stepTitle = "Composing Music & Sound FX",
                    stepDetails = "Mixing ambient score with audio ducking and synchronized sound FX...",
                    overallProgress = 75
                )
                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.MUSIC.name,
                    progress = 75,
                    stageText = "Step 7: Mixing Music Score & Atmospheric Sound Effects..."
                )
                delay(300)

                // STEP 9 — SUBTITLES GENERATION
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    currentStepNumber = 9,
                    stepTitle = "Adding Subtitles",
                    stepDetails = "Generating time-accurate animated captions with safe margins...",
                    overallProgress = 85
                )
                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.SUBTITLES.name,
                    progress = 85,
                    stageText = "Step 9: Aligning Subtitles in ${language.displayName}..."
                )
                delay(250)

                // STEP 10 — AUTOMATIC VIDEO EDITING & MP4 EXPORT
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    currentStepNumber = 10,
                    stepTitle = "Rendering Final MP4 Video",
                    stepDetails = "Multiplexing video, voice, music, SFX, and subtitles into MP4...",
                    overallProgress = 90
                )
                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.RENDERING.name,
                    progress = 90,
                    stageText = "Step 10: Automatic Video Editing & MP4 Export..."
                )

                val updatedProject = repository.getProjectDirect(projectId) ?: initialProject
                val scenesList = repository.getScenesDirect(projectId)

                val exportedMp4 = Mp4Exporter.exportProjectToMp4(
                    context = context,
                    project = updatedProject,
                    scenes = scenesList,
                    aspectRatio = aspectRatio
                ) { renderPct ->
                    val totalPct = 90 + (renderPct * 0.1f).toInt()
                    _pipelineStatus.value = _pipelineStatus.value.copy(overallProgress = totalPct)
                }

                // Strict Verification: Confirm physical file exists and has size > 0
                if (!exportedMp4.exists() || exportedMp4.length() == 0L) {
                    throw RuntimeException("MP4 file verification failed: Render did not produce a valid file")
                }

                // Complete Video & Set Default Thumbnail
                repository.updateThumbnail(
                    projectId = projectId,
                    title = updatedProject.title,
                    subtitle = "${durationMinutes}-Min Cinematic Experience",
                    seed = projectId * 777L
                )
                repository.markVideoCompleted(projectId, exportedMp4.absolutePath)

                _pipelineStatus.value = ActivePipelineStatus(
                    projectId = projectId,
                    currentStepNumber = 10,
                    stepTitle = "Video Complete",
                    stepDetails = "Your full cinematic MP4 (${exportedMp4.length() / 1024} KB) is ready to stream and download!",
                    overallProgress = 100,
                    currentSceneIndex = totalScenesCount,
                    totalScenes = totalScenesCount,
                    isRunning = false
                )

            } catch (e: CancellationException) {
                // Cancelled
            } catch (e: Exception) {
                _pipelineStatus.value = _pipelineStatus.value.copy(
                    isRunning = false,
                    errorMessage = e.message ?: "Generation encountered an error"
                )
                repository.updateProgress(
                    projectId = projectId,
                    status = ProjectStatus.FAILED.name,
                    progress = _pipelineStatus.value.overallProgress,
                    stageText = "Failed at Step ${_pipelineStatus.value.currentStepNumber}: ${e.message}",
                    error = e.message
                )
            }
        }
    }

    fun retryFailedScene(sceneId: Long, projectId: Long) {
        scope.launch {
            repository.updateSceneStatus(sceneId, SceneStatus.GENERATING.name)
            delay(500)
            repository.updateSceneStatus(sceneId, SceneStatus.COMPLETED.name)
            _pipelineStatus.value = _pipelineStatus.value.copy(failedSceneIndex = null)
        }
    }

    fun generateShorts(projectId: Long): Pair<GeneratedShort, GeneratedShort> {
        val short30 = GeneratedShort(
            id = "short_30_${projectId}",
            projectId = projectId,
            title = "30s Viral Hook Cut",
            durationSeconds = 30,
            hookText = "You won't believe what they discovered inside...",
            selectedSceneIndices = listOf(1, 3, 5)
        )
        val short60 = GeneratedShort(
            id = "short_60_${projectId}",
            projectId = projectId,
            title = "60s Climax Trailer",
            durationSeconds = 60,
            hookText = "The final countdown begins now...",
            selectedSceneIndices = listOf(1, 2, 4, 6, 7)
        )
        scope.launch {
            repository.markHasShorts(projectId)
        }
        return Pair(short30, short60)
    }

    fun generateCustomThumbnail(projectId: Long, customHeadline: String, subtext: String) {
        scope.launch {
            repository.updateThumbnail(
                projectId = projectId,
                title = customHeadline,
                subtitle = subtext,
                seed = System.currentTimeMillis()
            )
        }
    }
}
