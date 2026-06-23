package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gamePackageName: String,
    val gameName: String,
    val timestamp: Long, // Epoch ms
    val durationSeconds: Long,
    val avgFps: Float,
    val minFps: Float,
    val maxFps: Float,
    val avgPing: Int,
    val avgCpuUsage: Float,
    val avgRamUsage: Float,
    val avgTemp: Float,
    val frameDrops: Int
)
