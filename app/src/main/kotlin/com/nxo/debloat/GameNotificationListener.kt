package com.nxo.debloat

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class GameNotificationListener : NotificationListenerService() {
    companion object {
        var listener: ((String) -> Unit)? = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val title = try {
            sbn.notification.extras.getCharSequence("android.title")?.toString().orEmpty()
        } catch (_: Exception) { "" }

        val text = "${sbn.packageName}${if (title.isNotBlank()) " • $title" else ""}"
        MonitorState.setLast(this, text)
        listener?.invoke(text)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val text = "Removed: ${sbn.packageName}"
        MonitorState.setLast(this, text)
        listener?.invoke(text)
    }
}
