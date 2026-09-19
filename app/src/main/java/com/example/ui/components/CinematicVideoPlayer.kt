package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMovement
import com.example.data.model.SceneEntity
import com.example.pipeline.SceneSubtitleData
import com.example.pipeline.VoiceoverEngine
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun CinematicVideoPlayer(
    scenes: List<SceneEntity>,
    aspectRatio: AspectRatio,
    modifier: Modifier = Modifier,
    onSceneChanged: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val voiceEngine = remember { VoiceoverEngine(context) }
    DisposableEffect(Unit) {
        onDispose { voiceEngine.release() }
    }

    if (scenes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NexoraSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text("No video scenes available", color = NexoraTextMuted)
        }
        return
    }

    var isPlaying by remember { mutableStateOf(true) }
    var currentSceneIdx by remember { mutableIntStateOf(0) }
    var sceneElapsedMs by remember { mutableLongStateOf(0L) }
    var isAudioMuted by remember { mutableStateOf(false) }

    val activeScene = scenes.getOrNull(currentSceneIdx) ?: scenes.first()
    val sceneDurationMs = (activeScene.durationSeconds * 1000L).coerceAtLeast(4000L)

    // Trigger voice narration when entering a new scene
    LaunchedEffect(currentSceneIdx, isPlaying) {
        if (isPlaying && !isAudioMuted) {
            voiceEngine.speakNarration(activeScene.narrationText)
        } else {
            voiceEngine.stop()
        }
    }

    // Playback clock
    LaunchedEffect(isPlaying, currentSceneIdx) {
        val interval = 50L
        while (isActive && isPlaying) {
            delay(interval)
            sceneElapsedMs += interval
            if (sceneElapsedMs >= sceneDurationMs) {
                sceneElapsedMs = 0L
                if (currentSceneIdx < scenes.size - 1) {
                    currentSceneIdx++
                    onSceneChanged?.invoke(currentSceneIdx)
                } else {
                    currentSceneIdx = 0 // Loop
                    onSceneChanged?.invoke(0)
                }
            }
        }
    }

    // Calculate scene subtitle timing
    val subtitleData = remember(activeScene) {
        VoiceoverEngine.generateWordTimings(
            sceneIndex = activeScene.sceneIndex,
            narration = activeScene.narrationText,
            speaker = activeScene.dialogueSpeaker,
            dialogue = activeScene.dialogueText,
            sceneDurationSec = activeScene.durationSeconds
        )
    }

    val progressInScene = (sceneElapsedMs.toFloat() / sceneDurationMs.toFloat()).coerceIn(0f, 1f)

    // Ken Burns camera motion offsets
    val cameraMovement = remember(activeScene) {
        try {
            CameraMovement.valueOf(activeScene.cameraMovement)
        } catch (_: Exception) {
            CameraMovement.ZOOM_IN
        }
    }

    val cameraScale = when (cameraMovement) {
        CameraMovement.ZOOM_IN -> 1.0f + (progressInScene * 0.15f)
        CameraMovement.ZOOM_OUT -> 1.15f - (progressInScene * 0.15f)
        else -> 1.05f
    }
    val cameraPanX = when (cameraMovement) {
        CameraMovement.PAN_LEFT -> 20f - (progressInScene * 40f)
        CameraMovement.PAN_RIGHT -> -20f + (progressInScene * 40f)
        else -> 0f
    }
    val cameraPanY = when (cameraMovement) {
        CameraMovement.TILT_UP -> 15f - (progressInScene * 30f)
        else -> 0f
    }

    val targetHeight = if (aspectRatio.isVertical) 420.dp else 220.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NexoraSurfaceDark)
            .border(1.dp, NexoraGlassBorder, RoundedCornerShape(24.dp))
    ) {
        // VIEWPORT CANVAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(targetHeight)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.Black)
                .clickable { isPlaying = !isPlaying }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw synthetic cinematic scene visual
                val c1 = Color(activeScene.colorTonePrimary)
                val c2 = Color(activeScene.colorToneSecondary)

                val gradient = Brush.linearGradient(
                    colors = listOf(c1, c2, NexoraDarkBg),
                    start = Offset(cameraPanX * 2, cameraPanY * 2),
                    end = Offset(canvasW * cameraScale, canvasH * cameraScale)
                )
                drawRect(brush = gradient, size = size)

                // Atmospheric aperture rings (representing motion vector)
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = (canvasH * 0.45f) * cameraScale,
                    center = Offset(canvasW / 2 + cameraPanX * 3, canvasH / 2 + cameraPanY * 3),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = NexoraCyanBright.copy(alpha = 0.12f),
                    radius = (canvasH * 0.3f) * cameraScale,
                    center = Offset(canvasW / 2 + cameraPanX * 2, canvasH / 2 + cameraPanY * 2),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Atmospheric film grain / particle dots
                for (p in 0..12) {
                    val px = ((activeScene.visualSeed + p * 137) % canvasW.toInt()).toFloat()
                    val py = ((activeScene.visualSeed + p * 83) % canvasH.toInt()).toFloat()
                    drawCircle(
                        color = NexoraCyanBright.copy(alpha = 0.25f),
                        radius = 2.dp.toPx(),
                        center = Offset(
                            (px + (cameraPanX * (p % 3))) % canvasW,
                            (py + (cameraPanY * (p % 3))) % canvasH
                        )
                    )
                }

                // Cinematic letterbox top/bottom subtle shading
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
                        startY = 0f,
                        endY = 50.dp.toPx()
                    ),
                    size = Size(canvasW, 50.dp.toPx())
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = canvasH - 70.dp.toPx(),
                        endY = canvasH
                    ),
                    topLeft = Offset(0f, canvasH - 70.dp.toPx()),
                    size = Size(canvasW, 70.dp.toPx())
                )
            }

            // Top Info Bar (Scene index, camera angle, audio ducking badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeonBadge(
                        text = "SCENE ${activeScene.sceneIndex}/${scenes.size}",
                        color = NexoraCyanBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeScene.cameraMovement,
                        color = NexoraTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Audio ducking & SFX indicator
                    NeonBadge(
                        text = "SFX: ${activeScene.soundEffect.take(16)}...",
                        color = NexoraAmber
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { isAudioMuted = !isAudioMuted },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Mute",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Central Play Indicator overlay when paused
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, NexoraCyanBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Bottom Subtitles Box (Animated word-by-word with safe margin)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (activeScene.dialogueSpeaker != null) {
                        Text(
                            text = activeScene.dialogueSpeaker,
                            color = NexoraCyanBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Render karaoke words with timing
                    val words = subtitleData.words
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildString {
                                words.forEach { wordData ->
                                    append(wordData.word)
                                    append(" ")
                                }
                            },
                            color = if (isPlaying) NexoraAmber else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // PLAYER CONTROLS & TIMELINE SCRUBBER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Scene Scrubber
            val totalSeconds = scenes.sumOf { it.durationSeconds }
            val currentTotalSec = scenes.take(currentSceneIdx).sumOf { it.durationSeconds } + (sceneElapsedMs / 1000).toInt()

            val curMin = currentTotalSec / 60
            val curSec = currentTotalSec % 60
            val totMin = totalSeconds / 60
            val totSec = totalSeconds % 60
            val timeStr = String.format("%02d:%02d / %02d:%02d", curMin, curSec, totMin, totSec)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeStr,
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${aspectRatio.label} • 1080p H.264",
                    color = NexoraCyanBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress Slider
            Slider(
                value = currentTotalSec.toFloat(),
                onValueChange = { targetSec ->
                    var accumulated = 0
                    for ((idx, sc) in scenes.withIndex()) {
                        accumulated += sc.durationSeconds
                        if (accumulated >= targetSec.toInt()) {
                            currentSceneIdx = idx
                            sceneElapsedMs = 0L
                            break
                        }
                    }
                },
                valueRange = 0f..totalSeconds.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = NexoraCyanBright,
                    activeTrackColor = NexoraCyanBright,
                    inactiveTrackColor = NexoraSurfaceElevated
                ),
                modifier = Modifier.testTag("video_timeline_scrubber")
            )

            // Playback buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (currentSceneIdx > 0) {
                                currentSceneIdx--
                                sceneElapsedMs = 0L
                            }
                        },
                        modifier = Modifier.testTag("prev_scene_btn")
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Scene", tint = Color.White)
                    }

                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .testTag("play_pause_btn")
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NexoraCyanBright)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = NexoraDarkBg
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentSceneIdx < scenes.size - 1) {
                                currentSceneIdx++
                                sceneElapsedMs = 0L
                            }
                        },
                        modifier = Modifier.testTag("next_scene_btn")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Scene", tint = Color.White)
                    }
                }

                // Music mood tag
                Text(
                    text = "Score: ${activeScene.backgroundMusicMood.take(24)}...",
                    color = NexoraTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
