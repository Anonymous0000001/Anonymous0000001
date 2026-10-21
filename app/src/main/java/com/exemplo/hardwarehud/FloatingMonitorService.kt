package com.exemplo.hardwarehud

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class FloatingMonitorService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var rootLayout: LinearLayout
    private lateinit var tvStats: TextView
    private lateinit var tvModel: TextView
    private lateinit var graphRam: RealtimeGraphView
    private lateinit var graphCpu: RealtimeGraphView

    private val diagnostics by lazy { TechDiagnostics(this) }
    private val handler = Handler(Looper.getMainLooper())
    private var frameCount = 0
    private var currentFps = 0
    private var lastFpsTime = System.nanoTime()

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameCount++
            val elapsedNanos = frameTimeNanos - lastFpsTime
            if (elapsedNanos >= 1_000_000_000L) {
                currentFps = ((frameCount * 1_000_000_000L) / elapsedNanos).toInt()
                frameCount = 0
                lastFpsTime = frameTimeNanos
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            atualizarDadosHardware()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        iniciarNotificacaoForeground()

        windowManager = getSystemService(WindowManager::class.java)

        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.argb(180, 10, 12, 16))
            setPadding(16, 12, 16, 12)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        tvModel = TextView(this).apply {
            text = "${Build.MANUFACTURER} ${Build.MODEL}"
            setTextColor(Color.parseColor("#00FF66"))
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val btnClose = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#FF4444"))
            textSize = 14f
            setPadding(18, 0, 0, 0)
            setOnClickListener { stopSelf() }
        }

        header.addView(tvModel)
        header.addView(btnClose)

        tvStats = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(0, 8, 0, 8)
        }

        graphRam = RealtimeGraphView(this, "RAM", "%", 100f, Color.parseColor("#00E5FF"))
        graphCpu = RealtimeGraphView(this, "CPU", "%", 100f, Color.parseColor("#FF9100"))

        val graphParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            140
        ).apply { topMargin = 8 }

        rootLayout.addView(header)
        rootLayout.addView(tvStats)
        rootLayout.addView(graphRam, graphParams)
        rootLayout.addView(graphCpu, graphParams)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 80
        }

        rootLayout.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(rootLayout, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(rootLayout, params)
        Choreographer.getInstance().postFrameCallback(frameCallback)
        handler.post(updateRunnable)
    }

    private fun atualizarDadosHardware() {
        val (usedRam, totalRam, ramPercent) = diagnostics.getRamData()
        val (cpuLoad, cpuCoresText) = diagnostics.getCpuData()
        val batInfo = diagnostics.getBatteryTechInfo()
        val (netSpeed, ip) = diagnostics.getNetworkSpeedAndIp()

        graphRam.addValue(ramPercent.toFloat(), "$usedRam / $totalRam MB ($ramPercent%)")
        graphCpu.addValue(cpuLoad, "${cpuLoad.toInt()}% Clock Médio")

        val tempText = batInfo["Tensão / Temp"] ?: "N/A"
        tvStats.text = """
            FPS: $currentFps | RAM: $usedRam/$totalRam MB ($ramPercent%)
            BAT: ${batInfo["Status"]} | TEMP: $tempText
            REDE: $netSpeed (IP:$ip)
            CPU: $cpuCoresText
        """.trimIndent()
    }

    private fun iniciarNotificacaoForeground() {
        val channelId = "hud_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitor de Hardware",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("KZO PRO Ativo")
            .setContentText("Toque no X da janela flutuante para fechar")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        handler.removeCallbacksAndMessages(null)
        if (this::rootLayout.isInitialized) {
            try {
                windowManager.removeView(rootLayout)
            } catch (_: IllegalArgumentException) {
            }
        }
    }
}
