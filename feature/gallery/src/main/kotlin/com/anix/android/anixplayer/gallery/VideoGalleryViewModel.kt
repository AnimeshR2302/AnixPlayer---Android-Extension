package com.anix.android.anixplayer.gallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LocalVideo(
    val uri: Uri,
    val title: String,
    val duration: Long,
    val size: Long
)

@HiltViewModel
class VideoGalleryViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _videos = MutableStateFlow<List<LocalVideo>>(emptyList())
    val videos: StateFlow<List<LocalVideo>> = _videos.asStateFlow()

    fun loadVideos() {
        viewModelScope.launch {
            val videoList = fetchVideosFromMediaStore(context)
            _videos.value = videoList
        }
    }

    private suspend fun fetchVideosFromMediaStore(context: Context): List<LocalVideo> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<LocalVideo>()
        
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )
        
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                videoList += LocalVideo(contentUri, name, duration, size)
            }
        }
        videoList
    }
}
