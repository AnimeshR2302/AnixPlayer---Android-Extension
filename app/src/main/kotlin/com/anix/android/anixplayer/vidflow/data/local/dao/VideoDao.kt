package com.anix.android.anixplayer.vidflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anix.android.anixplayer.vidflow.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM video_history ORDER BY lastWatched DESC")
    fun getAllHistory(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM video_history WHERE isBookmarked = 1 ORDER BY lastWatched DESC")
    fun getBookmarks(): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("SELECT * FROM video_history WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("DELETE FROM video_history WHERE id = :id")
    suspend fun deleteVideo(id: String)
}
