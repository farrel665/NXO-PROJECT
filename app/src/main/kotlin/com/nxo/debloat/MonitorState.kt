package com.nxo.debloat

import android.content.Context

object MonitorState {
    private const val PREF = "monitor"
    private const val LAST = "last"

    fun setLast(context: Context, text: String) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(LAST, text).apply()

    fun last(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(LAST, "—") ?: "—"
}
