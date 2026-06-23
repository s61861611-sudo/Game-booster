package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val packageName: String,
    val name: String,
    val isSystemGame: Boolean = false,
    val isCustomAdded: Boolean = false,
    val lastLaunched: Long = 0,
    val isOptimized: Boolean = true,
    val targetFps: Int = 60, // 30, 60, 90, 120
    val renderProfile: String = "Balanced", // Performance, Balanced, Ultra Smooth
    val customDnsEnabled: Boolean = false
)
