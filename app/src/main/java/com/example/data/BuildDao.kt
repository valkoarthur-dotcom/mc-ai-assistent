package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildDao {
    @Query("SELECT * FROM builds ORDER BY timestamp DESC")
    fun getAllBuilds(): Flow<List<MinecraftBuild>>

    @Query("SELECT * FROM builds WHERE id = :id LIMIT 1")
    suspend fun getBuildById(id: Int): MinecraftBuild?

    @Query("SELECT * FROM builds WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteBuilds(): Flow<List<MinecraftBuild>>

    @Query("SELECT * FROM builds WHERE isShared = 1 ORDER BY likesCount DESC, timestamp DESC")
    fun getSharedBuilds(): Flow<List<MinecraftBuild>>

    @Query("SELECT * FROM builds WHERE isShared = 1 AND category = :category ORDER BY likesCount DESC")
    fun getSharedBuildsByCategory(category: String): Flow<List<MinecraftBuild>>

    @Query("SELECT COUNT(*) FROM builds")
    suspend fun getTotalBuildsCount(): Int

    @Query("SELECT COUNT(*) FROM builds WHERE creatorEmail = :email")
    suspend fun getBuildsCountByCreator(email: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: MinecraftBuild): Long

    @Update
    suspend fun updateBuild(build: MinecraftBuild)

    @Delete
    suspend fun deleteBuild(build: MinecraftBuild)

    @Query("DELETE FROM builds WHERE id = :id")
    suspend fun deleteBuildById(id: Int)
}
