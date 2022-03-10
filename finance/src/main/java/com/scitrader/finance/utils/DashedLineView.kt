package com.scitrader.finance.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class DashedLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
    }

    private val path = Path()

    fun setStrokeColor(@ColorInt color: Int) {
        paint.color = color
    }

    fun setStrokeThickness(thickness: Float) {
        paint.strokeWidth = thickness
    }

    fun setDash(pattern: FloatArray, phase: Float = 0f) {
        paint.pathEffect = DashPathEffect(pattern, phase)
    }

    override fun onDraw(canvas: Canvas) {
        val halfHeight = measuredHeight / 2f
        val width = measuredWidth.toFloat()

        path.reset()
        path.moveTo(0f, halfHeight)
        path.lineTo(width, halfHeight)

        canvas.drawPath(path, paint)
    }
}
