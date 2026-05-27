package com.anix.android.anixplayer.vidflow.player

import kotlin.math.abs
import kotlin.math.roundToLong

object SeekGestureMapper {
    fun offsetForDrag(
        dragDistancePx: Float,
        overlayWidthPx: Int,
        durationMs: Long?,
        config: SeekGestureConfig = SeekGestureConfig(),
    ): Long {
        if (overlayWidthPx <= 0 || dragDistancePx == 0f) return 0L

        val fullWidthSeekMs = fullWidthSeekMs(durationMs, config)
        val widthFraction = (abs(dragDistancePx) / overlayWidthPx).coerceIn(0f, 1f)
        val offset = (fullWidthSeekMs * widthFraction).roundToLong()

        return if (dragDistancePx < 0f) -offset else offset
    }

    fun targetPosition(
        currentPositionMs: Long?,
        offsetMs: Long,
        durationMs: Long?,
    ): Long? {
        if (currentPositionMs == null) return null

        val unclamped = currentPositionMs + offsetMs
        return if (durationMs != null && durationMs >= 0L) {
            unclamped.coerceIn(0L, durationMs)
        } else {
            unclamped.coerceAtLeast(0L)
        }
    }

    private fun fullWidthSeekMs(durationMs: Long?, config: SeekGestureConfig): Long {
        val knownDuration = durationMs?.takeIf { it >= 0L }
            ?: return config.unknownDurationFullWidthSeekMs

        return (knownDuration * config.knownDurationFraction)
            .roundToLong()
            .coerceAtMost(config.maxKnownDurationFullWidthSeekMs)
            .coerceAtLeast(1L)
    }
}
