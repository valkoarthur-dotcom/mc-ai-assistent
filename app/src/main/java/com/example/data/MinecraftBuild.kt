package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "builds")
data class MinecraftBuild(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // Houses, Castles, Farms, Redstone, Survival Bases, Mega Builds, PvP Builds, Pixel Art
    val difficulty: String, // Easy, Medium, Hard
    val estimatedTime: String, // e.g. "45 Mins", "3 Hours"
    val materials: String, // Serialized list or comma separated block items
    val dimensions: String, // e.g. "10x10x6"
    val instructions: String, // Newline or JSON separated step descriptions
    val tips: String, // Newline or JSON separated pro tips
    val layersJson: String, // 3D block blueprint grid represented as Y, X, Z elements
    val creatorName: String = "Steve", // Creator profile name
    val creatorEmail: String = "steve@minecraft.net",
    val isFavorite: Boolean = false,
    val isShared: Boolean = false, // Uploaded to Gallery
    val likesCount: Int = 0,
    val userLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
