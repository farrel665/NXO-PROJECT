package com.example.imguimenu

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.WindowManager
import kotlin.math.abs
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImGuiOverlayView(context: Context, private val wm: WindowManager) : GLSurfaceView(context) {
    private var lp: WindowManager.LayoutParams? = null
    private var visiblePanel = false
    private var downX = 0f; private var downY = 0f
    private var startX = 0; private var startY = 0

    private external fun nativeInit()
    private external fun nativeResize(width: Int, height: Int)
    private external fun nativeRender()
    private external fun nativeTouch(action: Int, x: Float, y: Float)
    private external fun nativeConsumeHideRequest(): Boolean
    private external fun nativeDestroy()

    init {
        System.loadLibrary("imgui_overlay")
        setEGLContextClientVersion(3)
        setPreserveEGLContextOnPause(true)
        setRenderer(object : Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) { nativeInit() }
            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) { nativeResize(width, height) }
            override fun onDrawFrame(gl: GL10?) {
                nativeRender()
                if (nativeConsumeHideRequest()) post { setPanel(false) }
            }
        })
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun attach(params: WindowManager.LayoutParams) { lp = params }

    override fun onDetachedFromWindow() { nativeDestroy(); super.onDetachedFromWindow() }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!visiblePanel) {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downX=e.rawX; downY=e.rawY; startX=lp?.x ?: 0; startY=lp?.y ?: 0 }
                MotionEvent.ACTION_MOVE -> lp?.let { it.x=startX+(e.rawX-downX).toInt(); it.y=startY+(e.rawY-downY).toInt(); wm.updateViewLayout(this,it) }
                MotionEvent.ACTION_UP -> if (abs(e.rawX-downX)<20 && abs(e.rawY-downY)<20) setPanel(true)
            }
            return true
        }
        nativeTouch(e.actionMasked, e.x, e.y)
        return true
    }

    fun setPanel(open: Boolean) {
        visiblePanel=open
        lp?.let { p ->
            if (open) {
                p.width = (resources.displayMetrics.widthPixels * .92f).toInt()
                p.height = (resources.displayMetrics.heightPixels * .72f).toInt()
                p.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                p.x = (resources.displayMetrics.widthPixels-p.width)/2
                p.y = (resources.displayMetrics.heightPixels-p.height)/3
            } else {
                p.width=160; p.height=160
                p.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                p.x=24; p.y=180
            }
            wm.updateViewLayout(this,p); requestRender()
        }
    }
}
