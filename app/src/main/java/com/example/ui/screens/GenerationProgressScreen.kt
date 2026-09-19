package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipeline.ActivePipelineStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.NexoraGlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

data class StepProgressItem(
    val stepIndex: Int,
    val title: String,
    val description: String
)

@Composable
fun GenerationProgressScreen(
    status: ActivePipelineStatus,
    onViewCompletedVideo: (Long) -> Unit,
    onRetryScene: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        StepProgressItem(1, "Analyzing Story", "Characters, theme, mood & progression determined"),
        StepProgressItem(2, "Creating Script", "Cinematic script divided into ${status.totalScenes.coerceAtLeast(65)} scenes"),
        StepProgressItem(3, "Character Consistency", "Character bible initialized with persistent facial & attire attributes"),
        StepProgressItem(4, "Generating Scene Visuals", "Scene ${status.currentSceneIndex}/${status.totalScenes.coerceAtLeast(65)} rendered with camera movement"),
        StepProgressItem(5, "Generating AI Voiceover", "Synchronized narration & character dialogue generated"),
        StepProgressItem(6, "Generating Background Music", "Adaptive cinematic score selected with dialogue ducking"),
        StepProgressItem(7, "Adding Sound Effects", "Environmental audio & keyframe sound effects synchronized"),
        StepProgressItem(8, "Aligning Subtitles", "Time-coded animated karaoke subtitles styled"),
        StepProgressItem(9, "Automatic Video Editing", "Timeline transitions, color grading, and intro/outro finalized"),
        StepProgressItem(10, "Rendering Final MP4 Video", "H.264 video & AAC audio multiplexed into single downloadable MP4")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // TOP HEADER
        item {
            SectionHeader(
                title = "Generation Dashboard",
                subtitle = "Autonomous pipeline executing end-to-end. Runs in background safely.",
                badgeText = if (status.overallProgress >= 100) "Completed" else "Processing"
            )
        }

        // LIVE PROGRESS RADIAL / GAUGE CARD
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark,
                borderColor = NexoraCyanBright
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { status.overallProgress / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = NexoraCyanBright,
                            trackColor = NexoraSurfaceElevated,
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${status.overallProgress}%",
                                color = NexoraTextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                            Text(
                                text = if (status.overallProgress >= 100) "COMPLETE" else "STEP ${status.currentStepNumber}/10",
                                color = NexoraCyanBright,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = status.stepTitle,
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status.stepDetails.ifBlank { "Initializing neural pipeline..." },
                        color = if (status.isProviderNotConfigured) NexoraRose else NexoraTextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Provider Not Configured Warning
                    if (status.isProviderNotConfigured || status.errorMessage?.contains("PROVIDER NOT CONFIGURED", ignoreCase = true) == true) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexoraRose.copy(alpha = 0.15f))
                                .border(1.dp, NexoraRose, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = NexoraRose, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VIDEO GENERATION PROVIDER NOT CONFIGURED — Please set your API Key in Settings.",
                                    color = NexoraRose,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Error & Scene Retry notice
                    if (status.failedSceneIndex != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexoraRose.copy(alpha = 0.15f))
                                .border(1.dp, NexoraRose, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = NexoraRose, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scene ${status.failedSceneIndex} failed — Cost-saving cache active",
                                        color = NexoraRose,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { onRetryScene(status.failedSceneIndex.toLong(), status.projectId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NexoraRose),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Done Action
                    if (status.overallProgress >= 100 && status.projectId > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        NexoraGlowButton(
                            text = "Open & Watch Video",
                            icon = Icons.Default.PlayArrow,
                            onClick = { onViewCompletedVideo(status.projectId) },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "watch_completed_video_btn"
                        )
                    }
                }
            }
        }

        // 10-STEP PIPELINE BREAKDOWN CHECKLIST
        item {
            SectionHeader(
                title = "Pipeline Execution Stages",
                subtitle = "Automated continuous synthesis with scene caching and resume capability"
            )
        }

        items(steps.size) { index ->
            val step = steps[index]
            val isPassed = status.currentStepNumber > step.stepIndex || status.overallProgress >= 100
            val isCurrent = status.currentStepNumber == step.stepIndex && status.overallProgress < 100

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (isCurrent) Color(0xFF162544) else NexoraSurfaceDark,
                borderColor = if (isCurrent) NexoraCyanBright else NexoraGlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isPassed -> NexoraEmerald
                                    isCurrent -> NexoraCyanBright
                                    else -> NexoraSurfaceElevated
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPassed) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else if (isCurrent) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "${step.stepIndex}",
                                color = NexoraTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = step.title,
                                color = if (isCurrent) NexoraCyanBright else NexoraTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (isPassed) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("— ✓", color = NexoraEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = step.description,
                            color = NexoraTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (isCurrent) {
                        NeonBadge(text = "Running", color = NexoraCyanBright)
                    }
                }
            }
        }
    }
}
