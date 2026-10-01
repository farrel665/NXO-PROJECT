package com.nxo.debloat

object GameTools {
    const val FF = "com.dts.freefireth"
    const val MAX = "com.dts.freefiremax"

    fun installed(pkg: String): Boolean =
        Shell.run("pm path ${Shell.q(pkg)} 2>/dev/null").ok

    fun compileSpeed(pkg: String) =
        Shell.run("cmd package compile -m speed -f ${Shell.q(pkg)}")

    fun compileSpeedProfile(pkg: String) =
        Shell.run("cmd package compile -m speed-profile -f ${Shell.q(pkg)}")

    /*
     * ART/JIT note:
     * JIT is managed by ART at runtime. There is no universal supported
     * shell command that turns a game into "100% JIT" permanently.
     * These operations expose real ART/package compilation modes instead
     * of presenting a fake JIT percentage.
     */
    fun clearAppCache(pkg: String): Shell.Result =
        Shell.run("pm clear --cache-only ${Shell.q(pkg)}")

    fun trimCaches(): Shell.Result =
        Shell.run("pm trim-caches 2147483648")

    fun currentRefreshRate(): Shell.Result =
        Shell.run("dumpsys display | grep -i -E 'mRefreshRate|refreshRate|DisplayMode' | head -n 80")

    fun inputDiagnostics(): Shell.Result =
        Shell.run("dumpsys input | grep -i -E 'touch|device|sources|keyboard' | head -n 120")

    fun gpuSurfaceDiagnostics(): Shell.Result =
        Shell.run("dumpsys SurfaceFlinger | grep -i -E 'refresh|vsync|frame|display' | head -n 120")

    /*
     * These are best-effort Android settings. OEMs may ignore them.
     * We never claim that they override the physical panel limit.
     */
    fun requestMaxRefreshRate(hz: Float): Shell.Result {
        val h = hz.toString()
        return Shell.run(
            "settings put system peak_refresh_rate $h; " +
            "settings put system min_refresh_rate $h"
        )
    }

    fun resetRefreshRate(): Shell.Result =
        Shell.run(
            "settings delete system peak_refresh_rate; " +
            "settings delete system min_refresh_rate"
        )

    /*
     * There is no universal public Android switch for disabling VSYNC
     * system-wide. We therefore report SurfaceFlinger state rather than
     * writing undocumented debug properties that may destabilize a device.
     */
    fun vsyncDiagnostics(): Shell.Result =
        Shell.run("dumpsys SurfaceFlinger | grep -i -E 'vsync|present|frame timeline' | head -n 100")
}
