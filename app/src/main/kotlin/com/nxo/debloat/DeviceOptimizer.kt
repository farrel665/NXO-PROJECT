package com.nxo.debloat

import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager

object DeviceOptimizer {
    const val FF = "com.dts.freefireth"
    const val FF_MAX = "com.dts.freefiremax"

    fun installed(pkg: String): Boolean = ShizukuShell.run("pm path ${ShizukuShell.q(pkg)} 2>/dev/null").ok

    /** Real ART compilation. 'speed-profile' is profile-guided AOT/JIT-friendly compilation. */
    fun compileProfile(pkg: String) = ShizukuShell.run("cmd package compile -m speed-profile -f ${ShizukuShell.q(pkg)}")
    fun compileSpeed(pkg: String) = ShizukuShell.run("cmd package compile -m speed -f ${ShizukuShell.q(pkg)}")
    fun runBgDexopt() = ShizukuShell.run("cmd package bg-dexopt-job")
    fun requestPerformanceMode(pkg: String) = ShizukuShell.run("cmd game mode performance ${ShizukuShell.q(pkg)}")

    /** Trims package caches without clearing app data. */
    fun cleanCache() = ShizukuShell.run("pm trim-caches 2G")

    fun surfaceDiagnostics() = ShizukuShell.run("dumpsys SurfaceFlinger | grep -i -E 'refresh|fps|vsync|frame rate|display' | head -n 160")
    fun gfxDiagnostics(pkg: String) = ShizukuShell.run("dumpsys gfxinfo ${ShizukuShell.q(pkg)} framestats | head -n 220")

    /**
     * Only writes settings if the device exposes the standard refresh-rate keys.
     * OEMs may ignore these values. No claim is made that this changes hardware capability.
     */
    fun setRefreshRate(hz: Float): ShizukuShell.Result {
        val v = "%.2f".format(java.util.Locale.US, hz)
        return ShizukuShell.run("settings put system peak_refresh_rate $v; settings put system min_refresh_rate $v")
    }

    /**
     * Some Android/OEM builds expose a debug SurfaceFlinger switch. We probe first.
     * This is deliberately opt-in because these properties are device/build dependent.
     */
    fun vsyncCapability() = ShizukuShell.run("getprop | grep -i -E 'vsync|surfaceflinger' | head -n 120")

    fun displayHz(context: Context): Float {
        val wm = context.getSystemService(WindowManager::class.java)
        val display = wm?.defaultDisplay ?: return 60f
        return if (Build.VERSION.SDK_INT >= 23) display.mode.refreshRate else 60f
    }

    fun allSupportedRefreshRates(context: Context): List<Float> {
        val display = context.getSystemService(WindowManager::class.java)?.defaultDisplay ?: return listOf(60f)
        return if (Build.VERSION.SDK_INT >= 23) display.supportedModes.map { it.refreshRate }.distinct().sorted() else listOf(60f)
    }
}
