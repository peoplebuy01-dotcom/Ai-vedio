package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.AIProvider
import com.example.data.model.ProviderSettings
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.NexoraGlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    settings: ProviderSettings,
    onSaveSettings: (ProviderSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeProvider by remember(settings) { mutableStateOf(settings.activeProvider) }
    var geminiKey by remember(settings) { mutableStateOf(settings.geminiApiKey) }
    var openAiKey by remember(settings) { mutableStateOf(settings.openAiApiKey) }
    var elevenLabsKey by remember(settings) { mutableStateOf(settings.elevenLabsApiKey) }
    var googleTtsKey by remember(settings) { mutableStateOf(settings.googleTtsApiKey) }
    var resolution by remember(settings) { mutableStateOf(settings.resolution) }
    var autoRetry by remember(settings) { mutableStateOf(settings.autoRetryFailedScenes) }
    var sceneCache by remember(settings) { mutableStateOf(settings.enableCostSavingCache) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            SectionHeader(
                title = "AI Provider & Pipeline Settings",
                subtitle = "Modular AI provider orchestration: switch between Gemini, Veo, OpenAI, ElevenLabs, or Local Engine.",
                badgeText = "Modular v3.5"
            )
        }

        // ACTIVE PROVIDER SELECTION
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Text("Active Primary AI Engine", color = NexoraCyanBright, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                AIProvider.values().forEach { prov ->
                    val isSelected = (activeProvider == prov)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NexoraCyanBright.copy(alpha = 0.15f) else NexoraSurfaceElevated)
                            .border(1.dp, if (isSelected) NexoraCyanBright else NexoraGlassBorder, RoundedCornerShape(12.dp))
                            .clickable { activeProvider = prov }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = prov.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (prov == AIProvider.GEMINI_VEO) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        NeonBadge(text = "Recommended", color = NexoraEmerald)
                                    }
                                }
                                Text(text = prov.description, color = NexoraTextSecondary, fontSize = 11.sp)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { activeProvider = prov },
                                colors = RadioButtonDefaults.colors(selectedColor = NexoraCyanBright)
                            )
                        }
                    }
                }
            }
        }

        // API KEYS CONFIGURATION
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Text("AI Provider API Key Vault", color = NexoraCyanBright, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A valid Google AI / Gemini API Key is required for real AI script analysis and scene breakdown. You can enter it here or define GEMINI_API_KEY in the Secrets panel.",
                    color = NexoraTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val isGeminiReady = com.example.pipeline.NexoraGeminiClient.isConfigured(geminiKey)
                KeyInputField(
                    label = "Google AI / Gemini API Key",
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    isConfigured = isGeminiReady,
                    testTag = "gemini_key_input"
                )
                KeyInputField(label = "OpenAI API Key (Sora / GPT-4o)", value = openAiKey, onValueChange = { openAiKey = it }, testTag = "openai_key_input")
                KeyInputField(label = "ElevenLabs API Key", value = elevenLabsKey, onValueChange = { elevenLabsKey = it }, testTag = "elevenlabs_key_input")
                KeyInputField(label = "Google Cloud TTS API Key", value = googleTtsKey, onValueChange = { googleTtsKey = it }, testTag = "google_tts_key_input")
            }
        }

        // COST CONTROL & SCENE CACHING
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                SectionHeader(title = "Cost Control & Error Recovery", badgeText = "Smart Cache")
                Spacer(modifier = Modifier.height(10.dp))

                SwitchRow(
                    title = "Enable Cost-Saving Scene Cache",
                    description = "Never regenerate already completed scenes if generation is paused or interrupted.",
                    checked = sceneCache,
                    onCheckedChange = { sceneCache = it }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = NexoraGlassBorder)

                SwitchRow(
                    title = "Auto-Retry Failed Scenes Only",
                    description = "If scene 23 fails, automatically retry ONLY scene 23 without re-running scenes 1–22.",
                    checked = autoRetry,
                    onCheckedChange = { autoRetry = it }
                )
            }
        }

        // RESOLUTION & EXPORT SETTINGS
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark
            ) {
                Text("Export Resolution", color = NexoraCyanBright, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("1080p (Full HD)", "4K (Ultra HD)").forEach { res ->
                        val isSelected = (resolution == res)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NexoraViolet.copy(alpha = 0.25f) else NexoraSurfaceElevated)
                                .border(1.dp, if (isSelected) NexoraViolet else NexoraGlassBorder, RoundedCornerShape(12.dp))
                                .clickable { resolution = res }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = res, color = if (isSelected) Color.White else NexoraTextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // SAVE BUTTON
        item {
            NexoraGlowButton(
                text = "Save Configuration",
                icon = Icons.Default.Save,
                onClick = {
                    onSaveSettings(
                        settings.copy(
                            activeProvider = activeProvider,
                            geminiApiKey = geminiKey,
                            openAiApiKey = openAiKey,
                            elevenLabsApiKey = elevenLabsKey,
                            googleTtsApiKey = googleTtsKey,
                            resolution = resolution,
                            autoRetryFailedScenes = autoRetry,
                            enableCostSavingCache = sceneCache
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_settings_btn"
            )
        }
    }
}

@Composable
private fun KeyInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    isConfigured: Boolean? = null
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = NexoraTextSecondary, fontSize = 11.sp)
            if (isConfigured != null) {
                Text(
                    text = if (isConfigured) "ACTIVE & CONFIGURED ✓" else "NOT SET",
                    color = if (isConfigured) NexoraEmerald else NexoraRose,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("••••••••••••••••••••", color = NexoraTextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexoraCyanBright,
                unfocusedBorderColor = NexoraGlassBorder,
                focusedTextColor = NexoraTextPrimary,
                unfocusedTextColor = NexoraTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = NexoraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = description, color = NexoraTextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NexoraCyanBright
            )
        )
    }
}
