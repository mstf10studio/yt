package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shorts_projects")
data class ShortsProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val title: String,
    val durationSeconds: Int,
    val language: String,
    val voiceTone: String,
    val captionStyle: String,
    val scriptJson: String,
    val fullNarration: String,
    val hashtags: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED",
    val videoExportPath: String? = null
)
