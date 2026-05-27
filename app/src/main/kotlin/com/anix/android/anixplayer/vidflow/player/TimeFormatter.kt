package com.anix.android.anixplayer.vidflow.player

object TimeFormatter {
    fun formatDuration(ms: Long?): String {
        if (ms == null || ms < 0L) return "--:--"

        val totalSeconds = ms / 1_000L
        val seconds = totalSeconds % 60L
        val totalMinutes = totalSeconds / 60L
        val minutes = totalMinutes % 60L
        val hours = totalMinutes / 60L

        return if (hours > 0L) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}
