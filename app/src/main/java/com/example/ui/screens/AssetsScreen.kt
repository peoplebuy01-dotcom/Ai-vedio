package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

data class AssetEntry(
    val category: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun AssetsScreen(modifier: Modifier = Modifier) {
    val assets = listOf(
        AssetEntry("Music Score", "Ethereal Celestial Drone", "Low cello swells and celestial synthesizers for sci-fi atmosphere", Icons.Default.MusicNote, NexoraCyanBright),
        AssetEntry("Music Score", "Psychological Ostinato", "Tense violin staccato with accelerating acoustic pulses", Icons.Default.MusicNote, NexoraMagenta),
        AssetEntry("Music Score", "Epic Orchestral Crescendo", "Thunderous hybrid brass, timpani hits, and choir swells", Icons.Default.MusicNote, NexoraAmber),
        AssetEntry("Sound Effect", "Pneumatic Vault Door Hiss", "Pressurized air release, mechanical gear lock", Icons.Default.SurroundSound, NexoraViolet),
        AssetEntry("Sound Effect", "Echoing Footsteps on Wet Stone", "Deliberate rhythmic boot impacts with cavern reverberation", Icons.Default.SurroundSound, NexoraEmerald),
        AssetEntry("Sound Effect", "Sub-Bass Impact Drop", "Deep low-frequency cinematic boom for climactic reveals", Icons.Default.SurroundSound, NexoraRose),
        AssetEntry("Sound Effect", "Atmospheric Canyon Wind", "High-frequency whistling gusts through stone spires", Icons.Default.SurroundSound, NexoraCyanBright),
        AssetEntry("Camera Movement", "Dynamic Ken Burns Pan & Zoom", "Smooth focal shift enhancing narrative drama per keyframe", Icons.Default.Videocam, NexoraCyanBright),
        AssetEntry("Transition", "Dip to Black / Film Crossfade", "Zero-jarring cinematic transition pacing between narrative acts", Icons.Default.MovieFilter, NexoraViolet)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            SectionHeader(
                title = "Audio & Visual Assets Library",
                subtitle = "Built-in atmospheric scores, sound effects, and camera kinematics.",
                badgeText = "${assets.size} Pre-Tuned Assets"
            )
        }

        items(assets) { asset ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NexoraSurfaceDark,
                borderColor = NexoraGlassBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = asset.icon, contentDescription = null, tint = asset.color, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = asset.title, color = NexoraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            NeonBadge(text = asset.category, color = asset.color)
                        }
                        Text(text = asset.description, color = NexoraTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
