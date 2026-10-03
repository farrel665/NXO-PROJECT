package com.example.imguimenu

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var overlay: ImGuiOverlayView? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        overlay = ImGuiOverlayView(this, wm)
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val lp = WindowManager.LayoutParams(160, 160, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 24; y = 180 }
        overlay!!.attach(lp)
        wm.addView(overlay, lp)
    }

    override fun onDestroy() { overlay?.let { runCatching { wm.removeView(it) } }; overlay = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
