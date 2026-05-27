package com.anix.android.anixplayer.vidflow.player

data class SeekGestureConfig(
    val unknownDurationFullWidthSeekMs: Long = 60_000L,
    val maxKnownDurationFullWidthSeekMs: Long = 300_000L,
    val knownDurationFraction: Float = 0.2f,
)

data class PlaybackTimelineState(
    val elapsedMs: Long?,
    val durationMs: Long?,
) {
    val remainingMs: Long?
        get() = if (elapsedMs != null && durationMs != null) {
            (durationMs - elapsedMs).coerceAtLeast(0L)
        } else {
            null
        }
}

data class ActiveVideoSessionState(
    val packageName: String?,
    val title: String?,
    val playbackState: Int,
    val positionMs: Long?,
    val durationMs: Long?,
    val lastPositionUpdateElapsedRealtimeMs: Long,
    val playbackSpeed: Float,
    val canSeek: Boolean,
) {
    val hasController: Boolean = packageName != null

    fun timeline(nowElapsedRealtimeMs: Long): PlaybackTimelineState {
        if (positionMs == null) {
            return PlaybackTimelineState(elapsedMs = null, durationMs = normalizedDuration())
        }

        val projectedPosition = if (
            PlaybackStateRanking.isActivelyAdvancing(playbackState) &&
            playbackSpeed > 0f &&
            lastPositionUpdateElapsedRealtimeMs > 0L
        ) {
            val elapsedSinceUpdate = (nowElapsedRealtimeMs - lastPositionUpdateElapsedRealtimeMs)
                .coerceAtLeast(0L)
            positionMs + (elapsedSinceUpdate * playbackSpeed).toLong()
        } else {
            positionMs
        }

        val duration = normalizedDuration()
        val clampedPosition = if (duration != null) {
            projectedPosition.coerceIn(0L, duration)
        } else {
            projectedPosition.coerceAtLeast(0L)
        }

        return PlaybackTimelineState(elapsedMs = clampedPosition, durationMs = duration)
    }

    private fun normalizedDuration(): Long? = durationMs?.takeIf { it >= 0L }

    companion object {
        fun none(): ActiveVideoSessionState = ActiveVideoSessionState(
            packageName = null,
            title = null,
            playbackState = PlaybackStateRanking.STATE_NONE,
            positionMs = null,
            durationMs = null,
            lastPositionUpdateElapsedRealtimeMs = 0L,
            playbackSpeed = 0f,
            canSeek = false,
        )
    }
}
