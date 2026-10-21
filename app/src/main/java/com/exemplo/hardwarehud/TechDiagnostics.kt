package com.exemplo.hardwarehud

import android.app.ActivityManager
import android.content.Context
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.InputDevice
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.math.abs

class TechDiagnostics(private val context: Context) {

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastNetTime = System.currentTimeMillis()

    fun getRamData(): Triple<Long, Long, Int> {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val usedMb = totalMb - (memInfo.availMem / (1024 * 1024))
        val percent = ((usedMb.toDouble() / totalMb) * 100).toInt()
        return Triple(usedMb, totalMb, percent)
    }

    fun getCpuData(): Pair<Float, String> {
        val cores = Runtime.getRuntime().availableProcessors()
        val sb = StringBuilder()
        var totalRatio = 0f
        var validCores = 0

        for (i in 0 until cores) {
            val curFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
            val maxFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
            try {
                if (curFile.exists()) {
                    val curKhz = curFile.readText().trim().toLong()
                    val maxKhz = if (maxFile.exists()) maxFile.readText().trim().toLong() else 2500000L
                    if (maxKhz > 0) {
                        totalRatio += (curKhz.toFloat() / maxKhz.toFloat()) * 100f
                        validCores++
                    }
                    sb.append("C$i: ${curKhz / 1000}MHz")
                } else {
                    sb.append("C$i: Off")
                }
            } catch (_: Exception) {
                sb.append("C$i: Restrito")
            }
            if (i % 2 == 1 && i != cores - 1) sb.append("\n") else if (i != cores - 1) sb.append(" | ")
        }

        val avgLoad = if (validCores > 0) (totalRatio / validCores) else 0f
        return Pair(avgLoad, sb.toString())
    }

    fun getBatteryTechInfo(): Map<String, String> {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        val statusInt = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val healthInt = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val rawCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = if (abs(rawCurrent.toDouble()) > 10000) rawCurrent / 1000 else rawCurrent
        val watts = (abs(currentMa.toDouble()) * (voltageMv / 1000.0)) / 1000.0

        val statusStr = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Carregando ⚡"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descarregando"
            BatteryManager.BATTERY_STATUS_FULL -> "Carga Completa"
            else -> "Em Uso"
        }

        val healthStr = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Boa (100%)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Superaquecida!"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degradada"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobretensão"
            else -> "Normal"
        }

        return mapOf(
            "Status" to "$statusStr ($level%)",
            "Corrente / Potência" to "${currentMa} mA (${String.format("%.2f", watts)} W)",
            "Tensão / Temp" to "${voltageMv} mV | ${temp} °C",
            "Saúde / Química" to "$healthStr ($tech)"
        )
    }

    fun getNetworkSpeedAndIp(): Pair<String, String> {
        val now = System.currentTimeMillis()
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        val deltaSec = ((now - lastNetTime) / 1000.0).coerceAtLeast(1.0)

        val downKbs = ((rx - lastRxBytes) / 1024.0 / deltaSec).toInt().coerceAtLeast(0)
        val upKbs = ((tx - lastTxBytes) / 1024.0 / deltaSec).toInt().coerceAtLeast(0)

        lastRxBytes = rx
        lastTxBytes = tx
        lastNetTime = now

        var ipAddress = "Desconectado"
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val netIf = interfaces.nextElement()
                val addresses = netIf.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        ipAddress = addr.hostAddress ?: "Desconhecido"
                    }
                }
            }
        } catch (_: Exception) {
        }

        return Pair("↓ $downKbs KB/s | ↑$upKbs KB/s", ipAddress)
    }

    fun getConnectedGamepadsAndInputs(): String {
        val ids = InputDevice.getDeviceIds()
        val gamepads = mutableListOf<String>()
        for (id in ids) {
            val dev = InputDevice.getDevice(id) ?: continue
            val sources = dev.sources
            val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
            if (isGamepad || dev.isExternal) {
                val tipo = if (isGamepad) "🎮 Gamepad" else "🔌 Periférico"
                gamepads.add("$tipo:${dev.name}")
            }
        }
        return if (gamepads.isEmpty()) "Nenhum controle/periférico externo conectado" else gamepads.joinToString("\n")
    }

    fun getDeepSystemSpecs(): String {
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/su/bin/su"
        )
        val hasRoot = rootPaths.any { File(it).exists() }

        val selinux = try {
            val process = Runtime.getRuntime().exec("getenforce")
            BufferedReader(InputStreamReader(process.inputStream)).readLine() ?: "Restrito"
        } catch (_: Exception) {
            "Enforcing / Restrito"
        }

        val uptimeMinutes = SystemClock.elapsedRealtime() / (1000 * 60)
        val hours = uptimeMinutes / 60
        val mins = uptimeMinutes % 60

        val stat = StatFs(Environment.getDataDirectory().path)
        val totalGb = (stat.blockCountLong * stat.blockSizeLong) / (1024L * 1024L * 1024L)
        val freeGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024L * 1024L * 1024L)

        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val totalSensors = sm.getSensorList(Sensor.TYPE_ALL).size

        return """
            • Modelo: ${android.os.Build.MANUFACTURER.uppercase()} ${android.os.Build.MODEL} (${android.os.Build.DEVICE})
            • SoC / Placa: ${android.os.Build.HARDWARE} /${android.os.Build.BOARD}
            • Arquitetura (ABI): ${android.os.Build.SUPPORTED_ABIS.joinToString(", ")}
            • Android: ${android.os.Build.VERSION.RELEASE} (SDK${android.os.Build.VERSION.SDK_INT})
            • Patch de Segurança: ${android.os.Build.VERSION.SECURITY_PATCH}
            • Kernel Linux: ${System.getProperty("os.version") ?: "N/A"}
            • Bootloader: ${android.os.Build.BOOTLOADER}
            • Acesso Root (su): ${if (hasRoot) "DETECTADO (Root Ativo)" else "Não detectado (Original)"}
            • Status SELinux: $selinux
            • Armazenamento Interno: $freeGb GB livres de $totalGb GB
            • Sensores Físicos no Aparelho: $totalSensors sensores ativos
            • Tempo Ligado (Uptime): ${hours}h${mins}min
        """.trimIndent()
    }
}
