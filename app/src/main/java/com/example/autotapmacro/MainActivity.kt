package com.example.autotapmacro

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku
import java.io.DataOutputStream

// --- SHIZUKU HELPER (PERSISTENT SHELL) ---
// Membuka satu proses dan mengirim perintah secara streaming agar SANGAT CEPAT
object ShizukuHelper {
    private var process: Process? = null
    private var os: DataOutputStream? = null

    fun isShizukuRunning() = Shizuku.pingBinder()
    fun hasPermission() = isShizukuRunning() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    fun requestPermission(code: Int) { if (isShizukuRunning() && !hasPermission()) Shizuku.requestPermission(code) }

    fun initShell(): Boolean {
        if (!hasPermission()) return false
        if (os != null) return true // Sudah aktif
        return try {
            val method = Shizuku::class.java.getMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
            process = method.invoke(null, arrayOf("sh"), null, "/") as Process
            os = DataOutputStream(process!!.outputStream)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun tapFast(x: Int, y: Int) {
        try {
            os?.writeBytes("input tap $x $y\n")
            os?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            os = null // Reset jika error
        }
    }

    fun closeShell() {
        try {
            os?.writeBytes("exit\n")
            os?.flush()
            os?.close()
            process?.destroy()
        } catch (e: Exception) {}
        os = null
        process = null
    }
}

// --- MAIN UI ---
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 50, 50, 50) }
        val title = TextView(this).apply { text = "Auto Tap (Fast Persistent)"; textSize = 22f; setPadding(0, 0, 0, 40) }
        
        val btnShizuku = Button(this).apply { text = "1. Minta Izin Shizuku" }
        btnShizuku.setOnClickListener {
            if (ShizukuHelper.isShizukuRunning()) ShizukuHelper.requestPermission(101)
            else Toast.makeText(this, "Shizuku belum aktif!", Toast.LENGTH_SHORT).show()
        }

        val btnOverlay = Button(this).apply { text = "2. Minta Izin Overlay" }
        btnOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            else Toast.makeText(this, "Izin Overlay Aktif", Toast.LENGTH_SHORT).show()
        }

        val btnStart = Button(this).apply { text = "3. Buka Floating Macro" }
        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) return@setOnClickListener Toast.makeText(this, "Izinkan Overlay!", Toast.LENGTH_SHORT).show()
            startForegroundService(Intent(this, TapService::class.java))
            finish()
        }

        layout.addView(title); layout.addView(btnShizuku); layout.addView(btnOverlay); layout.addView(btnStart)
        setContentView(layout)
    }
}
