package com.nxo.debloat

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object NotificationState {
    private const val PREF = "nxo_monitor"
    private const val LAST = "last"

    fun enabled(context: Context): Boolean {
        val raw = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        val expected = ComponentName(context, GameNotificationListener::class.java).flattenToString()
        return raw.split(":").any { it.equals(expected, true) }
    }
    fun setLast(context: Context, text: String) = context.getSharedPreferences(PREF, 0).edit().putString(LAST, text).apply()
    fun last(context: Context): String = context.getSharedPreferences(PREF, 0).getString(LAST, "—") ?: "—"
}
