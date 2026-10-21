package com.exemplo.hardwarehud

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var diagnostics: TechDiagnostics
    private lateinit var coreBars: CoreBarsView
    private lateinit var cpuSparkline: InlineCpuSparklineView
    private lateinit var tvSocModel: TextView
    private lateinit var tvArchitecture: TextView
    private lateinit var tvRevision: TextView
    private lateinit var tvProcessNode: TextView
    private lateinit var tvClockRange: TextView
    private lateinit var tvCpuClock: TextView
    private lateinit var tvThermalStatus: TextView
    private lateinit var tvBattery: TextView
    private lateinit var tvSystem: TextView
    private lateinit var tvSensors: TextView
    private lateinit var tvGpu: TextView
    @Volatile
    private var gpuMediaSpecs = "Consultando informações de mídia..."
    private lateinit var tabPages: List<View>
    private lateinit var tabLabels: List<TextView>
    private lateinit var tabIndicators: List<View>

    private val handler = Handler(Looper.getMainLooper())
    private val refreshLoop = object : Runnable {
        override fun run() {
            atualizarInterface()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diagnostics = TechDiagnostics(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#080B11"))
        }
        root.addView(criarHeader())
        root.addView(criarDivisoriaDourada())
        root.addView(criarAbas())

        val pageFrame = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0D131D"))
                setStroke(3, Color.parseColor("#B71C1C"))
                cornerRadius = 14f
            }
        }
        val pagesHolder = FrameLayout(this).apply {
            setPadding(22, 20, 22, 26)
        }
        val pages = listOf(
            criarPaginaSoc(),
            criarPaginaSistema(),
            criarPaginaBateria(),
            criarPaginaSensores(),
            criarPaginaSobre()
        )
        tabPages = pages
        pages.forEach { page ->
            pagesHolder.addView(
                page,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
        pageFrame.addView(ScrollView(this).apply {
            isFillViewport = true
            addView(pagesHolder)
        }, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        root.addView(pageFrame, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ).apply {
            setMargins(16, 10, 16, 10)
        })
        root.addView(criarRodape())
        setContentView(root)
        selecionarAba(0)

        Thread {
            gpuMediaSpecs = diagnostics.getGpuAndMediaSpecs()
            runOnUiThread {
                if (this::tvGpu.isInitialized) tvGpu.text = gpuMediaSpecs
            }
        }.start()
        atualizarInterface()
    }

    private fun criarHeader(): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(20, 16, 20, 16)
        setBackgroundColor(Color.parseColor("#101722"))

        addView(ImageView(context).apply {
            setImageResource(R.mipmap.ic_launcher)
            contentDescription = "Ícone KZO PRO"
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(56, 56))

        addView(TextView(context).apply {
            text = "KZO PRO"
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#D8DEE9"))
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        addView(TextView(context).apply {
            text = "⚙"
            textSize = 26f
            setTextColor(Color.parseColor("#CFD8DC"))
            gravity = Gravity.CENTER
            contentDescription = "Abrir controles do HUD"
            setPadding(12, 8, 12, 8)
            setOnClickListener { iniciarOverlay() }
        })
    }

    private fun criarDivisoriaDourada(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            5
        )
        background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                Color.parseColor("#B71C1C"),
                Color.parseColor("#FFD54F"),
                Color.parseColor("#FFF59D"),
                Color.parseColor("#FFD54F"),
                Color.parseColor("#B71C1C")
            )
        )
    }

    private fun criarAbas(): View {
        val names = listOf("SOC", "SISTEMA", "BATERIA", "SENSORES", "SOBRE")
        val labels = mutableListOf<TextView>()
        val indicators = mutableListOf<View>()
        val tabBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#0B1018"))
        }
        names.forEachIndexed { index, name ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setOnClickListener { selecionarAba(index) }
            }
            val label = TextView(this).apply {
                text = name
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setPadding(2, 18, 2, 14)
            }
            val indicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    4
                )
                setBackgroundColor(Color.parseColor("#E53935"))
            }
            labels.add(label)
            indicators.add(indicator)
            item.addView(label)
            item.addView(indicator)
            tabBar.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
        }
        tabLabels = labels
        tabIndicators = indicators
        return tabBar
    }

    private fun selecionarAba(index: Int) {
        tabPages.forEachIndexed { pageIndex, page ->
            val selected = index == pageIndex
            page.visibility = if (selected) View.VISIBLE else View.GONE
            tabLabels[pageIndex].setTextColor(
                if (selected) Color.parseColor("#FBC02D") else Color.parseColor("#ECEFF1")
            )
            tabIndicators[pageIndex].visibility =
                if (selected) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun criarPaginaSoc(): View {
        val content = criarConteudoPagina()
        val soc = diagnostics.getSocDetails()

        tvSocModel = adicionarLinha(content, "Modelo do processador", soc.processorName, true)
        tvArchitecture = adicionarLinha(content, "Arquitetura", soc.architecture)
        tvRevision = adicionarLinha(content, "Revisão", soc.revision)
        tvProcessNode = adicionarLinha(content, "Processo de fabricação", soc.processNode)
        tvClockRange = adicionarLinha(content, "Faixa do clock", "Lendo frequências...")

        adicionarTitulo(content, "CLOCK POR NÚCLEO")
        coreBars = CoreBarsView(this)
        content.addView(coreBars, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            240
        ).apply { setMargins(4, 8, 4, 16) })

        val cpuRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        tvCpuClock = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
        }
        cpuSparkline = InlineCpuSparklineView(this)
        cpuRow.addView(tvCpuClock, LinearLayout.LayoutParams(0, -2, 0.3f))
        cpuRow.addView(cpuSparkline, LinearLayout.LayoutParams(0, 64, 0.7f))
        content.addView(cpuRow)
        tvThermalStatus = adicionarLinha(content, "Estado térmico", "A verificar")
        tvGpu = adicionarLinha(content, "GPU e mídia", "Consultando...")

        val button = criarBotao("ATIVAR HUD FLUTUANTE") { iniciarOverlay() }
        content.addView(button, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = 20
        })
        val benchmark = criarBotao("EXECUTAR TESTE CPU (3 SEGUNDOS)") {
            executarBenchmark(it as Button)
        }
        content.addView(benchmark, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = 10
        })
        return content
    }

    private fun criarPaginaSistema(): View {
        val content = criarConteudoPagina()
        tvSystem = TextView(this).apply {
            setTextColor(Color.parseColor("#ECEFF1"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setLineSpacing(8f, 1f)
        }
        content.addView(tvSystem)
        return content
    }

    private fun criarPaginaBateria(): View {
        val content = criarConteudoPagina()
        tvBattery = TextView(this).apply {
            setTextColor(Color.parseColor("#ECEFF1"))
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setLineSpacing(8f, 1f)
        }
        content.addView(tvBattery)
        return content
    }

    private fun criarPaginaSensores(): View {
        val content = criarConteudoPagina()
        content.addView(criarBotao("TESTAR CONTROLE / GAMEPAD") {
            startActivity(Intent(this, GamepadTestActivity::class.java))
        })
        content.addView(criarBotao("TESTAR MULTITOUCH") {
            startActivity(
                Intent(this, ScreenTestActivity::class.java)
                    .putExtra("MODE", "TOUCH")
            )
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 8 })
        content.addView(criarBotao("TESTAR CORES DA TELA") {
            startActivity(
                Intent(this, ScreenTestActivity::class.java)
                    .putExtra("MODE", "PIXEL")
            )
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 8 })
        adicionarTitulo(content, "SENSORES")
        tvSensors = TextView(this).apply {
            setTextColor(Color.parseColor("#ECEFF1"))
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setLineSpacing(5f, 1f)
        }
        content.addView(tvSensors)
        adicionarTitulo(content, "CONTROLES E PERIFÉRICOS")
        content.addView(TextView(this).apply {
            text = diagnostics.getConnectedGamepadsAndInputs()
            setTextColor(Color.parseColor("#ECEFF1"))
            textSize = 12f
            typeface = Typeface.MONOSPACE
        })
        return content
    }

    private fun criarPaginaSobre(): View {
        val content = criarConteudoPagina()
        adicionarTitulo(content, "KZO PRO • DRAGON EDITION")
        content.addView(TextView(this).apply {
            text = "Monitoramento de hardware e telemetria em tempo real."
            setTextColor(Color.parseColor("#ECEFF1"))
            textSize = 14f
        })
        content.addView(TextView(this).apply {
            text = "github.com/Anonymous0000001"
            setTextColor(Color.parseColor("#58A6FF"))
            textSize = 14f
            typeface = Typeface.MONOSPACE
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            contentDescription = "Abrir perfil do GitHub de Anonymous0000001"
            setPadding(4, 20, 4, 20)
            setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Anonymous0000001")
                    )
                )
            }
        })
        return content
    }

    private fun criarConteudoPagina(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(22, 20, 22, 26)
    }

    private fun adicionarTitulo(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title
            setTextColor(Color.parseColor("#FBC02D"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 20, 0, 10)
        })
    }

    private fun adicionarLinha(
        parent: LinearLayout,
        label: String,
        value: String,
        highlight: Boolean = false
    ): TextView {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4, 15, 4, 15)
        }
        row.addView(TextView(this).apply {
            text = label
            setTextColor(Color.parseColor("#ECEFF1"))
            textSize = 13f
        }, LinearLayout.LayoutParams(0, -2, 0.44f))
        val valueView = TextView(this).apply {
            text = value
            setTextColor(if (highlight) Color.parseColor("#F2C96D") else Color.WHITE)
            textSize = 14f
            if (highlight) typeface = Typeface.DEFAULT_BOLD
        }
        row.addView(valueView, LinearLayout.LayoutParams(0, -2, 0.56f))
        parent.addView(row)
        parent.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#222C3C"))
        }, LinearLayout.LayoutParams(-1, 1))
        return valueView
    }

    private fun criarBotao(label: String, click: (View) -> Unit): Button =
        Button(this).apply {
            text = label
            setBackgroundColor(Color.parseColor("#B71C1C"))
            setTextColor(Color.parseColor("#FFD54F"))
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener(click)
        }

    private fun criarRodape(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(Color.parseColor("#10141D"))
        addView(criarDivisoriaDourada())
        addView(TextView(this@MainActivity).apply {
            text = "KZO PRO"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#CFD8DC"))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 14)
        })
    }

    private fun atualizarInterface() {
        val soc = diagnostics.getSocDetails()
        val battery = diagnostics.getBatteryTelemetry()
        val cpu = diagnostics.getCpuTelemetry(battery.temperatureC)
        val (usedRam, totalRam, ramPercent) = diagnostics.getRamData()
        val (netSpeed, ip) = diagnostics.getNetworkSpeedAndIp()

        tvSocModel.text = soc.processorName
        tvArchitecture.text = soc.architecture
        tvRevision.text = soc.revision
        tvProcessNode.text = soc.processNode
        tvClockRange.text = "${cpu.minClockText} - ${cpu.maxClockText}"
        coreBars.updateCoreRatios(cpu.coreRatios)
        tvCpuClock.text = "Clock médio ${cpu.avgClockPercent.toInt()}%"
        cpuSparkline.addPoint(cpu.avgClockPercent)
        tvThermalStatus.text = if (cpu.possibleThrottling) {
            "Possível limite térmico de clock"
        } else {
            "Sem alerta térmico"
        }
        tvGpu.text = gpuMediaSpecs
        tvBattery.text = """
            DIAGNÓSTICO DA BATERIA
            • Estado: ${battery.status}
            • Corrente atual: ${battery.currentMa} mA
            • Pico da sessão: ${battery.peakCurrentMa} mA
            • Potência estimada: ${String.format("%.2f", battery.powerWatts)} W
            • Tensão: ${battery.voltageMv} mV
            • Temperatura: ${battery.temperatureC?.let { "$it °C" } ?: "Indisponível"}
            • Saúde: ${battery.health}
            • Throttling estimado: ${if (cpu.possibleThrottling) "Possível" else "Não detectado"}
        """.trimIndent()
        tvSystem.text = """
            • RAM: $usedRam / $totalRam MB ($ramPercent%)
            • Rede: $netSpeed
            • IP local: $ip
            • Núcleos lógicos: ${Runtime.getRuntime().availableProcessors()}

            ${diagnostics.getDeepSystemSpecs()}
        """.trimIndent()
        tvSensors.text = diagnostics.getSensorsListText()
    }

    private fun executarBenchmark(button: Button) {
        button.isEnabled = false
        button.text = "Executando..."
        Thread {
            val endAt = System.currentTimeMillis() + 3000
            var primes = 0
            var number = 2
            while (System.currentTimeMillis() < endAt) {
                var isPrime = true
                var divisor = 2
                while (divisor * divisor <= number) {
                    if (number % divisor == 0) {
                        isPrime = false
                        break
                    }
                    divisor++
                }
                if (isPrime) primes++
                number++
            }
            runOnUiThread {
                button.isEnabled = true
                button.text = "RESULTADO: $primes primos/3s (repetir)"
            }
        }.start()
    }

    private fun iniciarOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(
                this,
                "Ative a permissão de sobreposição e volte ao app",
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }

        val intent = Intent(this, FloatingMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "KZO PRO HUD ativado", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(refreshLoop)
        handler.post(refreshLoop)
    }

    override fun onPause() {
        handler.removeCallbacks(refreshLoop)
        super.onPause()
    }
}
