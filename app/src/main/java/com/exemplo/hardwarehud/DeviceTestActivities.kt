package com.exemplo.hardwarehud

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.min

class GamepadTestActivity : Activity() {

    private lateinit var readout: TextView
    private lateinit var sticksView: GamepadSticksView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readout = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            text = "Conecte um controle e mova os analógicos ou pressione os botões."
            setPadding(20, 20, 20, 20)
        }
        sticksView = GamepadSticksView(this)

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#080B11"))
            addView(readout)
            addView(sticksView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ))
        })
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        val isController = event.isFromSource(InputDevice.SOURCE_JOYSTICK) ||
            event.isFromSource(InputDevice.SOURCE_GAMEPAD)
        if (isController && event.action == MotionEvent.ACTION_MOVE) {
            val axes = listOf(
                MotionEvent.AXIS_X,
                MotionEvent.AXIS_Y,
                MotionEvent.AXIS_Z,
                MotionEvent.AXIS_RZ
            ).map { axis -> event.getAxisValue(axis) }
            sticksView.setAxes(axes)
            readout.text = "Controle: ${event.device?.name ?: "desconhecido"}\n" +
                axes.mapIndexed { index, value -> "Eixo ${index + 1}: ${"%.2f".format(value)}" }
                    .joinToString("\n")
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isController = event.isFromSource(InputDevice.SOURCE_GAMEPAD) ||
            event.isFromSource(InputDevice.SOURCE_JOYSTICK)
        if (!isController || event.keyCode == KeyEvent.KEYCODE_BACK) {
            return super.dispatchKeyEvent(event)
        }
        if (event.action == KeyEvent.ACTION_DOWN) {
            readout.text = "Botão pressionado: ${KeyEvent.keyCodeToString(event.keyCode)}"
            return true
        }
        if (event.action == KeyEvent.ACTION_UP) return true
        return super.dispatchKeyEvent(event)
    }
}

private class GamepadSticksView(context: Context) : View(context) {

    private val axes = floatArrayOf(0f, 0f, 0f, 0f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setAxes(values: List<Float>) {
        values.take(axes.size).forEachIndexed { index, value -> axes[index] = value }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#080B11"))
        val radius = min(width / 5f, height / 4f).coerceAtLeast(20f)
        drawStick(canvas, width * 0.3f, height * 0.48f, radius, axes[0], axes[1])
        drawStick(canvas, width * 0.7f, height * 0.48f, radius, axes[2], axes[3])
    }

    private fun drawStick(canvas: Canvas, cx: Float, cy: Float, radius: Float, x: Float, y: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.parseColor("#CFD8DC")
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#E53935")
        canvas.drawCircle(
            cx + x.coerceIn(-1f, 1f) * radius,
            cy + y.coerceIn(-1f, 1f) * radius,
            radius * 0.16f,
            paint
        )
    }
}

class ScreenTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val touchMode = intent.getStringExtra("MODE") == "TOUCH"
        setContentView(ScreenTestView(this, touchMode))
    }
}

private class ScreenTestView(context: Context, private val touchMode: Boolean) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val touches = mutableMapOf<Int, Pair<Float, Float>>()
    private val testColors = intArrayOf(
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.WHITE,
        Color.BLACK
    )
    private var colorIndex = 0
    private var touchCount = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(if (touchMode) Color.parseColor("#101722") else testColors[colorIndex])
        if (!touchMode) {
            paint.color = if (colorIndex == 3 || colorIndex == 4) Color.RED else Color.WHITE
            paint.textSize = 22f
            canvas.drawText(
                "Toque para mudar a cor (${colorIndex + 1}/${testColors.size}); Voltar para sair",
                24f,
                44f,
                paint
            )
            return
        }

        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText("Multitouch: ${touches.size} pontos • Toques: $touchCount", 24f, 40f, paint)
        paint.color = Color.parseColor("#FF5252")
        touches.values.forEach { (x, y) ->
            canvas.drawCircle(x, y, 32f, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(x, y, 5f, paint)
            paint.color = Color.parseColor("#FF5252")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!touchMode) {
            if (event.action == MotionEvent.ACTION_UP) {
                colorIndex = (colorIndex + 1) % testColors.size
                invalidate()
            }
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> touchCount++
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                touches.remove(event.getPointerId(event.actionIndex))
            }
        }
        if (event.actionMasked != MotionEvent.ACTION_UP &&
            event.actionMasked != MotionEvent.ACTION_POINTER_UP
        ) {
            for (index in 0 until event.pointerCount) {
                touches[event.getPointerId(index)] = event.getX(index) to event.getY(index)
            }
        }
        invalidate()
        return true
    }
}
