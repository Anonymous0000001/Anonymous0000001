package com.exemplo.hardwarehud

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.io.File

class FloatingMonitorService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var tvStats: TextView
    private lateinit var tvModel: TextView

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

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_hud, null)

        tvStats = floatingView.findViewById(R.id.tv_hud_stats)
        tvModel = floatingView.findViewById(R.id.tv_device_model)
        val btnClose = floatingView.findViewById<TextView>(R.id.btn_close_hud)

        tvModel.text = "${Build.MODEL} (${Build.HARDWARE})"

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

        btnClose.setOnClickListener { stopSelf() }

        floatingView.setOnTouchListener(object : View.OnTouchListener {
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
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
        Choreographer.getInstance().postFrameCallback(frameCallback)
        handler.post(updateRunnable)
    }

    private fun atualizarDadosHardware() {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalRam = memInfo.totalMem / (1024 * 1024)
        val usedRam = totalRam - (memInfo.availMem / (1024 * 1024))
        val ramPercent = ((usedRam.toDouble() / totalRam) * 100).toInt()

        val batIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val temp = (batIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
        val voltage = batIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        val cores = Runtime.getRuntime().availableProcessors()
        val cpuBuilder = StringBuilder()
        for (i in 0 until cores) {
            val freqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
            val freqMhz = if (freqFile.exists()) {
                try { "${freqFile.readText().trim().toLong() / 1000}MHz" } catch (e: Exception) { "N/A" }
            } else "Off"
            cpuBuilder.append("C$i:$freqMhz")
            if (i % 2 == 1 && i != cores - 1) cpuBuilder.append("\n") else if (i != cores - 1) cpuBuilder.append(" | ")
        }

        tvStats.text = """
            FPS: $currentFps | TEMP: $temp°C
            RAM: $usedRam/$totalRam MB ($ramPercent%)
            BAT: $level% (${voltage}mV)
            --- CPU ($cores Cores) ---
            $cpuBuilder
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
            .setContentTitle("Hardware HUD Ativo")
            .setContentText("Toque no X da janela flutuante para fechar")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        handler.removeCallbacks(updateRunnable)
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
