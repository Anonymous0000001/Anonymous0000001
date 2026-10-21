package com.exemplo.hardwarehud

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var diagnostics: TechDiagnostics
    private lateinit var graphRam: RealtimeGraphView
    private lateinit var graphCpu: RealtimeGraphView
    private lateinit var tvLiveMetrics: TextView
    private lateinit var tvControllers: TextView
    private lateinit var tvSpecs: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val refreshLoop = object : Runnable {
        override fun run() {
            atualizarPainelTecnico()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diagnostics = TechDiagnostics(this)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0D0F14"))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 48, 36, 60)
        }

        val header = TextView(this).apply {
            text = "⚡ HARDWARE HUD PRO - DIAGNÓSTICO TÉCNICO"
            setTextColor(Color.parseColor("#00FF88"))
            textSize = 16f
            typeface = Typeface.MONOSPACE
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, 24)
        }

        val btnHud = Button(this).apply {
            text = "ABRIR JANELA FLUTUANTE COM GRÁFICO (HUD)"
            setBackgroundColor(Color.parseColor("#00C853"))
            setTextColor(Color.BLACK)
            setOnClickListener { iniciarOverlay() }
        }

        val lpGraph = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            240
        ).apply { topMargin = 28 }

        graphRam = RealtimeGraphView(this, "USO DE RAM", "%", 100f, Color.parseColor("#00E5FF"))
        graphCpu = RealtimeGraphView(this, "CARGA DE CLOCK CPU", "%", 100f, Color.parseColor("#FF9100"))

        tvLiveMetrics = criarBlocoTexto("Lendo telemetria elétrica e clocks...")
        tvControllers = criarBlocoTexto("Verificando barramentos USB/Bluetooth...")
        tvSpecs = criarBlocoTexto(diagnostics.getDeepSystemSpecs())

        val btnTestStress = Button(this).apply {
            text = "RODAR BENCHMARK RÁPIDO DE CPU (STRESS 3s)"
            setBackgroundColor(Color.parseColor("#2962FF"))
            setTextColor(Color.WHITE)
            setOnClickListener { rodarBenchmarkCpu(this) }
        }

        val lpBtn = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 24 }

        container.addView(header)
        container.addView(btnHud)
        container.addView(graphRam, lpGraph)
        container.addView(graphCpu, lpGraph)
        container.addView(criarTituloSecao("TELEMETRIA ELÉTRICA, REDE E NÚCLEOS"))
        container.addView(tvLiveMetrics)
        container.addView(criarTituloSecao("CONTROLES (GAMEPADS) E PERIFÉRICOS"))
        container.addView(tvControllers)
        container.addView(btnTestStress, lpBtn)
        container.addView(criarTituloSecao("ESPECIFICAÇÕES DE HARDWARE, KERNEL E SEGURANÇA"))
        container.addView(tvSpecs)

        scroll.addView(container)
        setContentView(scroll)
    }

    private fun atualizarPainelTecnico() {
        val (usedRam, totalRam, ramPercent) = diagnostics.getRamData()
        val (cpuLoad, cpuCoresText) = diagnostics.getCpuData()
        val batInfo = diagnostics.getBatteryTechInfo()
        val (netSpeed, ip) = diagnostics.getNetworkSpeedAndIp()

        graphRam.addValue(ramPercent.toFloat(), "$usedRam / $totalRam MB ($ramPercent%)")
        graphCpu.addValue(cpuLoad, "${cpuLoad.toInt()}% Clock Médio")

        val refreshRate = try {
            windowManager.defaultDisplay.refreshRate.toInt()
        } catch (_: Exception) {
            60
        }

        tvLiveMetrics.text = """
            • Bateria: ${batInfo["Status"]}
            • Corrente / Potência: ${batInfo["Corrente / Potência"]}
            • Tensão / Temperatura: ${batInfo["Tensão / Temp"]}
            • Saúde da Bateria: ${batInfo["Saúde / Química"]}
            • Rede: $netSpeed (IP:$ip)
            • Taxa da Tela: $refreshRate Hz

            [Frequências dos Núcleos em Tempo Real]
            $cpuCoresText
        """.trimIndent()

        tvControllers.text = diagnostics.getConnectedGamepadsAndInputs()
    }

    private fun rodarBenchmarkCpu(btn: Button) {
        btn.isEnabled = false
        btn.text = "Calculando números primos em todos os núcleos..."
        Thread {
            val start = System.currentTimeMillis()
            var primes = 0
            var num = 2
            while (System.currentTimeMillis() - start < 3000) {
                var isPrime = true
                var i = 2
                while (i * i <= num) {
                    if (num % i == 0) {
                        isPrime = false
                        break
                    }
                    i++
                }
                if (isPrime) primes++
                num++
            }
            runOnUiThread {
                btn.isEnabled = true
                btn.text = "PONTUAÇÃO CPU: $primes Primos/3s (Toque p/ repetir)"
                Toast.makeText(this, "Score Single-Thread: $primes pontos!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun criarTituloSecao(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 36, 0, 10)
        }
    }

    private fun criarBlocoTexto(textoInicial: String): TextView {
        return TextView(this).apply {
            text = textoInicial
            setTextColor(Color.parseColor("#E0E6ED"))
            setBackgroundColor(Color.parseColor("#1A1D24"))
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setPadding(24, 24, 24, 24)
        }
    }

    private fun iniciarOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Ative a permissão 'Sobrepor a outros apps' e volte", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        } else {
            val intent = Intent(this, FloatingMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "HUD Flutuante Ativado!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshLoop)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshLoop)
    }
}
