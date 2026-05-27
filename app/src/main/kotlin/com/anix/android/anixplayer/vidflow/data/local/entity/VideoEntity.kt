package com.anix.android.anixplayer.vidflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_history")
data class VideoEntity(
    @PrimaryKey val id: String, // Can be a URI or a hash
    val title: String,
    val filePath: String,
    val lastPosition: Long,
    val duration: Long,
    val lastWatched: Long,
    val isBookmarked: Boolean = false,
    val subtitlePath: String? = null
)
