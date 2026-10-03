package com.example.imguioverlay

import android.app.*
import android.content.pm.ServiceInfo
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import kotlin.math.max

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var root: FrameLayout
    private lateinit var gl: ImGuiSurface
    private lateinit var bubble: TextView
    private lateinit var lp: WindowManager.LayoutParams
    private var panel = false
    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification = NotificationCompat.Builder(this, "imgui")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("ImGui Overlay")
            .setContentText("Floating menu is running")
            .setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(1001, notification)
        showOverlay()
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        root = FrameLayout(this)
        gl = ImGuiSurface(this)
        bubble = TextView(this).apply {
            text = "IM"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(90, 50, 170))
            elevation = 12f
            setOnTouchListener { _, e ->
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> { downX=e.rawX; downY=e.rawY; startX=lp.x; startY=lp.y; true }
                    MotionEvent.ACTION_MOVE -> { lp.x=startX+(e.rawX-downX).toInt(); lp.y=startY+(e.rawY-downY).toInt(); if(!panel) wm.updateViewLayout(root,lp); true }
                    MotionEvent.ACTION_UP -> { if(kotlin.math.abs(e.rawX-downX)<12 && kotlin.math.abs(e.rawY-downY)<12) togglePanel(); true }
                    else -> true
                }
            }
        }
        root.addView(gl, FrameLayout.LayoutParams(1,1))
        root.addView(bubble, FrameLayout.LayoutParams(64,64))
        lp = WindowManager.LayoutParams(64,64, if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP or Gravity.START; x=24; y=180 }
        wm.addView(root, lp)
    }

    private fun togglePanel() {
        panel = !panel
        if (panel) {
            bubble.visibility = View.GONE
            gl.visibility = View.VISIBLE
            lp.width = dp(360); lp.height = dp(560)
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            gl.visibility = View.GONE
            bubble.visibility = View.VISIBLE
            lp.width = dp(64); lp.height = dp(64)
        }
        wm.updateViewLayout(root, lp)
    }

    fun hidePanel() { if(panel) togglePanel() }
    private fun dp(v:Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() { if(::root.isInitialized) try { wm.removeView(root) } catch(_:Exception){}; super.onDestroy() }
    override fun onBind(intent: Intent?) = null

    private fun createChannel() {
        if(Build.VERSION.SDK_INT>=26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("imgui","ImGui Overlay",NotificationManager.IMPORTANCE_LOW))
    }

    inner class ImGuiSurface(context: Context) : GLSurfaceView(context) {
        private val renderer = NativeRenderer()
        init { setEGLContextClientVersion(3); setRenderer(renderer); renderMode=RENDERMODE_CONTINUOUSLY; setFocusable(true); setFocusableInTouchMode(true) }
        override fun onTouchEvent(e: MotionEvent): Boolean {
            val x=e.x; val y=e.y
            NativeBridge.touch(e.actionMasked, x, y, e.actionMasked==MotionEvent.ACTION_DOWN || e.actionMasked==MotionEvent.ACTION_MOVE)
            return true
        }
    }

    inner class NativeRenderer : Renderer {
        override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) = NativeBridge.init()
        override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width:Int, height:Int) = NativeBridge.resize(width,height)
        override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) { if (NativeBridge.render()) post { hidePanel() } }
    }
}
