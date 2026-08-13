package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortsDao {
    @Query("SELECT * FROM shorts_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ShortsProjectEntity>>

    @Query("SELECT * FROM shorts_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ShortsProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ShortsProjectEntity): Long

    @Query("DELETE FROM shorts_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM shorts_projects")
    suspend fun clearAll()
}
