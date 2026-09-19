package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import com.example.data.local.NexoraDatabase
import com.example.data.model.*
import com.example.data.repository.NexoraRepository
import com.example.pipeline.ActivePipelineStatus
import com.example.pipeline.VideoPipelineManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class NexoraNavTab(val title: String) {
    DASHBOARD("Dashboard"),
    CREATE_VIDEO("Create Video"),
    MY_PROJECTS("My Projects"),
    CHARACTERS("Characters"),
    ASSETS("Assets"),
    SETTINGS("Settings")
}

class NexoraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NexoraRepository
    val pipelineManager: VideoPipelineManager

    val allProjects: StateFlow<List<ProjectEntity>>
    val allCharacters: StateFlow<List<CharacterEntity>>
    val pipelineStatus: StateFlow<ActivePipelineStatus>
    val providerSettings: StateFlow<ProviderSettings>

    // Navigation & Screen selection
    private val _currentTab = MutableStateFlow(NexoraNavTab.DASHBOARD)
    val currentTab = _currentTab.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId = _selectedProjectId.asStateFlow()

    val selectedProjectScenes: StateFlow<List<SceneEntity>>

    // Create Video Form State
    var storyPromptInput by mutableStateOf("In the year 2184, an orbital researcher receives an ancient analog transmission from a buried subterranean cavern beneath the forgotten frost wastes.")
    var topicInput by mutableStateOf("The Forgotten Resonance")
    var durationMinutesInput by mutableIntStateOf(10)
    var selectedLanguage by mutableStateOf(Language.ENGLISH)
    var selectedVoiceGender by mutableStateOf(VoiceGender.MALE)
    var selectedVideoStyle by mutableStateOf(VideoStyle.CINEMATIC)
    var selectedAspectRatio by mutableStateOf(AspectRatio.LANDSCAPE_16_9)

    // Shorts & Thumbnail Modal States
    private val _generatedShorts = MutableStateFlow<Pair<GeneratedShort, GeneratedShort>?>(null)
    val generatedShorts = _generatedShorts.asStateFlow()

    private val _showThumbnailModal = MutableStateFlow(false)
    val showThumbnailModal = _showThumbnailModal.asStateFlow()

    init {
        val db = NexoraDatabase.getDatabase(application)
        repository = NexoraRepository(db.projectDao(), db.sceneDao(), db.characterDao(), application)
        pipelineManager = VideoPipelineManager(application, repository)

        allProjects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allCharacters = repository.allCharacters.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pipelineStatus = pipelineManager.pipelineStatus.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ActivePipelineStatus()
        )

        providerSettings = repository.settingsFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ProviderSettings()
        )

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        selectedProjectScenes = _selectedProjectId.flatMapLatest { id ->
            if (id != null) repository.getScenesForProject(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Seed a sample project if empty for instant out-of-the-box exploration
        viewModelScope.launch {
            if (repository.getProjectDirect(1L) == null) {
                seedInitialProject()
            }
        }
    }

    fun selectTab(tab: NexoraNavTab) {
        _currentTab.value = tab
    }

    fun openProjectDetails(projectId: Long) {
        _selectedProjectId.value = projectId
    }

    fun closeProjectDetails() {
        _selectedProjectId.value = null
    }

    fun startGenerationFromForm() {
        pipelineManager.startGeneration(
            title = topicInput.ifBlank { "Cinematic AI Odyssey" },
            storyPrompt = storyPromptInput,
            durationMinutes = durationMinutesInput,
            language = selectedLanguage,
            voiceGender = selectedVoiceGender,
            style = selectedVideoStyle,
            aspectRatio = selectedAspectRatio
        )
    }

    fun retryScene(sceneId: Long, projectId: Long) {
        pipelineManager.retryFailedScene(sceneId, projectId)
    }

    fun triggerCreateShorts(projectId: Long) {
        val shorts = pipelineManager.generateShorts(projectId)
        _generatedShorts.value = shorts
    }

    fun dismissShortsModal() {
        _generatedShorts.value = null
    }

    fun openThumbnailModal() {
        _showThumbnailModal.value = true
    }

    fun closeThumbnailModal() {
        _showThumbnailModal.value = false
    }

    fun generateThumbnail(projectId: Long, headline: String, subtext: String) {
        pipelineManager.generateCustomThumbnail(projectId, headline, subtext)
        _showThumbnailModal.value = false
        Toast.makeText(getApplication(), "High-Res YouTube Thumbnail Generated!", Toast.LENGTH_SHORT).show()
    }

    fun downloadVideo(project: ProjectEntity) {
        val filePath = project.videoFilePath
        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                val context = getApplication<Application>()
                Toast.makeText(
                    context,
                    "Saved to Movies / Downloads: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()

                // Launch share/view intent
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "video/mp4")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    // Fallback to media scanner broadcast
                    val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    scanIntent.data = Uri.fromFile(file)
                    context.sendBroadcast(scanIntent)
                }
            } else {
                Toast.makeText(getApplication(), "Generating MP4 file...", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(getApplication(), "Rendering MP4 video...", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
            }
        }
    }

    fun updateSettings(newSettings: ProviderSettings) {
        repository.updateSettings(newSettings)
        Toast.makeText(getApplication(), "AI Provider Settings Saved", Toast.LENGTH_SHORT).show()
    }

    private suspend fun seedInitialProject() {
        pipelineManager.startGeneration(
            title = "Chronicles of Neo-Aethel",
            storyPrompt = "A cinematic exploration of a futuristic metropolis where human consciousness and quantum artificial intelligence merge beneath permanent cyan neon rain.",
            durationMinutes = 10,
            language = Language.ENGLISH,
            voiceGender = VoiceGender.MALE,
            style = VideoStyle.CINEMATIC,
            aspectRatio = AspectRatio.LANDSCAPE_16_9
        )
    }
}
