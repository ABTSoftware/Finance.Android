package com.scitrader.finance.pane.modifiers.yAxisDrag

import androidx.appcompat.widget.AppCompatButton
import com.scichart.charting.modifiers.YAxisDragModifier
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.core.IServiceContainer
import com.scichart.core.utility.Dispatcher
import com.scichart.data.model.DoubleRange
import com.scichart.data.model.IRange
import com.scitrader.finance.pane.axes.AxisId

class FinanceYAxisDragModifier(private val autoRangeButton: AppCompatButton): YAxisDragModifier() {
    val excludeAxisIds = mutableListOf<AxisId>()

    var isYAutoRangeEnabled = true
        set(value) {
            if(field == value) return

            field = value

            onYAutoRangeEnabledChanged(value)
        }

    init {
        autoRangeButton.setOnClickListener {
            isYAutoRangeEnabled = !isYAutoRangeEnabled
        }
    }

    override fun attachTo(services: IServiceContainer?) {
        super.attachTo(services)

        // on attach update chart according to isYAutoRangeEnabled without animation
        if(isYAutoRangeEnabled) {
            parentSurface.zoomExtents()
            updateYAutoRange(AutoRange.Always)
        } else {
            updateYAutoRange(AutoRange.Never)
        }

        updateAutoRangeButtonState(isYAutoRangeEnabled)
    }

    private fun onYAutoRangeEnabledChanged(value: Boolean) {
        if(!isAttached) return

        updateAutoRangeButtonState(value)

        // disable button until updateYAutoRange is updated to prevent tapping while animation is in progress
        autoRangeButton.isEnabled = false

        if (value) {
            val duration: Long = 500
            parentSurface.animateZoomExtentsY(duration)

            Dispatcher.postDelayedOnUiThread({
                updateYAutoRange(AutoRange.Always)
            }, duration)
        } else {
            updateYAutoRange(AutoRange.Never)
        }
    }

    private fun updateYAutoRange(yAutoRange: AutoRange) {
        for (axis in applicableAxes) {
            axis.autoRange = yAutoRange
        }

        autoRangeButton.isEnabled = true
    }

    private fun updateAutoRangeButtonState(isSelected: Boolean) {
        Dispatcher.postOnUiThread {
            autoRangeButton.isSelected = isSelected
        }
    }

    override fun getApplicableAxes(): Iterable<IAxis> {
        return this.yAxes.filter { !excludeAxisIds.map { id -> id.toString() }.contains(it.axisId) }
    }

    override fun applyScaleToRange(
        applyTo: IRange<*>?,
        xDelta: Float,
        yDelta: Float,
        isSecondHalf: Boolean,
        axis: IAxis
    ) {
        isYAutoRangeEnabled = false

        val interactivityHelper = axis.currentInteractivityHelper

        val pixelsToScroll = if (axis.isHorizontalAxis) xDelta else yDelta

        interactivityHelper.scrollInMaxDirection(applyTo, pixelsToScroll)
        interactivityHelper.scrollInMinDirection(applyTo, -pixelsToScroll)

        (axis.visibleRangeLimit as? DoubleRange)?.let {
            (applyTo as DoubleRange).clipTo(it, axis.visibleRangeLimitMode)
        }
    }
}

