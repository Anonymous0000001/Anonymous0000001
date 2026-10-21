package com.exemplo.hardwarehud

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.view.View

class CoreBarsView(context: Context) : View(context) {

    private var coreRatios = FloatArray(1)

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#424752")
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#685942")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun updateCoreRatios(ratios: FloatArray) {
        coreRatios = ratios.copyOf()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0f || height <= 0f || coreRatios.isEmpty()) return

        val barHeight = minOf(24f, height / (coreRatios.size * 1.5f))
        val gap = (height - barHeight * coreRatios.size) / (coreRatios.size - 1).coerceAtLeast(1)
        val radius = barHeight / 2f

        coreRatios.forEachIndexed { index, coreRatio ->
            val top = index * (barHeight + gap)
            val bottom = top + barHeight
            canvas.drawRoundRect(RectF(0f, top, width, bottom), radius, radius, trackPaint)

            val fillWidth = (width * coreRatio.coerceIn(0f, 1f)).coerceAtLeast(barHeight)
            val fill = RectF(0f, top, fillWidth.coerceAtMost(width), bottom)
            fillPaint.shader = LinearGradient(
                0f,
                top,
                fill.right,
                top,
                intArrayOf(
                    Color.parseColor("#B71C1C"),
                    Color.parseColor("#E53935"),
                    Color.parseColor("#FBC02D"),
                    Color.parseColor("#FFF59D")
                ),
                floatArrayOf(0f, 0.45f, 0.82f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(fill, radius, radius, fillPaint)
            canvas.drawRoundRect(fill, radius, radius, borderPaint)
        }
        fillPaint.shader = null
    }
}

class InlineCpuSparklineView(context: Context) : View(context) {

    private val points = ArrayDeque<Float>()
    private val maxPoints = 36
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FF1744")
        strokeWidth = 9f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF2A2A")
        strokeWidth = 3.5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    fun addPoint(valuePercent: Float) {
        if (points.size >= maxPoints) points.removeFirst()
        points.addLast(valuePercent.coerceIn(0f, 100f))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0f || height <= 0f || points.size < 2) return

        val path = Path()
        val stepX = width / (maxPoints - 1)
        points.forEachIndexed { index, point ->
            val x = index * stepX
            val y = height - (point / 100f * (height - 16f)) - 8f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, glowPaint)
        canvas.drawPath(path, linePaint)
    }
}
