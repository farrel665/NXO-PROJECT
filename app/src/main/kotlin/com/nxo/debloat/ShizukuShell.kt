package com.nxo.debloat

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuShell {
    data class Result(val code: Int, val stdout: String = "", val stderr: String = "") {
        val ok get() = code == 0
        val text get() = listOf(stdout, stderr).filter { it.isNotBlank() }.joinToString("\n")
    }

    fun ready(): Boolean = Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun run(command: String): Result {
        if (!Shizuku.pingBinder()) return Result(-1, stderr = "Shizuku service is not running.")
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            return Result(-1, stderr = "Shizuku permission is not granted.")
        }
        var process: ShizukuRemoteProcess? = null
        return try {
            process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val out = StringBuilder(); val err = StringBuilder()
            val t1 = Thread { read(process!!.inputStream, out) }
            val t2 = Thread { read(process!!.errorStream, err) }
            t1.start(); t2.start()
            val code = process.waitFor()
            t1.join(3000); t2.join(3000)
            Result(code, out.toString().trim(), err.toString().trim())
        } catch (e: Exception) {
            Result(-1, stderr = e.toString())
        } finally { try { process?.destroy() } catch (_: Exception) {} }
    }

    private fun read(input: java.io.InputStream, target: StringBuilder) {
        try { BufferedReader(InputStreamReader(input)).useLines { lines -> lines.forEach { target.append(it).append('\n') } } }
        catch (_: Exception) {}
    }

    fun q(value: String): String = "'" + value.replace("'", "'\\''") + "'"
}
