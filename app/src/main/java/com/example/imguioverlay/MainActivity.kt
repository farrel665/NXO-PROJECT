package com.example.imguioverlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(48,48,48,48) }
        val title = TextView(this).apply { text = "ImGui Overlay Menu"; textSize = 24f; gravity = Gravity.CENTER }
        val permission = Button(this).apply { text = "Grant overlay permission" }
        val start = Button(this).apply { text = "Start floating menu" }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(permission, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 32 })
        root.addView(start, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        permission.setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
        start.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                startForegroundService(Intent(this, OverlayService::class.java))
            }
        }
    }
}
