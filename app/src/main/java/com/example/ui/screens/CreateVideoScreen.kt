package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.NexoraGlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun CreateVideoScreen(
    storyPrompt: String,
    onStoryPromptChange: (String) -> Unit,
    topic: String,
    onTopicChange: (String) -> Unit,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    language: Language,
    onLanguageChange: (Language) -> Unit,
    voiceGender: VoiceGender,
    onVoiceGenderChange: (VoiceGender) -> Unit,
    videoStyle: VideoStyle,
    onVideoStyleChange: (VideoStyle) -> Unit,
    aspectRatio: AspectRatio,
    onAspectRatioChange: (AspectRatio) -> Unit,
    onGenerateClicked: () -> Unit,
    isGenerating: Boolean,
    isProviderConfigured: Boolean = true,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val durationOptions = listOf(1, 3, 5, 10, 15)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // TOP BANNER
        item {
            SectionHeader(
                title = "Create AI Video",
                subtitle = "Only enter your story or idea. NEXORA automates all 10 pipeline steps to deliver a full MP4.",
                badgeText = "Fully Automated"
            )
        }

        // TOPIC / TITLE INPUT
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Text(
                    text = "Video Topic or Title",
                    color = NexoraCyanBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = onTopicChange,
                    placeholder = { Text("e.g., The Forgotten Resonance", color = NexoraTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_topic_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexoraCyanBright,
                        unfocusedBorderColor = NexoraGlassBorder,
                        focusedTextColor = NexoraTextPrimary,
                        unfocusedTextColor = NexoraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        }

        // STORY / SCRIPT TEXTAREA
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Story / Script / Concept",
                        color = NexoraCyanBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Auto-expands to 10 min",
                        color = NexoraAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = storyPrompt,
                    onValueChange = onStoryPromptChange,
                    placeholder = {
                        Text(
                            "Describe your story or paste your script...\n(If you enter only a short idea, the AI will automatically expand it into a full multi-scene story)",
                            color = NexoraTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("story_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexoraCyanBright,
                        unfocusedBorderColor = NexoraGlassBorder,
                        focusedTextColor = NexoraTextPrimary,
                        unfocusedTextColor = NexoraTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        // VIDEO LENGTH SELECTOR
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Video Duration",
                        color = NexoraCyanBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Default: 10 Minutes (~65 scenes)",
                        color = NexoraTextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durationOptions.forEach { dur ->
                        val isSelected = (durationMinutes == dur)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NexoraCyanBright else NexoraSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NexoraCyanBright else NexoraGlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onDurationChange(dur) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${dur}m",
                                color = if (isSelected) NexoraDarkBg else NexoraTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // LANGUAGE & VOICE SELECTOR
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Text(
                    text = "Desired Language & Voice",
                    color = NexoraCyanBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Language chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Language.values()) { lang ->
                        val isSelected = (language == lang)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NexoraViolet else NexoraSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NexoraViolet else NexoraGlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onLanguageChange(lang) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${lang.displayName} (${lang.nativeName})",
                                color = if (isSelected) Color.White else NexoraTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Gender
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VoiceGender.values().forEach { gender ->
                        val isSelected = (voiceGender == gender)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NexoraCyanBright.copy(alpha = 0.2f) else NexoraSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NexoraCyanBright else NexoraGlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onVoiceGenderChange(gender) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (gender == VoiceGender.MALE) Icons.Default.RecordVoiceOver else Icons.Default.SpatialAudio,
                                    contentDescription = null,
                                    tint = if (isSelected) NexoraCyanBright else NexoraTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${gender.name.lowercase().replaceFirstChar { it.uppercase() }} Voice",
                                    color = if (isSelected) NexoraCyanBright else NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // VIDEO STYLE SELECTOR
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Text(
                    text = "Video Visual Style",
                    color = NexoraCyanBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(VideoStyle.values()) { style ->
                        val isSelected = (videoStyle == style)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) NexoraMagenta.copy(alpha = 0.25f) else NexoraSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NexoraMagenta else NexoraGlassBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onVideoStyleChange(style) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = style.displayName,
                                color = if (isSelected) Color.White else NexoraTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // ASPECT RATIO SELECTOR
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Text(
                    text = "Aspect Ratio",
                    color = NexoraCyanBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AspectRatio.values().forEach { ratio ->
                        val isSelected = (aspectRatio == ratio)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NexoraCyanBright.copy(alpha = 0.2f) else NexoraSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NexoraCyanBright else NexoraGlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onAspectRatioChange(ratio) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (ratio.isVertical) Icons.Default.StayCurrentPortrait else Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = if (isSelected) NexoraCyanBright else NexoraTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ratio.label,
                                    color = if (isSelected) NexoraCyanBright else NexoraTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // PRIMARY GENERATE BUTTON
        item {
            if (!isProviderConfigured) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NexoraRose.copy(alpha = 0.15f))
                        .border(1.dp, NexoraRose, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = NexoraRose, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VIDEO GENERATION PROVIDER NOT CONFIGURED",
                                color = NexoraRose,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A valid Gemini API Key is required to run the real AI video pipeline. Configure it in Settings or the AI Studio Secrets panel.",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp
                        )
                        if (onNavigateToSettings != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onNavigateToSettings,
                                colors = ButtonDefaults.buttonColors(containerColor = NexoraRose),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Go to Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            NexoraGlowButton(
                text = if (isGenerating) "Generating Video (Please Wait)..." else "GENERATE 10-MIN VIDEO",
                icon = Icons.Default.AutoAwesome,
                onClick = onGenerateClicked,
                enabled = !isGenerating && storyPrompt.isNotBlank(),
                isLoading = isGenerating,
                modifier = Modifier.fillMaxWidth(),
                testTag = "generate_video_primary_btn"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Autonomous pipeline will execute: Story Analysis → Script → Characters → Visuals → Voiceover → Music → Subtitles → Video Editing → MP4.",
                color = NexoraTextMuted,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
