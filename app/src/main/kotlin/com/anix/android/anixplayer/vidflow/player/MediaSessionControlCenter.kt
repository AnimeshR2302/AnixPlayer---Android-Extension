package com.anix.android.anixplayer.vidflow.player

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MediaSessionControlCenter {
    private val _state = MutableStateFlow(ActiveVideoSessionState.none())
    val state: StateFlow<ActiveVideoSessionState> = _state.asStateFlow()

    private var appContext: Context? = null
    private var sessionManager: MediaSessionManager? = null
    private var selectedController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            selectedController?.let(::publishState)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            selectedController?.let(::publishState)
        }

        override fun onSessionDestroyed() {
            selectedController = null
            _state.value = ActiveVideoSessionState.none()
            appContext?.let(::refreshSessions)
        }
    }

    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        if (sessionManager == null) {
            sessionManager = applicationContext.getSystemService(MediaSessionManager::class.java)
        }
    }

    fun refreshSessions(context: Context) {
        initialize(context)
        if (!OverlayPermissionHelper.isNotificationListenerEnabled(context)) {
            clearSelectedController()
            return
        }

        val manager = sessionManager ?: return
        val componentName = OverlayPermissionHelper.notificationListenerComponent(context)
        val controllers = runCatching { manager.getActiveSessions(componentName) }
            .getOrElse {
                clearSelectedController()
                return
            }

        val nextController = chooseBestController(controllers)
        if (nextController == null) {
            clearSelectedController()
            return
        }

        if (selectedController?.sessionToken != nextController.sessionToken) {
            selectedController?.unregisterCallback(controllerCallback)
            selectedController = nextController
            nextController.registerCallback(controllerCallback)
        }

        publishState(nextController)
    }

    fun release() {
        clearSelectedController()
        appContext = null
        sessionManager = null
    }

    fun seekBy(offsetMs: Long, nowElapsedRealtimeMs: Long = SystemClock.elapsedRealtime()): Boolean {
        val controller = selectedController ?: return false
        val currentState = _state.value
        if (!currentState.canSeek) return false

        val timeline = currentState.timeline(nowElapsedRealtimeMs)
        val targetPosition = SeekGestureMapper.targetPosition(
            currentPositionMs = timeline.elapsedMs,
            offsetMs = offsetMs,
            durationMs = timeline.durationMs,
        ) ?: return false

        controller.transportControls.seekTo(targetPosition)
        publishState(controller)
        return true
    }

    private fun chooseBestController(controllers: List<MediaController>): MediaController? {
        var bestController: MediaController? = null
        var bestRank = Int.MIN_VALUE

        controllers.forEach { controller ->
            val rank = PlaybackStateRanking.rankForState(
                controller.playbackState?.state ?: PlaybackState.STATE_NONE,
            )
            if (bestController == null || rank > bestRank) {
                bestController = controller
                bestRank = rank
            }
        }

        return bestController
    }

    private fun publishState(controller: MediaController) {
        val playbackState = controller.playbackState
        val metadata = controller.metadata
        val duration = metadata?.durationMs()
        val position = playbackState?.position?.takeIf {
            it != PlaybackState.PLAYBACK_POSITION_UNKNOWN
        }

        _state.value = ActiveVideoSessionState(
            packageName = controller.packageName,
            title = metadata?.title(),
            playbackState = playbackState?.state ?: PlaybackState.STATE_NONE,
            positionMs = position,
            durationMs = duration,
            lastPositionUpdateElapsedRealtimeMs = playbackState?.lastPositionUpdateTime ?: 0L,
            playbackSpeed = playbackState?.playbackSpeed ?: 0f,
            canSeek = playbackState?.let {
                PlaybackStateRanking.isSeekActionAvailable(it.actions)
            } ?: false,
        )
    }

    private fun clearSelectedController() {
        selectedController?.unregisterCallback(controllerCallback)
        selectedController = null
        _state.value = ActiveVideoSessionState.none()
    }

    private fun MediaMetadata.durationMs(): Long? =
        if (containsKey(MediaMetadata.METADATA_KEY_DURATION)) {
            getLong(MediaMetadata.METADATA_KEY_DURATION).takeIf { it >= 0L }
        } else {
            null
        }

    private fun MediaMetadata.title(): String? =
        getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
}
