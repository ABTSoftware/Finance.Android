package com.scitrader.finance.pane.modifiers.crosshair

import android.graphics.*
import androidx.core.content.ContextCompat
import com.scichart.charting.modifiers.TouchModifierBase
import com.scichart.charting.modifiers.behaviors.AxisTooltipsBehavior
import com.scichart.charting.modifiers.behaviors.DrawableBehavior
import com.scichart.charting.modifiers.behaviors.ModifierBehavior
import com.scichart.charting.visuals.rendering.RenderedMessage
import com.scichart.core.IServiceContainer
import com.scichart.core.utility.Dispatcher
import com.scichart.core.utility.touch.ModifierTouchEventArgs
import com.scichart.drawing.utility.PointUtil
import com.scitrader.finance.R
import com.scitrader.finance.pane.modifiers.legend.StudyLegend
import com.scitrader.finance.state.IPanePropertyContainer
import com.scitrader.finance.state.PropertyState
import com.scitrader.finance.utils.toDip
import kotlin.math.max
import kotlin.math.min

class CrosshairModifier(private val studyLegend: StudyLegend) : TouchModifierBase(), IPanePropertyContainer {
    private val axisTooltipsBehavior = AxisTooltipsBehavior(CrosshairModifier::class.java)
    private val crosshairDrawableBehavior = CrosshairDrawableBehavior(CrosshairModifier::class.java)

    private var hasBeginBehaviorUpdate = false

    private val crosshairPaint = Paint()

    private var shouldResetUpdatePointOnTouch = true

    private var initialPointToDrawRatioX = defaultRatio
    private var initialPointToDrawRatioY = defaultRatio

    private val lastUpdatePoint = PointF(Float.NaN, Float.NaN)
    private val lastTouchPoint = PointF(Float.NaN, Float.NaN)
    private val pointToDraw = PointF(Float.NaN, Float.NaN)

    private val modifierSurfaceBounds = RectF()

    private var isMaster: Boolean = false

    override fun onIsEnabledChanged(isEnabled: Boolean) {
        super.onIsEnabledChanged(isEnabled)

        axisTooltipsBehavior.isEnabled = isEnabled
        crosshairDrawableBehavior.isEnabled = isEnabled

        if (isAttached)
            onIsEnabledChangedInternal(isEnabled)
    }

    private fun onIsEnabledChangedInternal(isEnabled: Boolean) {
        if (isEnabled) {
            shouldResetUpdatePointOnTouch = lastUpdatePoint.x.isNaN() || lastUpdatePoint.y.isNaN()

            if(!modifierSurface.layoutRect.isEmpty)
                tryUpdateBehaviors()
        } else {
            axisTooltipsBehavior.onEndUpdate(pointToDraw, false)
            crosshairDrawableBehavior.onEndUpdate(pointToDraw, false)
            studyLegend.tryUpdateTooltips()

            hasBeginBehaviorUpdate = false
        }
    }

    override fun attachTo(services: IServiceContainer?) {
        super.attachTo(services)

        val crosshairColor = ContextCompat.getColor(context, R.color.crosshairLineColor)
        val crosshairThickness = 1f.toDip()

        with(crosshairPaint) {
            strokeWidth = crosshairThickness
            color = crosshairColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.BUTT

            val dashLength = 4f.toDip()
            setPathEffect(DashPathEffect(floatArrayOf(dashLength, dashLength), 0f))
        }

        ModifierBehavior.attachTo(axisTooltipsBehavior, this, isEnabled)
        ModifierBehavior.attachTo(crosshairDrawableBehavior, this, isEnabled)

        parentSurface.run {
            axisTooltipsBehavior.observableXAxes = xAxes
            axisTooltipsBehavior.observableYAxes = yAxes
        }

        onIsEnabledChangedInternal(isEnabled)
    }

    override fun detach() {
        axisTooltipsBehavior.observableXAxes = null
        axisTooltipsBehavior.observableYAxes = null

        axisTooltipsBehavior.detach()
        crosshairDrawableBehavior.detach()

        super.detach()
    }

    override fun onXAxesDrasticallyChanged() {
        super.onXAxesDrasticallyChanged()

        axisTooltipsBehavior.observableXAxes = parentSurface.xAxes
    }

    override fun onYAxesDrasticallyChanged() {
        super.onYAxesDrasticallyChanged()

        axisTooltipsBehavior.observableYAxes = parentSurface.yAxes
    }

    override fun onRenderSurfaceRendered(message: RenderedMessage?) {
        super.onRenderSurfaceRendered(message)

        // need to update tooltips from UI thread
        Dispatcher.postOnUiThread {
            tryUpdateBehaviors()
        }
    }

    override fun onTouchDown(args: ModifierTouchEventArgs): Boolean {
        val x = args.e.x
        val y = args.e.y

        lastTouchPoint.set(x, y)

        isMaster = parentSurface.isPointWithinBounds(lastUpdatePoint.x, lastUpdatePoint.y, args.source)

        if (shouldResetUpdatePointOnTouch) {
            shouldResetUpdatePointOnTouch = false

            modifierSurface.getBoundsRelativeTo(modifierSurfaceBounds, args.source)

            lastUpdatePoint.set(modifierSurfaceBounds.left + pointToDraw.x, y)
        }

        if (isMaster) {
            initialPointToDrawRatioY = pointToDraw.y / modifierSurface.layoutHeight
        }

        return true
    }

    override fun onTouchMove(args: ModifierTouchEventArgs): Boolean {
        val x = args.e.x
        val y = args.e.y

        lastUpdatePoint.offset(x - lastTouchPoint.x, y - lastTouchPoint.y)
        lastTouchPoint.set(x, y)

        PointUtil.clipToBounds(lastUpdatePoint, args.source.view.width, args.source.view.height)

        modifierSurface.getBoundsRelativeTo(modifierSurfaceBounds, args.source)

        isMaster = parentSurface.isPointWithinBounds(lastUpdatePoint.x, lastUpdatePoint.y, args.source)

        if (args.source.isPointWithinBounds(lastUpdatePoint.x, lastUpdatePoint.y)) {
            if (isMaster) {
                pointToDraw.set(lastUpdatePoint.x - modifierSurfaceBounds.left, lastUpdatePoint.y - modifierSurfaceBounds.top)
                pointToDraw.x = min(max(pointToDraw.x, 0f), modifierSurfaceBounds.width())
                pointToDraw.y = min(max(pointToDraw.y, 0f), modifierSurfaceBounds.height())
            } else {
                pointToDraw.set(lastUpdatePoint.x - modifierSurfaceBounds.left, Float.NaN)
                pointToDraw.x = min(max(pointToDraw.x, 0f), modifierSurfaceBounds.width())
            }

            tryUpdateBehaviors()

            if (isMaster) {
                initialPointToDrawRatioY = pointToDraw.y / modifierSurface.layoutHeight
            }
        }

        return true
    }

    override fun onTouchUp(args: ModifierTouchEventArgs): Boolean {
        lastTouchPoint.set(Float.NaN, Float.NaN)

        return true
    }

    fun normalizePointToDraw() {
        val layoutHeight = modifierSurface.layoutHeight
        if (isMaster && layoutHeight > 0) {
            pointToDraw.y = layoutHeight * initialPointToDrawRatioY
        } else {
            pointToDraw.y = Float.NaN
        }

        crosshairDrawableBehavior.onBeginUpdate(pointToDraw, isMaster)
    }

    private fun tryUpdateBehaviors() {
        if (isAttached && isEnabled) {
            if (pointToDraw.x.isNaN()) {
                val layoutWidth = modifierSurface.layoutWidth
                if (layoutWidth > 0)
                    pointToDraw.x = layoutWidth * initialPointToDrawRatioX
                else
                    return
            }

            if (!hasBeginBehaviorUpdate) {
                axisTooltipsBehavior.onBeginUpdate(pointToDraw, isMaster)
                crosshairDrawableBehavior.onBeginUpdate(pointToDraw, isMaster)
                studyLegend.tryUpdateTooltips(pointToDraw.x, pointToDraw.y)

                hasBeginBehaviorUpdate = true
            } else {
                axisTooltipsBehavior.clear()
                axisTooltipsBehavior.onUpdate(pointToDraw, isMaster)
                crosshairDrawableBehavior.onUpdate(pointToDraw, isMaster)
                studyLegend.tryUpdateTooltips(pointToDraw.x, pointToDraw.y)
            }
        }
    }

    override fun savePropertyStateTo(chartState: PropertyState, paneState: PropertyState) {
        val currentXRatio = pointToDraw.x / modifierSurface.layoutWidth
        val currentYRatio = pointToDraw.y / modifierSurface.layoutHeight

        if (currentXRatio != defaultRatio)
            chartState.writeFloat(initialPointToDrawRatioXKey, currentXRatio)

        if (isMaster && currentYRatio != defaultRatio)
            chartState.writeFloat(initialPointToDrawRatioYKey, currentYRatio)
    }

    override fun restorePropertyStateFrom(chartState: PropertyState, paneState: PropertyState) {
        chartState.readFloat(initialPointToDrawRatioXKey)?.let { value ->
            if(value.isNaN() || value.isInfinite())
                return@let

            initialPointToDrawRatioX = value
        }

        chartState.readFloat(initialPointToDrawRatioYKey)?.let { value ->
            if (value.isNaN() || value.isInfinite())
                return@let

            initialPointToDrawRatioY = value
        }

        normalizePointToDraw()
    }

    companion object {
        private const val defaultRatio = 0.5f
        private const val initialPointToDrawRatioXKey = "initialPointToDrawRatioX"
        private const val initialPointToDrawRatioYKey = "initialPointToDrawRatioY"
    }

    class CrosshairDrawableBehavior<T : CrosshairModifier?>(modifierType: Class<T>?) : DrawableBehavior<T>(
        modifierType
    ) {
        private val path = Path()

        override fun onDrawOverlay(canvas: Canvas) {
            val x = lastUpdatePoint.x
            val y = lastUpdatePoint.y

            val crosshairPaint = modifier!!.crosshairPaint

            path.moveTo(x, 0f)
            path.lineTo(x, canvas.height.toFloat())

            if(!y.isNaN()) {
                path.moveTo(0f, y)
                path.lineTo(canvas.width.toFloat(), y)
            }

            canvas.drawPath(path, crosshairPaint)

            path.rewind()
        }
    }
}
