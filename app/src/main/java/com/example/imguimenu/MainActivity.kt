package com.example.imguimenu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 64, 48, 48) }
        val title = TextView(this).apply { text = "ImGui Overlay Menu"; textSize = 26f }
        val info = TextView(this).apply { text = "Grant overlay permission, then start the floating ImGui menu."; textSize = 16f; setPadding(0,24,0,32) }
        val permission = Button(this).apply { text = "Grant overlay permission"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) } }
        val start = Button(this).apply { text = "Start floating menu"; setOnClickListener { if (Settings.canDrawOverlays(this@MainActivity)) startService(Intent(this@MainActivity, OverlayService::class.java)) else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) } }
        val stop = Button(this).apply { text = "Stop floating menu"; setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) } }
        root.addView(title); root.addView(info); root.addView(permission); root.addView(start); root.addView(stop)
        setContentView(root)
    }
}
