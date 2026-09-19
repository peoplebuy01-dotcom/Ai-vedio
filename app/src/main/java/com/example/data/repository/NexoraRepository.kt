package com.example.data.repository

import android.content.Context
import com.example.data.local.CharacterDao
import com.example.data.local.ProjectDao
import com.example.data.local.SceneDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NexoraRepository(
    private val projectDao: ProjectDao,
    private val sceneDao: SceneDao,
    private val characterDao: CharacterDao,
    private val context: Context
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allCharacters: Flow<List<CharacterEntity>> = characterDao.getAllCharacters()

    private val _settingsFlow = MutableStateFlow(ProviderSettings())
    val settingsFlow = _settingsFlow.asStateFlow()

    fun updateSettings(newSettings: ProviderSettings) {
        _settingsFlow.value = newSettings
    }

    fun getProjectById(projectId: Long): Flow<ProjectEntity?> =
        projectDao.getProjectById(projectId)

    suspend fun getProjectDirect(projectId: Long): ProjectEntity? =
        projectDao.getProjectDirect(projectId)

    fun getScenesForProject(projectId: Long): Flow<List<SceneEntity>> =
        sceneDao.getScenesForProject(projectId)

    suspend fun getScenesDirect(projectId: Long): List<SceneEntity> =
        sceneDao.getScenesDirect(projectId)

    fun getCharactersForProject(projectId: Long): Flow<List<CharacterEntity>> =
        characterDao.getCharactersForProject(projectId)

    suspend fun insertProject(project: ProjectEntity): Long =
        projectDao.insertProject(project)

    suspend fun updateProject(project: ProjectEntity) =
        projectDao.updateProject(project)

    suspend fun updateProgress(projectId: Long, status: String, progress: Int, stageText: String, error: String? = null) =
        projectDao.updateProgress(projectId, status, progress, stageText, error)

    suspend fun updateCompletedScenes(projectId: Long, completedScenes: Int) =
        projectDao.updateCompletedScenes(projectId, completedScenes)

    suspend fun markVideoCompleted(projectId: Long, path: String) =
        projectDao.markVideoCompleted(projectId, path)

    suspend fun markHasShorts(projectId: Long) =
        projectDao.markHasShorts(projectId)

    suspend fun updateThumbnail(projectId: Long, title: String, subtitle: String, seed: Long) =
        projectDao.updateThumbnail(projectId, title, subtitle, seed)

    suspend fun insertScenes(scenes: List<SceneEntity>) =
        sceneDao.insertScenes(scenes)

    suspend fun updateScene(scene: SceneEntity) =
        sceneDao.updateScene(scene)

    suspend fun updateSceneStatus(sceneId: Long, status: String) =
        sceneDao.updateSceneStatus(sceneId, status)

    suspend fun insertCharacters(characters: List<CharacterEntity>) =
        characterDao.insertCharacters(characters)

    suspend fun deleteProject(projectId: Long) {
        sceneDao.deleteScenesForProject(projectId)
        characterDao.deleteCharactersForProject(projectId)
        projectDao.deleteProjectById(projectId)
    }
}
