package com.mj.preventbullying.client.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.mj.preventbullying.client.R


class AttendanceRateView(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val anglePaint = Paint()
    private val rectF = RectF()
    private var percentage: Int = 0
    private var startAngle = 0f
    private var sweepAngle = 10f

    init {
        paint.color = context.getColor(R.color.gray_1)
        paint.isAntiAlias = true
        anglePaint.isAntiAlias = true

        anglePaint.style = Paint.Style.FILL
    }

    fun setStartAngle() {
        startAngle += 10
        if (startAngle >= 360f) startAngle = 0f
        invalidate()
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width.coerceAtMost(height) / 2f
        canvas.drawCircle(centerX, centerY, radius, paint)
        paint.color = context.getColor(R.color.gray_3)
        canvas.drawCircle(centerX, centerY, 3 * radius / 4, paint)
        paint.color = context.getColor(R.color.gray_2)
        canvas.drawCircle(centerX, centerY, 2 * radius / 4, paint)
        paint.color = context.getColor(R.color.gray_4)
        canvas.drawCircle(centerX, centerY, radius / 4, paint)
        val colors = intArrayOf(Color.TRANSPARENT, context.getColor(com.sjb.base.R.color.blue1))
        val positions = floatArrayOf(0.0f, 1.0f)
        val shader: Shader =
            LinearGradient(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                colors,
                positions,
                Shader.TileMode.CLAMP
            )
        // 设置画笔
        anglePaint.shader = shader
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rectF, startAngle, sweepAngle, true, anglePaint)
    }


}