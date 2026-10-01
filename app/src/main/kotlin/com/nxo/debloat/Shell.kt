package com.nxo.debloat

import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader

object Shell {
    data class Result(val code: Int, val stdout: String, val stderr: String) {
        val ok get() = code == 0
        val text get() = listOf(stdout, stderr).filter { it.isNotBlank() }.joinToString("\n")
    }

    fun available(): Boolean =
        Shizuku.pingBinder() &&
        Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED

    fun run(command: String): Result {
        if (!Shizuku.pingBinder()) return Result(-1, "", "Shizuku daemon is not running.")
        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED)
            return Result(-1, "", "Shizuku permission not granted.")

        var process: ShizukuRemoteProcess? = null
        return try {
            process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)

            val out = StringBuilder()
            val err = StringBuilder()
            val outReader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))

            val a = Thread {
                try { outReader.forEachLine { out.append(it).append('\n') } } catch (_: Exception) {}
            }
            val b = Thread {
                try { errReader.forEachLine { err.append(it).append('\n') } } catch (_: Exception) {}
            }
            a.start(); b.start()
            val code = process.waitFor()
            a.join(3000); b.join(3000)

            Result(code, out.toString().trim(), err.toString().trim())
        } catch (e: Exception) {
            Result(-1, "", e.stackTraceToString())
        } finally {
            try { process?.destroy() } catch (_: Exception) {}
        }
    }

    fun q(s: String) = "'" + s.replace("'", "'\\''") + "'"
}
