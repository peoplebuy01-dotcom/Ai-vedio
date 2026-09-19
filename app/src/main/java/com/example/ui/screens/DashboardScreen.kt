package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.pipeline.ActivePipelineStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.NexoraGlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    projects: List<ProjectEntity>,
    pipelineStatus: ActivePipelineStatus,
    onNavigateToCreate: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onDownloadProject: (ProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // HERO HEADER
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark,
                borderColor = NexoraGlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NeonBadge(text = "NEXORA ENGINE v3.5", color = NexoraCyanBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            NeonBadge(text = "PRIVATE VAULT", color = NexoraViolet)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Automated 10-Minute Video AI",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = NexoraTextPrimary,
                                fontSize = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter your story → AI generates script, characters, scenes, voiceover, music, subtitles, and exports single MP4.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = NexoraTextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                NexoraGlowButton(
                    text = "Generate New 10-Min Video",
                    icon = Icons.Default.VideoCall,
                    onClick = onNavigateToCreate,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dashboard_create_video_btn"
                )
            }
        }

        // PROVIDER NOT CONFIGURED BANNER
        if (pipelineStatus.isProviderNotConfigured || pipelineStatus.errorMessage?.contains("PROVIDER NOT CONFIGURED", ignoreCase = true) == true) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NexoraRose.copy(alpha = 0.15f),
                    borderColor = NexoraRose
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NexoraRose,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VIDEO GENERATION PROVIDER NOT CONFIGURED",
                                color = NexoraRose,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Please configure your Gemini API Key in Settings or the AI Studio Secrets panel to enable real video generation.",
                                color = NexoraTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // ACTIVE PIPELINE RUNNING BANNER (If generating)
        if (pipelineStatus.isRunning) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF131D33),
                    borderColor = NexoraCyanBright
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { pipelineStatus.overallProgress / 100f },
                                modifier = Modifier.size(36.dp),
                                color = NexoraCyanBright,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "STEP ${pipelineStatus.currentStepNumber}/10: ${pipelineStatus.stepTitle}",
                                    color = NexoraCyanBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = pipelineStatus.stepDetails,
                                    color = NexoraTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        NeonBadge(text = "${pipelineStatus.overallProgress}%", color = NexoraAmber)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { pipelineStatus.overallProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NexoraCyanBright,
                        trackColor = NexoraSurfaceElevated
                    )
                }
            }
        }

        // QUICK STATS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Videos Created",
                    value = "${projects.size}",
                    icon = Icons.Default.Movie,
                    color = NexoraCyanBright,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Render Minutes",
                    value = "${projects.sumOf { it.durationMinutes }}m",
                    icon = Icons.Default.Schedule,
                    color = NexoraViolet,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Export Format",
                    value = "MP4/H.264",
                    icon = Icons.Default.Download,
                    color = NexoraEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // PRESET TEMPLATES
        item {
            SectionHeader(
                title = "Cinematic Story Starters",
                subtitle = "Pre-tuned story blueprints with instant 10-minute scene plans"
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    PresetCard(
                        title = "Orbital Awakening",
                        duration = "10 Min • Sci-Fi",
                        description = "Quantum archaeologist uncovers forbidden alien beacon in deep space.",
                        accent = NexoraCyanBright,
                        onClick = onNavigateToCreate
                    )
                }
                item {
                    PresetCard(
                        title = "Whispering Pines",
                        duration = "10 Min • Horror",
                        description = "Investigator trapped in misty mountain manor with temporal shadows.",
                        accent = NexoraMagenta,
                        onClick = onNavigateToCreate
                    )
                }
                item {
                    PresetCard(
                        title = "Lost Glacier Valley",
                        duration = "5 Min • Documentary",
                        description = "Breathtaking expedition through untouched polar caverns and wildlife.",
                        accent = NexoraEmerald,
                        onClick = onNavigateToCreate
                    )
                }
            }
        }

        // RECENT PRIVATE PROJECTS
        item {
            SectionHeader(
                title = "My Private Projects",
                subtitle = "Stored securely on device — ready for instant playback and download"
            )
        }

        if (projects.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NexoraSurfaceDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NexoraCyanBright,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Videos Generated Yet",
                            fontWeight = FontWeight.Bold,
                            color = NexoraTextPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter a prompt and watch NEXORA build a complete 10-minute video.",
                            color = NexoraTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(projects) { proj ->
                ProjectItemCard(
                    project = proj,
                    onOpen = { onOpenProject(proj.id) },
                    onDownload = { onDownloadProject(proj) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = NexoraSurfaceDark,
        borderColor = color.copy(alpha = 0.25f)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = NexoraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = title, color = NexoraTextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun PresetCard(
    title: String,
    duration: String,
    description: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NexoraSurfaceDark)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            NeonBadge(text = duration, color = accent)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = NexoraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = NexoraTextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onDownload: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(project.createdAt))

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        backgroundColor = NexoraSurfaceDark,
        borderColor = NexoraGlassBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Thumbnail preview block
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NexoraCyan.copy(alpha = 0.4f), NexoraViolet.copy(alpha = 0.4f), NexoraDarkBg)
                        )
                    )
                    .border(1.dp, NexoraGlassBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeonBadge(
                        text = "${project.durationMinutes} MIN",
                        color = if (project.durationMinutes >= 10) NexoraCyanBright else NexoraAmber
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = project.videoStyle,
                        color = NexoraTextMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.title,
                    color = NexoraTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$dateStr • ${project.language} • ${project.aspectRatio}",
                    color = NexoraTextSecondary,
                    fontSize = 11.sp
                )
            }

            // Download MP4 button
            IconButton(
                onClick = onDownload,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NexoraCyanBright.copy(alpha = 0.15f))
                    .border(1.dp, NexoraCyanBright.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download MP4",
                    tint = NexoraCyanBright,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
