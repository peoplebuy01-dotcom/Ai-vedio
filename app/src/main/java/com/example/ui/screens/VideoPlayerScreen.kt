package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.CinematicVideoPlayer
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.NexoraGlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun VideoPlayerScreen(
    project: ProjectEntity,
    scenes: List<SceneEntity>,
    onBack: () -> Unit,
    onDownload: (ProjectEntity) -> Unit,
    onGenerateAgain: () -> Unit,
    onCreateShorts: (Long) -> Unit,
    onOpenThumbnailGenerator: () -> Unit,
    onDelete: (Long) -> Unit,
    generatedShorts: Pair<GeneratedShort, GeneratedShort>?,
    showThumbnailModal: Boolean,
    onGenerateThumbnail: (String, String) -> Unit,
    onDismissThumbnailModal: () -> Unit,
    onDismissShortsModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeAspect by remember(project) {
        val parsed = try {
            AspectRatio.valueOf(project.aspectRatio)
        } catch (_: Exception) {
            AspectRatio.LANDSCAPE_16_9
        }
        mutableStateOf(parsed)
    }

    var selectedSceneDetail by remember { mutableStateOf<SceneEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // TOP APP BAR ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NexoraSurfaceElevated)
                        .border(1.dp, NexoraGlassBorder, CircleShape)
                        .testTag("video_details_back_btn")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = project.title,
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${project.durationMinutes} Min Full Video • ${scenes.size} Scenes",
                        color = NexoraCyanBright,
                        fontSize = 11.sp
                    )
                }

                // Aspect Ratio Toggle
                IconButton(
                    onClick = {
                        activeAspect = if (activeAspect == AspectRatio.LANDSCAPE_16_9) AspectRatio.PORTRAIT_9_16 else AspectRatio.LANDSCAPE_16_9
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NexoraSurfaceElevated)
                        .border(1.dp, NexoraGlassBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (activeAspect.isVertical) Icons.Default.StayCurrentPortrait else Icons.Default.Tv,
                        contentDescription = "Aspect Ratio",
                        tint = NexoraCyanBright
                    )
                }
            }
        }

        // LARGE VIDEO PLAYER
        item {
            CinematicVideoPlayer(
                scenes = scenes,
                aspectRatio = activeAspect,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ACTION BUTTONS BAR (Download MP4, Generate Again, Create Short, Thumbnail, Delete)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark,
                borderColor = NexoraCyanBright
            ) {
                // Primary Download MP4 Button
                NexoraGlowButton(
                    text = "DOWNLOAD MP4 VIDEO",
                    icon = Icons.Default.Download,
                    onClick = { onDownload(project) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "download_mp4_action_btn"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Action Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        title = "Create Shorts",
                        icon = Icons.Default.Bolt,
                        color = NexoraViolet,
                        modifier = Modifier.weight(1f),
                        onClick = { onCreateShorts(project.id) },
                        testTag = "create_shorts_btn"
                    )
                    ActionButton(
                        title = "Thumbnail",
                        icon = Icons.Default.Image,
                        color = NexoraMagenta,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenThumbnailGenerator,
                        testTag = "generate_thumbnail_btn"
                    )
                    ActionButton(
                        title = "New Video",
                        icon = Icons.Default.Refresh,
                        color = NexoraCyanBright,
                        modifier = Modifier.weight(1f),
                        onClick = onGenerateAgain,
                        testTag = "generate_again_btn"
                    )
                    ActionButton(
                        title = "Delete",
                        icon = Icons.Default.Delete,
                        color = NexoraRose,
                        modifier = Modifier.weight(1f),
                        onClick = { onDelete(project.id) },
                        testTag = "delete_project_btn"
                    )
                }
            }
        }

        // PROJECT SPECS & DETAILS
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                SectionHeader(title = "Video Architecture", badgeText = "H.264 / AAC")
                Spacer(modifier = Modifier.height(10.dp))
                SpecsRow("Duration Target", "${project.durationMinutes} Minutes (~${scenes.size} scenes)")
                SpecsRow("Language & Audio", "${project.language} • ${project.voiceGender} Voice")
                SpecsRow("Visual Aesthetics", project.videoStyle)
                SpecsRow("Soundtrack", "Dynamic Score with dialogue ducking & localized SFX")
                SpecsRow("Subtitles", "Synchronized Karaoke Subtitles with Safe Margin clipping")
            }
        }

        // SCENE BREAKDOWN TIMELINE
        item {
            SectionHeader(
                title = "Timeline Scene Breakdown (${scenes.size} Scenes)",
                subtitle = "Tap any scene to inspect camera motion, dialogue, and sound cues"
            )
        }

        items(scenes) { scene ->
            SceneItemCard(
                scene = scene,
                onClick = { selectedSceneDetail = scene }
            )
        }
    }

    // MODAL: CREATE SHORTS
    if (generatedShorts != null) {
        AlertDialog(
            onDismissRequest = onDismissShortsModal,
            containerColor = NexoraSurfaceDark,
            title = {
                Text("AI Shorts Generated (9:16 Vertical)", color = NexoraCyanBright, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "AI analyzed your 10-minute video and extracted the most engaging high-impact moments:",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp
                    )

                    // 30s Short
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexoraSurfaceElevated)
                            .border(1.dp, NexoraViolet, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NeonBadge(text = "30-SEC SHORT", color = NexoraViolet)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Viral Hook Cut", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("\"${generatedShorts.first.hookText}\"", color = NexoraAmber, fontSize = 12.sp)
                            Text("Scenes: ${generatedShorts.first.selectedSceneIndices.joinToString(", ")} • 9:16 Vertical", color = NexoraTextMuted, fontSize = 11.sp)
                        }
                    }

                    // 60s Short
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexoraSurfaceElevated)
                            .border(1.dp, NexoraMagenta, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NeonBadge(text = "60-SEC SHORT", color = NexoraMagenta)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Climax Teaser", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("\"${generatedShorts.second.hookText}\"", color = NexoraAmber, fontSize = 12.sp)
                            Text("Scenes: ${generatedShorts.second.selectedSceneIndices.joinToString(", ")} • 9:16 Vertical", color = NexoraTextMuted, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissShortsModal) {
                    Text("Done", color = NexoraCyanBright)
                }
            }
        )
    }

    // MODAL: GENERATE THUMBNAIL
    if (showThumbnailModal) {
        var headline by remember { mutableStateOf(project.title) }
        var subtext by remember { mutableStateOf("THE COMPLETE 10-MINUTE STORY") }

        AlertDialog(
            onDismissRequest = onDismissThumbnailModal,
            containerColor = NexoraSurfaceDark,
            title = {
                Text("Generate YouTube Thumbnail", color = NexoraCyanBright, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Creates a cinematic, high-contrast, attention-grabbing thumbnail without misleading fake information:",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = headline,
                        onValueChange = { headline = it },
                        label = { Text("Main Headline") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyanBright,
                            unfocusedBorderColor = NexoraGlassBorder,
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = subtext,
                        onValueChange = { subtext = it },
                        label = { Text("Sub-Badge / Tagline") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexoraCyanBright,
                            unfocusedBorderColor = NexoraGlassBorder,
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onGenerateThumbnail(headline, subtext) },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraCyanBright)
                ) {
                    Text("Generate & Apply", color = NexoraDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissThumbnailModal) {
                    Text("Cancel", color = NexoraTextMuted)
                }
            }
        )
    }

    // MODAL: SCENE DETAIL INSPECTOR
    if (selectedSceneDetail != null) {
        val sc = selectedSceneDetail!!
        AlertDialog(
            onDismissRequest = { selectedSceneDetail = null },
            containerColor = NexoraSurfaceDark,
            title = {
                Text("Scene ${sc.sceneIndex}: Detailed Script", color = NexoraCyanBright, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Camera: ${sc.cameraMovement} • Duration: ${sc.durationSeconds}s", color = NexoraAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Environment: ${sc.environment}", color = NexoraTextPrimary, fontSize = 12.sp)
                    Text("Actions: ${sc.characterActions}", color = NexoraTextSecondary, fontSize = 12.sp)
                    if (sc.dialogueText != null) {
                        Text("Dialogue (${sc.dialogueSpeaker}): \"${sc.dialogueText}\"", color = NexoraCyanBright, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Narration: ${sc.narrationText}", color = NexoraTextPrimary, fontSize = 12.sp)
                    Text("SFX: ${sc.soundEffect}", color = NexoraTextMuted, fontSize = 11.sp)
                    Text("Score: ${sc.backgroundMusicMood}", color = NexoraTextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSceneDetail = null }) {
                    Text("Close", color = NexoraCyanBright)
                }
            }
        )
    }
}

@Composable
private fun ActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SpecsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = NexoraTextSecondary, fontSize = 12.sp)
        Text(text = value, color = NexoraTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SceneItemCard(
    scene: SceneEntity,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = NexoraSurfaceDark,
        borderColor = NexoraGlassBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                NeonBadge(text = "SCENE ${scene.sceneIndex}", color = NexoraCyanBright)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = scene.cameraMovement,
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = scene.narrationText,
                        color = NexoraTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "${scene.durationSeconds}s",
                color = NexoraAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
