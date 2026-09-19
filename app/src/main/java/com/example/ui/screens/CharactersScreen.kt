package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CharacterEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun CharactersScreen(
    characters: List<CharacterEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            SectionHeader(
                title = "Character Consistency Bible",
                subtitle = "Ensures characters maintain identical faces, attire, and voices across all scenes.",
                badgeText = "${characters.size} Profiles"
            )
        }

        if (characters.isEmpty()) {
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
                        Text("No character bibles generated yet. Create a video to build profiles.", color = NexoraTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(characters) { char ->
                CharacterCard(character = char)
            }
        }
    }
}

@Composable
private fun CharacterCard(character: CharacterEntity) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = NexoraSurfaceDark,
        borderColor = NexoraGlassBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(NexoraCyanBright, NexoraViolet)
                        )
                    )
                    .border(2.dp, NexoraCyanBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = character.name,
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    NeonBadge(text = character.role, color = NexoraCyanBright)
                }
                Text(
                    text = "Age: ${character.age} • ${character.bodyType}",
                    color = NexoraTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        CharAttributeRow("Hair & Head", character.hair)
        CharAttributeRow("Wardrobe", character.clothes)
        CharAttributeRow("Complexion", character.skinTone)
        CharAttributeRow("Accessories", character.accessories)
        CharAttributeRow("Voice Profile", character.voiceCharacteristics)
        CharAttributeRow("Personality", character.personality)
    }
}

@Composable
private fun CharAttributeRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = NexoraTextMuted, fontSize = 11.sp, modifier = Modifier.width(90.dp))
        Text(text = value, color = NexoraTextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1f))
    }
}
