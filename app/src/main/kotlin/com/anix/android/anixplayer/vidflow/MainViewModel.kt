package com.anix.android.anixplayer.vidflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.android.anixplayer.vidflow.data.local.dao.VideoDao
import com.anix.android.anixplayer.vidflow.data.local.entity.VideoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val videoDao: VideoDao
) : ViewModel() {

    private val _subtitlePath = MutableStateFlow<String?>(null)
    val subtitlePath: StateFlow<String?> = _subtitlePath.asStateFlow()

    fun loadVideoInfo(videoUriString: String, title: String) {
        viewModelScope.launch {
            val entity = videoDao.getVideoById(videoUriString)
            if (entity != null) {
                _subtitlePath.value = entity.subtitlePath
            } else {
                _subtitlePath.value = null
                // Insert a placeholder to track this video history later
                videoDao.insertVideo(
                    VideoEntity(
                        id = videoUriString,
                        title = title,
                        filePath = videoUriString,
                        lastPosition = 0L,
                        duration = 0L,
                        lastWatched = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun updateSubtitle(videoUriString: String, subtitleUriString: String?) {
        viewModelScope.launch {
            videoDao.updateSubtitlePath(videoUriString, subtitleUriString)
            _subtitlePath.value = subtitleUriString
        }
    }
}
