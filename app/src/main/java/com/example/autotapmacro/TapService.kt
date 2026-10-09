package com.example.autotapmacro

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

// --- OVERLAY MANAGER ---
@SuppressLint("SetTextI18n")
class OverlayManager(private val context: Context, private val onStartStop: () -> Unit, private val onClose: () -> Unit) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private lateinit var view: LinearLayout
    private lateinit var txtStatus: TextView
    private lateinit var btnStart: Button
    private var params: WindowManager.LayoutParams? = null

    fun show() {
        view = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#CC000000")); setPadding(24, 24, 24, 24)
        }
        txtStatus = TextView(context).apply { text = "IDLE"; setTextColor(Color.WHITE) }
        btnStart = Button(context).apply { text = "START"; setOnClickListener { onStartStop() } }
        val btnClose = Button(context).apply { text = "CLOSE"; setOnClickListener { onClose() } }
        
        view.addView(txtStatus); view.addView(btnStart); view.addView(btnClose)

        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        params = WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 100; y = 100 }
        
        var initX = 0; var initY = 0; var initTouchX = 0f; var initTouchY = 0f
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { initX = params!!.x; initY = params!!.y; initTouchX = event.rawX; initTouchY = event.rawY; true }
                MotionEvent.ACTION_MOVE -> { params!!.x = initX + (event.rawX - initTouchX).toInt(); params!!.y = initY + (event.rawY - initTouchY).toInt(); wm.updateViewLayout(view, params); true }
                else -> false
            }
        }
        wm.addView(view, params)
    }

    fun updateStatus(run: Boolean, taps: Int) {
        txtStatus.text = if(run) "RUNNING\nTaps: $taps" else "IDLE\nTaps: $taps"
        btnStart.text = if(run) "STOP" else "START"
    }
    fun hide() { if(::view.isInitialized) wm.removeView(view) }
}

// --- TAP SERVICE ---
class TapService : Service() {
    private var overlay: OverlayManager? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var taps = 0

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel("macro_ch", "Macro", NotificationManager.IMPORTANCE_LOW))
        }
        startForeground(1, NotificationCompat.Builder(this, "macro_ch").setContentTitle("Macro").setContentText("Running").setSmallIcon(android.R.drawable.ic_menu_manage).build())
        
        overlay = OverlayManager(this, { if (job?.isActive == true) stopTap() else startTap() }, { stopSelf() })
        overlay?.show()
    }

    private fun startTap() {
        val points = PresetRepository(this).loadPoints()
        if (points.isEmpty()) return Toast.makeText(this, "Tambah titik dulu!", Toast.LENGTH_SHORT).show()
        if (!ShizukuHelper.hasPermission()) return Toast.makeText(this, "Izin Shizuku hilang!", Toast.LENGTH_SHORT).show()
        
        taps = 0; overlay?.updateStatus(true, taps)
        job = scope.launch {
            while (isActive) {
                for (p in points) {
                    if (!isActive) break
                    if (!ShizukuHelper.tap(p.x, p.y)) {
                        withContext(Dispatchers.Main) { Toast.makeText(applicationContext, "Shizuku mati!", Toast.LENGTH_SHORT).show(); stopTap() }
                        break
                    }
                    taps++; withContext(Dispatchers.Main) { overlay?.updateStatus(true, taps) }
                    delay(p.delayMs)
                }
            }
        }
    }
    private fun stopTap() { job?.cancel(); overlay?.updateStatus(false, taps) }
    override fun onBind(i: Intent?): IBinder? = null
    override fun onDestroy() { stopTap(); scope.cancel(); overlay?.hide(); super.onDestroy() }
}
