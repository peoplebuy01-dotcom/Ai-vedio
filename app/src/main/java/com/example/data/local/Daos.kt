package com.example.data.local

import androidx.room.*
import com.example.data.model.CharacterEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.SceneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectById(projectId: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectDirect(projectId: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET status = :status, progress = :progress, currentStageText = :stageText, errorMessage = :error WHERE id = :projectId")
    suspend fun updateProgress(projectId: Long, status: String, progress: Int, stageText: String, error: String? = null)

    @Query("UPDATE projects SET completedScenes = :completedScenes WHERE id = :projectId")
    suspend fun updateCompletedScenes(projectId: Long, completedScenes: Int)

    @Query("UPDATE projects SET videoFilePath = :path, status = 'COMPLETED', progress = 100, currentStageText = 'Video Complete — Ready to Download' WHERE id = :projectId")
    suspend fun markVideoCompleted(projectId: Long, path: String)

    @Query("UPDATE projects SET hasShorts = 1 WHERE id = :projectId")
    suspend fun markHasShorts(projectId: Long)

    @Query("UPDATE projects SET thumbnailTitle = :title, thumbnailSubtitle = :subtitle, thumbnailVisualSeed = :seed WHERE id = :projectId")
    suspend fun updateThumbnail(projectId: Long, title: String, subtitle: String, seed: Long)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Long)
}

@Dao
interface SceneDao {
    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY sceneIndex ASC")
    fun getScenesForProject(projectId: Long): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY sceneIndex ASC")
    suspend fun getScenesDirect(projectId: Long): List<SceneEntity>

    @Query("SELECT * FROM scenes WHERE id = :sceneId")
    suspend fun getSceneById(sceneId: Long): SceneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenes(scenes: List<SceneEntity>)

    @Update
    suspend fun updateScene(scene: SceneEntity)

    @Query("UPDATE scenes SET status = :status WHERE id = :sceneId")
    suspend fun updateSceneStatus(sceneId: Long, status: String)

    @Query("DELETE FROM scenes WHERE projectId = :projectId")
    suspend fun deleteScenesForProject(projectId: Long)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE projectId = :projectId ORDER BY id ASC")
    fun getCharactersForProject(projectId: Long): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE projectId = :projectId ORDER BY id ASC")
    suspend fun getCharactersDirect(projectId: Long): List<CharacterEntity>

    @Query("SELECT * FROM characters ORDER BY id DESC")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters WHERE projectId = :projectId")
    suspend fun deleteCharactersForProject(projectId: Long)
}
