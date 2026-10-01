package com.nxo.debloat

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class GameNotificationListener : NotificationListenerService() {
    companion object {
        @Volatile var listener: ((String) -> Unit)? = null
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val title = sbn.notification.extras?.getCharSequence("android.title")?.toString().orEmpty()
        val text = "${sbn.packageName}${if (title.isNotBlank()) " • $title" else ""}"
        NotificationState.setLast(this, text)
        listener?.invoke(text)
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val text = "Removed: ${sbn.packageName}"
        NotificationState.setLast(this, text); listener?.invoke(text)
    }
}
