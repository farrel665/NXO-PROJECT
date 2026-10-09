package com.example.autotapmacro

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

@SuppressLint("SetTextI18n")
class OverlayManager(
    private val context: Context, 
    private val onPlayStop: (Boolean) -> Unit, 
    private val onClose: () -> Unit
) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    // Panel Kontrol
    private lateinit var controlView: LinearLayout
    private lateinit var btnPlay: Button
    private lateinit var txtDelay: TextView
    
    // Target Lingkaran
    private lateinit var targetView: View
    private val targetSize = 120 // Ukuran lingkaran (pixel)
    
    var delayMs = 100L
    private var isPlaying = false
    
    private var controlParams: WindowManager.LayoutParams? = null
    private var targetParams: WindowManager.LayoutParams? = null

    fun show() {
        setupControlView()
        setupTargetView()
    }

    private fun setupControlView() {
        controlView = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#EE222222"))
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER_VERTICAL
        }

        btnPlay = Button(context).apply { text = "▶ PLAY"; setOnClickListener { togglePlay() } }
        val btnMin = Button(context).apply { text = "-"; setOnClickListener { adjustDelay(-50) } }
        txtDelay = TextView(context).apply { text = "${delayMs}ms"; setTextColor(Color.WHITE); setPadding(10,0,10,0) }
        val btnPlus = Button(context).apply { text = "+"; setOnClickListener { adjustDelay(50) } }
        val btnClose = Button(context).apply { text = "X"; setBackgroundColor(Color.RED); setTextColor(Color.WHITE); setOnClickListener { onClose() } }
        
        controlView.addView(btnPlay); controlView.addView(btnMin); controlView.addView(txtDelay); controlView.addView(btnPlus); controlView.addView(btnClose)

        controlParams = createLayoutParams(300, 100)
        setupDragging(controlView, controlParams!!)
        wm.addView(controlView, controlParams)
    }

    private fun setupTargetView() {
        targetView = View(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(8, Color.RED)
                setColor(Color.parseColor("#44FF0000")) // Transparan merah
            }
        }

        targetParams = createLayoutParams(targetSize, targetSize).apply {
            x = 500; y = 800 // Posisi awal lingkaran
        }
        setupDragging(targetView, targetParams!!)
        wm.addView(targetView, targetParams)
    }

    private fun adjustDelay(amount: Long) {
        delayMs = (delayMs + amount).coerceAtLeast(10L) // Minimal 10ms
        txtDelay.text = "${delayMs}ms"
    }

    private fun togglePlay() {
        isPlaying = !isPlaying
        btnPlay.text = if (isPlaying) "⏸ STOP" else "▶ PLAY"
        btnPlay.setTextColor(if (isPlaying) Color.RED else Color.BLACK)
        onPlayStop(isPlaying)
    }

    // Mendapatkan koordinat X dan Y tepat di tengah lingkaran target
    fun getTargetX(): Int = targetParams!!.x + (targetSize / 2)
    fun getTargetY(): Int = targetParams!!.y + (targetSize / 2)

    private fun createLayoutParams(xPos: Int, yPos: Int): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        return WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { 
            gravity = Gravity.TOP or Gravity.START
            x = xPos; y = yPos
        }
    }

    private fun setupDragging(view: View, params: WindowManager.LayoutParams) {
        var initX = 0; var initY = 0; var initTouchX = 0f; var initTouchY = 0f
        view.setOnTouchListener { _, event ->
            if (isPlaying) return@setOnTouchListener false // Kunci posisi saat jalan
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { initX = params.x; initY = params.y; initTouchX = event.rawX; initTouchY = event.rawY; true }
                MotionEvent.ACTION_MOVE -> { params.x = initX + (event.rawX - initTouchX).toInt(); params.y = initY + (event.rawY - initTouchY).toInt(); wm.updateViewLayout(view, params); true }
                else -> false
            }
        }
    }

    fun hide() {
        if (::controlView.isInitialized) wm.removeView(controlView)
        if (::targetView.isInitialized) wm.removeView(targetView)
    }
}

// --- TAP SERVICE ---
class TapService : Service() {
    private var overlay: OverlayManager? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel("macro_ch", "Macro", NotificationManager.IMPORTANCE_LOW))
        }
        startForeground(1, NotificationCompat.Builder(this, "macro_ch").setContentTitle("Macro").setContentText("Running").setSmallIcon(android.R.drawable.ic_menu_manage).build())
        
        overlay = OverlayManager(this, 
            onPlayStop = { isPlaying -> if (isPlaying) startTap() else stopTap() }, 
            onClose = { stopSelf() }
        )
        overlay?.show()
    }

    private fun startTap() {
        if (!ShizukuHelper.initShell()) {
            Toast.makeText(this, "Shizuku tidak aktif!", Toast.LENGTH_SHORT).show()
            return
        }
        
        job = scope.launch {
            while (isActive) {
                // Ambil titik X Y dari lingkaran overlay
                val x = overlay?.getTargetX() ?: 500
                val y = overlay?.getTargetY() ?: 500
                
                ShizukuHelper.tapFast(x, y)
                delay(overlay?.delayMs ?: 100L)
            }
        }
    }
    
    private fun stopTap() { 
        job?.cancel() 
    }
    
    override fun onBind(i: Intent?): IBinder? = null
    
    override fun onDestroy() { 
        stopTap()
        ShizukuHelper.closeShell()
        scope.cancel()
        overlay?.hide()
        super.onDestroy() 
    }
}
