package com.forgeprom.studio.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun activeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun archivedProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun byId(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' ORDER BY updatedAt DESC")
    fun search(q: String): Flow<List<ProjectEntity>>

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("UPDATE projects SET isPinned = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, now: Long)

    @Query("UPDATE projects SET isArchived = :archived, updatedAt = :now WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean, now: Long)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun count(): Int
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun forProject(projectId: Long): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    suspend fun byId(id: Long): PromptEntity?

    @Insert
    suspend fun insert(prompt: PromptEntity): Long

    @Update
    suspend fun update(prompt: PromptEntity)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun count(): Int
}

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analyses WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun forProject(projectId: Long): Flow<List<AnalysisEntity>>

    @Insert
    suspend fun insert(analysis: AnalysisEntity): Long
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY at DESC LIMIT :limit")
    fun recent(limit: Int = 20): Flow<List<ActivityEntity>>

    @Insert
    suspend fun insert(activity: ActivityEntity)

    @Query("SELECT COUNT(*) FROM activities")
    suspend fun count(): Int
}

@Dao
interface EditorFileDao {
    @Query("SELECT * FROM editor_files WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun forProject(projectId: Long): Flow<List<EditorFileEntity>>

    @Query("SELECT * FROM editor_files WHERE id = :id")
    suspend fun byId(id: Long): EditorFileEntity?

    @Insert
    suspend fun insert(file: EditorFileEntity): Long

    @Update
    suspend fun update(file: EditorFileEntity)

    @Query("DELETE FROM editor_files WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ImageAnalysisDao {
    @Query("SELECT * FROM image_analyses WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun forProject(projectId: Long): Flow<List<ImageAnalysisEntity>>

    @Insert
    suspend fun insert(analysis: ImageAnalysisEntity): Long
}
