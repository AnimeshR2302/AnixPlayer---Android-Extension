package com.anix.android.anixplayer.vidflow.player

import android.media.session.PlaybackState

object PlaybackStateRanking {
    const val STATE_NONE: Int = PlaybackState.STATE_NONE

    fun rankForState(state: Int): Int = when (state) {
        PlaybackState.STATE_PLAYING -> 3
        PlaybackState.STATE_BUFFERING,
        PlaybackState.STATE_FAST_FORWARDING,
        PlaybackState.STATE_REWINDING -> 2
        PlaybackState.STATE_PAUSED -> 1
        else -> 0
    }

    fun isActivelyAdvancing(state: Int): Boolean = state == PlaybackState.STATE_PLAYING ||
        state == PlaybackState.STATE_FAST_FORWARDING

    fun isSeekActionAvailable(actions: Long): Boolean =
        actions and PlaybackState.ACTION_SEEK_TO != 0L
}
