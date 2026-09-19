package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NeonBadge
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexoraNavTab
import com.example.ui.viewmodel.NexoraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: NexoraViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedProjectId by viewModel.selectedProjectId.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val pipelineStatus by viewModel.pipelineStatus.collectAsStateWithLifecycle()
    val providerSettings by viewModel.providerSettings.collectAsStateWithLifecycle()
    val selectedScenes by viewModel.selectedProjectScenes.collectAsStateWithLifecycle()
    val generatedShorts by viewModel.generatedShorts.collectAsStateWithLifecycle()
    val showThumbnailModal by viewModel.showThumbnailModal.collectAsStateWithLifecycle()

    val selectedProject = allProjects.firstOrNull { it.id == selectedProjectId }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(NexoraDarkBg)) {
        val isWideScreen = maxWidth > 700.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // SIDE NAVIGATION RAIL FOR WIDE / TABLET SCREENS
            if (isWideScreen) {
                NavigationRail(
                    containerColor = NexoraSurfaceDark,
                    contentColor = NexoraTextSecondary,
                    modifier = Modifier.border(1.dp, NexoraGlassBorder),
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(NexoraCyanBright, NexoraViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                        }
                    }
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    NexoraNavTab.values().forEach { tab ->
                        NavigationRailItem(
                            selected = (currentTab == tab && selectedProjectId == null),
                            onClick = {
                                viewModel.closeProjectDetails()
                                viewModel.selectTab(tab)
                            },
                            icon = {
                                Icon(imageVector = getTabIcon(tab), contentDescription = tab.title)
                            },
                            label = { Text(tab.title, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = NexoraDarkBg,
                                indicatorColor = NexoraCyanBright,
                                unselectedIconColor = NexoraTextSecondary
                            )
                        )
                    }
                }
            }

            // MAIN CONTENT AREA
            Scaffold(
                containerColor = NexoraDarkBg,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(listOf(NexoraCyanBright, NexoraViolet))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "NEXORA",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            letterSpacing = 1.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "VIDEO AI",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NexoraCyanBright
                                        )
                                    }
                                    Text(
                                        text = "Autonomous 10-Minute Video Engine",
                                        fontSize = 10.sp,
                                        color = NexoraTextSecondary
                                    )
                                }
                            }
                        },
                        actions = {
                            if (pipelineStatus.isRunning) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(NexoraCyanBright.copy(alpha = 0.2f))
                                        .border(1.dp, NexoraCyanBright, RoundedCornerShape(20.dp))
                                        .clickable {
                                            viewModel.closeProjectDetails()
                                            viewModel.selectTab(NexoraNavTab.DASHBOARD)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = NexoraCyanBright,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Step ${pipelineStatus.currentStepNumber}/10",
                                            color = NexoraCyanBright,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = NexoraDarkBg
                        )
                    )
                },
                bottomBar = {
                    // BOTTOM NAVIGATION FOR MOBILE SCREENS
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = NexoraSurfaceDark,
                            contentColor = NexoraTextSecondary,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .border(1.dp, NexoraGlassBorder)
                                .testTag("bottom_nav_bar")
                        ) {
                            NexoraNavTab.values().forEach { tab ->
                                val isSelected = (currentTab == tab && selectedProjectId == null)
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.closeProjectDetails()
                                        viewModel.selectTab(tab)
                                    },
                                    icon = {
                                        Icon(imageVector = getTabIcon(tab), contentDescription = tab.title)
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NexoraDarkBg,
                                        indicatorColor = NexoraCyanBright,
                                        unselectedIconColor = NexoraTextSecondary,
                                        unselectedTextColor = NexoraTextSecondary,
                                        selectedTextColor = NexoraCyanBright
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    // IF A PROJECT IS SELECTED FOR WATCHING/DETAILS
                    if (selectedProject != null) {
                        VideoPlayerScreen(
                            project = selectedProject,
                            scenes = selectedScenes,
                            onBack = { viewModel.closeProjectDetails() },
                            onDownload = { viewModel.downloadVideo(it) },
                            onGenerateAgain = {
                                viewModel.closeProjectDetails()
                                viewModel.selectTab(NexoraNavTab.CREATE_VIDEO)
                            },
                            onCreateShorts = { viewModel.triggerCreateShorts(it) },
                            onOpenThumbnailGenerator = { viewModel.openThumbnailModal() },
                            onDelete = { viewModel.deleteProject(it) },
                            generatedShorts = generatedShorts,
                            showThumbnailModal = showThumbnailModal,
                            onGenerateThumbnail = { headline, subtext ->
                                viewModel.generateThumbnail(selectedProject.id, headline, subtext)
                            },
                            onDismissThumbnailModal = { viewModel.closeThumbnailModal() },
                            onDismissShortsModal = { viewModel.dismissShortsModal() }
                        )
                    } else {
                        // TAB NAVIGATION
                        when (currentTab) {
                            NexoraNavTab.DASHBOARD -> {
                                if (pipelineStatus.isRunning) {
                                    GenerationProgressScreen(
                                        status = pipelineStatus,
                                        onViewCompletedVideo = { projId ->
                                            viewModel.openProjectDetails(projId)
                                        },
                                        onRetryScene = { scId, projId ->
                                            viewModel.retryScene(scId, projId)
                                        }
                                    )
                                } else {
                                    DashboardScreen(
                                        projects = allProjects,
                                        pipelineStatus = pipelineStatus,
                                        onNavigateToCreate = { viewModel.selectTab(NexoraNavTab.CREATE_VIDEO) },
                                        onOpenProject = { viewModel.openProjectDetails(it) },
                                        onDownloadProject = { viewModel.downloadVideo(it) }
                                    )
                                }
                            }

                            NexoraNavTab.CREATE_VIDEO -> {
                                CreateVideoScreen(
                                    storyPrompt = viewModel.storyPromptInput,
                                    onStoryPromptChange = { viewModel.storyPromptInput = it },
                                    topic = viewModel.topicInput,
                                    onTopicChange = { viewModel.topicInput = it },
                                    durationMinutes = viewModel.durationMinutesInput,
                                    onDurationChange = { viewModel.durationMinutesInput = it },
                                    language = viewModel.selectedLanguage,
                                    onLanguageChange = { viewModel.selectedLanguage = it },
                                    voiceGender = viewModel.selectedVoiceGender,
                                    onVoiceGenderChange = { viewModel.selectedVoiceGender = it },
                                    videoStyle = viewModel.selectedVideoStyle,
                                    onVideoStyleChange = { viewModel.selectedVideoStyle = it },
                                    aspectRatio = viewModel.selectedAspectRatio,
                                    onAspectRatioChange = { viewModel.selectedAspectRatio = it },
                                    onGenerateClicked = {
                                        viewModel.startGenerationFromForm()
                                        viewModel.selectTab(NexoraNavTab.DASHBOARD)
                                    },
                                    isGenerating = pipelineStatus.isRunning,
                                    isProviderConfigured = com.example.pipeline.NexoraGeminiClient.isConfigured(providerSettings.geminiApiKey),
                                    onNavigateToSettings = { viewModel.selectTab(NexoraNavTab.SETTINGS) }
                                )
                            }

                            NexoraNavTab.MY_PROJECTS -> {
                                MyProjectsScreen(
                                    projects = allProjects,
                                    onOpenProject = { viewModel.openProjectDetails(it) },
                                    onDownloadProject = { viewModel.downloadVideo(it) },
                                    onNavigateToCreate = { viewModel.selectTab(NexoraNavTab.CREATE_VIDEO) }
                                )
                            }

                            NexoraNavTab.CHARACTERS -> {
                                CharactersScreen(characters = allCharacters)
                            }

                            NexoraNavTab.ASSETS -> {
                                AssetsScreen()
                            }

                            NexoraNavTab.SETTINGS -> {
                                SettingsScreen(
                                    settings = providerSettings,
                                    onSaveSettings = { viewModel.updateSettings(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getTabIcon(tab: NexoraNavTab) = when (tab) {
    NexoraNavTab.DASHBOARD -> Icons.Default.Dashboard
    NexoraNavTab.CREATE_VIDEO -> Icons.Default.VideoCall
    NexoraNavTab.MY_PROJECTS -> Icons.Default.VideoLibrary
    NexoraNavTab.CHARACTERS -> Icons.Default.People
    NexoraNavTab.ASSETS -> Icons.Default.Audiotrack
    NexoraNavTab.SETTINGS -> Icons.Default.Settings
}
