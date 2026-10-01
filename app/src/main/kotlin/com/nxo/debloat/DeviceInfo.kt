package com.nxo.debloat

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display

data class DisplayInfo(val supportedHz: List<Float>, val maxHz: Float)

object DeviceInfo {
    fun displayInfo(context: Context): DisplayInfo {
        val dm = context.getSystemService(DisplayManager::class.java)
        val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)
        val modes = display?.supportedModes.orEmpty()
        val rates = modes.map { it.refreshRate }.filter { it > 0f }.distinct().sorted()
        return DisplayInfo(rates, rates.maxOrNull() ?: display?.refreshRate ?: 60f)
    }

    fun summary(context: Context): String {
        val d = displayInfo(context)
        return "${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.SDK_INT} • max ${"%.2f".format(d.maxHz)} Hz"
    }
}
