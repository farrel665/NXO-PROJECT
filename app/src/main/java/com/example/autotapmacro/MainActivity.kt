package com.example.autotapmacro

import android.content.Context
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import rikka.shizuku.Shizuku

// --- DATA & PRESET ---
data class TapPoint(val x: Int, val y: Int, var delayMs: Long = 1000L)

class PresetRepository(context: Context) {
    private val prefs = context.getSharedPreferences("macro_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    fun savePoints(points: List<TapPoint>) = prefs.edit().putString("points", gson.toJson(points)).apply()
    fun loadPoints(): MutableList<TapPoint> {
        val json = prefs.getString("points", null) ?: return mutableListOf()
        return try { gson.fromJson(json, object : TypeToken<MutableList<TapPoint>>() {}.type) } catch (e: Exception) { mutableListOf() }
    }
}

// --- SHIZUKU HELPER ---
object ShizukuHelper {
    fun isShizukuRunning() = Shizuku.pingBinder()
    fun hasPermission() = isShizukuRunning() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    fun requestPermission(code: Int) { if (isShizukuRunning() && !hasPermission()) Shizuku.requestPermission(code) }
    
    fun tap(x: Int, y: Int): Boolean {
        if (!hasPermission()) return false
        return try { 
            // PERBAIKAN FINAL: 
            // Menggunakan Java Reflection untuk menghindari bug visibilitas (private) pada Kotlin.
            // Ini memaksa pemanggilan fungsi Shizuku.newProcess() tanpa dicegat compiler Kotlin.
            val method = Shizuku::class.java.getMethod(
                "newProcess", 
                Array<String>::class.java, 
                Array<String>::class.java, 
                String::class.java
            )
            val cmd = arrayOf("sh", "-c", "input tap $x $y")
            val process = method.invoke(null, cmd, null, null) as java.lang.Process
            
            process.waitFor() == 0 
        } catch (e: Exception) { 
            e.printStackTrace()
            false 
        }
    }
}

// --- MAIN UI ---
class MainActivity : AppCompatActivity() {
    private lateinit var repo: PresetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = PresetRepository(this)
        
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 50, 50, 50) }
        val title = TextView(this).apply { text = "Auto Tap Macro (Shizuku)"; textSize = 20f; setPadding(0, 0, 0, 40) }
        
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

        val btnAdd = Button(this).apply { text = "3. Tambah Tap Center (500,1000)" }
        btnAdd.setOnClickListener {
            val points = repo.loadPoints()
            points.add(TapPoint(500, 1000, 1000))
            repo.savePoints(points)
            Toast.makeText(this, "Tersimpan! Total titik: ${points.size}", Toast.LENGTH_SHORT).show()
        }

        val btnStart = Button(this).apply { text = "4. Buka Floating Macro" }
        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) return@setOnClickListener Toast.makeText(this, "Izinkan Overlay!", Toast.LENGTH_SHORT).show()
            startForegroundService(Intent(this, TapService::class.java))
            finish()
        }

        layout.addView(title); layout.addView(btnShizuku); layout.addView(btnOverlay); layout.addView(btnAdd); layout.addView(btnStart)
        setContentView(layout)
    }
}
