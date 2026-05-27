package com.anix.android.anixplayer.vidflow.player

import android.service.notification.NotificationListenerService

class AnixNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        MediaSessionControlCenter.refreshSessions(this)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        MediaSessionControlCenter.release()
    }

    override fun onNotificationPosted(sbn: android.service.notification.StatusBarNotification?) {
        MediaSessionControlCenter.refreshSessions(this)
    }

    override fun onNotificationRemoved(sbn: android.service.notification.StatusBarNotification?) {
        MediaSessionControlCenter.refreshSessions(this)
    }
}
