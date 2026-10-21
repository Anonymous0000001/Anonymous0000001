package com.exemplo.hardwarehud

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(48, 48, 48, 48)

            val titulo = TextView(context).apply {
                text = "Hardware HUD Monitor\n\nModelo: ${Build.MODEL}\nSoC: ${Build.HARDWARE}\nBoard: ${Build.BOARD}\nABI: ${Build.SUPPORTED_ABIS.joinToString()}"
                setTextColor(Color.WHITE)
                textSize = 15f
                setPadding(0, 0, 0, 40)
            }

            val btnIniciar = Button(context).apply {
                text = "ATIVAR OVERLAY (JANELA FLUTUANTE)"
                setOnClickListener { verificarPermissaoEIniciar() }
            }

            addView(titulo)
            addView(btnIniciar)
        }
        setContentView(layout)
    }

    private fun verificarPermissaoEIniciar() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Autorize a sobreposição e volte ao app", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            val serviceIntent = Intent(this, FloatingMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            finish()
        }
    }
}
