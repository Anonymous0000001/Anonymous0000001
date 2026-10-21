package com.exemplo.hardwarehud

import android.content.Context
import android.graphics.*
import android.view.View

class RealtimeGraphView(
    context: Context,
    private val title: String,
    private val unit: String,
    private val maxVal: Float,
    private val lineColor: Int
) : View(context) {

    private val dataPoints = ArrayList<Float>()
    private val maxPoints = 40
    private var currentValueText = "0 $unit"

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#161922")
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint().apply {
        color = Color.parseColor("#2A2F3D")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lineColor
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(55, Color.red(lineColor), Color.green(lineColor), Color.blue(lineColor))
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 26f
        typeface = Typeface.MONOSPACE
        isFakeBoldText = true
    }

    init {
        repeat(maxPoints) { dataPoints.add(0f) }
    }

    fun addValue(value: Float, customLabel: String? = null) {
        if (dataPoints.size >= maxPoints) dataPoints.removeAt(0)
        dataPoints.add(value.coerceIn(0f, maxVal))
        currentValueText = customLabel ?: "${value.toInt()}$unit"
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        canvas.drawRoundRect(0f, 0f, w, h, 14f, 14f, bgPaint)

        canvas.drawLine(0f, h * 0.25f, w, h * 0.25f, gridPaint)
        canvas.drawLine(0f, h * 0.50f, w, h * 0.50f, gridPaint)
        canvas.drawLine(0f, h * 0.75f, w, h * 0.75f, gridPaint)

        if (dataPoints.size > 1) {
            val path = Path()
            val fillPath = Path()
            val stepX = w / (maxPoints - 1)

            fillPath.moveTo(0f, h)
            for (i in dataPoints.indices) {
                val x = i * stepX
                val normalized = (dataPoints[i] / maxVal).coerceIn(0f, 1f)
                val y = h - (normalized * (h - 42f)) - 6f

                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            fillPath.lineTo(w, h)
            fillPath.close()

            canvas.drawPath(fillPath, fillPaint)
            canvas.drawPath(path, linePaint)
        }

        canvas.drawText("$title:$currentValueText", 16f, 32f, textPaint)
    }
}
