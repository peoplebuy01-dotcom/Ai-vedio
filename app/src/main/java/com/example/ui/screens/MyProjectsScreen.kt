package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun MyProjectsScreen(
    projects: List<ProjectEntity>,
    onOpenProject: (Long) -> Unit,
    onDownloadProject: (ProjectEntity) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) projects
        else projects.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.videoStyle.contains(searchQuery, ignoreCase = true) ||
            it.language.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            SectionHeader(
                title = "My Private Vault",
                subtitle = "All rendered videos are stored locally on your device with direct MP4 export.",
                badgeText = "${projects.size} Projects"
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by title, style, or language...", color = NexoraTextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NexoraCyanBright) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("projects_search_bar"),
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

        if (filteredProjects.isEmpty()) {
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
                        Text("No projects found", color = NexoraTextSecondary, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(filteredProjects) { project ->
                ProjectItemCard(
                    project = project,
                    onOpen = { onOpenProject(project.id) },
                    onDownload = { onDownloadProject(project) }
                )
            }
        }
    }
}
