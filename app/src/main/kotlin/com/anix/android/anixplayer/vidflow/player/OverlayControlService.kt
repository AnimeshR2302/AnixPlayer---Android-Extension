package com.anix.android.anixplayer.vidflow.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.anix.android.anixplayer.R
import com.anix.android.anixplayer.vidflow.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import kotlin.math.abs

class OverlayControlService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager

    private var overlayRoot: FrameLayout? = null
    private var statusText: TextView? = null
    private var timeText: TextView? = null
    private var previewText: TextView? = null
    private var settingsPanel: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WindowManager::class.java)
        MediaSessionControlCenter.initialize(this)
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        scope.launch {
            MediaSessionControlCenter.state.collectLatest { state ->
                updatePlaybackViews(state)
            }
        }
        scope.launch {
            while (isActive) {
                MediaSessionControlCenter.refreshSessions(this@OverlayControlService)
                updatePlaybackViews(MediaSessionControlCenter.state.value)
                delay(1_000L)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        showOverlay()
        return START_STICKY
    }

    override fun onDestroy() {
        overlayRoot?.let(windowManager::removeView)
        overlayRoot = null
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay() {
        if (overlayRoot != null) return

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setOnTouchListener(SeekTouchListener())
        }

        val topBar = FrameLayout(this).apply {
            setPadding(dp(12), dp(12), dp(12), 0)
        }
        root.addView(
            topBar,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(64),
                Gravity.TOP,
            ),
        )

        val closeButton = overlayButton("X").apply {
            setOnClickListener { stopSelf() }
        }
        topBar.addView(
            closeButton,
            FrameLayout.LayoutParams(dp(48), dp(48), Gravity.START or Gravity.TOP),
        )

        val settingsButton = overlayButton("...").apply {
            setOnClickListener { toggleSettingsPanel() }
        }
        topBar.addView(
            settingsButton,
            FrameLayout.LayoutParams(dp(48), dp(48), Gravity.END or Gravity.TOP),
        )

        settingsPanel = TextView(this).apply {
            text = context.getString(R.string.settings_title_text)
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            background = roundedBackground(Color.argb(230, 30, 30, 30), dp(8).toFloat())
            visibility = View.GONE
        }.also { panel ->
            root.addView(
                panel,
                FrameLayout.LayoutParams(dp(144), dp(56), Gravity.TOP or Gravity.END).apply {
                    topMargin = dp(68)
                    rightMargin = dp(12)
                },
            )
        }

        previewText = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = roundedBackground(Color.argb(220, 20, 20, 20), dp(10).toFloat())
            visibility = View.GONE
        }.also { preview ->
            root.addView(
                preview,
                FrameLayout.LayoutParams(dp(176), dp(64), Gravity.CENTER),
            )
        }

        val bottomStrip = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = roundedBackground(Color.argb(230, 18, 18, 18), dp(8).toFloat())
        }
        statusText = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 14f
            maxLines = 1
        }
        timeText = TextView(this).apply {
            setTextColor(Color.rgb(210, 210, 210))
            textSize = 13f
            maxLines = 1
        }
        bottomStrip.addView(statusText)
        bottomStrip.addView(timeText)
        root.addView(
            bottomStrip,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM,
            ).apply {
                leftMargin = dp(12)
                rightMargin = dp(12)
                bottomMargin = dp(18)
            },
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(root, params)
        overlayRoot = root
        updatePlaybackViews(MediaSessionControlCenter.state.value)
    }

    private fun updatePlaybackViews(state: ActiveVideoSessionState) {
        val now = android.os.SystemClock.elapsedRealtime()
        val timeline = state.timeline(now)

        statusText?.text = if (state.hasController) {
            state.title ?: state.packageName ?: "Active video"
        } else {
            "No controllable video detected"
        }

        timeText?.text = buildString {
            append(TimeFormatter.formatDuration(timeline.elapsedMs))
            append(" / ")
            append(TimeFormatter.formatDuration(timeline.durationMs))
            append("  -")
            append(TimeFormatter.formatDuration(timeline.remainingMs))
            if (state.hasController && !state.canSeek) {
                append("  Seek unavailable")
            }
        }
    }

    private fun toggleSettingsPanel() {
        settingsPanel?.let { panel ->
            panel.visibility = if (panel.isVisible) View.GONE else View.VISIBLE
        }
    }

    private fun updateSeekPreview(dragDistancePx: Float, overlayWidthPx: Int) {
        val state = MediaSessionControlCenter.state.value
        if (!state.hasController || !state.canSeek) {
            previewText?.text = getString(R.string.seek_unavailable_info_text)
            previewText?.visibility = View.VISIBLE
            return
        }

        val timeline = state.timeline(android.os.SystemClock.elapsedRealtime())
        val offset = SeekGestureMapper.offsetForDrag(
            dragDistancePx = dragDistancePx,
            overlayWidthPx = overlayWidthPx,
            durationMs = timeline.durationMs,
        )
        val target = SeekGestureMapper.targetPosition(
            currentPositionMs = timeline.elapsedMs,
            offsetMs = offset,
            durationMs = timeline.durationMs,
        )
        val sign = if (offset >= 0L) "+" else "-"
        previewText?.text = buildString {
            append(sign)
            append(TimeFormatter.formatDuration(abs(offset)))
            append(TimeFormatter.formatDuration(target))
        }
        previewText?.visibility = View.VISIBLE
    }

    private fun commitSeek(dragDistancePx: Float, overlayWidthPx: Int) {
        val state = MediaSessionControlCenter.state.value
        val timeline = state.timeline(android.os.SystemClock.elapsedRealtime())
        val offset = SeekGestureMapper.offsetForDrag(
            dragDistancePx = dragDistancePx,
            overlayWidthPx = overlayWidthPx,
            durationMs = timeline.durationMs,
        )
        if (offset != 0L) {
            MediaSessionControlCenter.seekBy(offset)
        }
    }

    private fun overlayButton(label: String): TextView = TextView(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = roundedBackground(Color.argb(210, 24, 24, 24), dp(24).toFloat())
    }

    private fun roundedBackground(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = radius
        }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Player overlay",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, OverlayControlService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("AnixPlayer overlay")
            .setContentText("Listening for active video playback")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_launcher_foreground),
                    "Close",
                    stopIntent,
                ).build(),
            )
            .build()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private inner class SeekTouchListener : View.OnTouchListener {
        private var downX = 0f
        private var tracking = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    tracking = true
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!tracking) return false
                    updateSeekPreview(event.rawX - downX, view.width)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (!tracking) return false
                    commitSeek(event.rawX - downX, view.width)
                    previewText?.visibility = View.GONE
                    tracking = false
                    return true
                }

                MotionEvent.ACTION_CANCEL -> {
                    previewText?.visibility = View.GONE
                    tracking = false
                    return true
                }
            }

            if(!tracking) view.performClick()
            return false
        }
    }

    companion object {
        private const val CHANNEL_ID = "player_overlay"
        private const val NOTIFICATION_ID = 4107
        private const val ACTION_STOP = "com.anix.android.anixplayer.STOP_OVERLAY"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, OverlayControlService::class.java),
            )
        }
    }
}
