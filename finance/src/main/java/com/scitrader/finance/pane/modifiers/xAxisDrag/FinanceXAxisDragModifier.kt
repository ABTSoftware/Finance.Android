package com.scitrader.finance.pane.modifiers.xAxisDrag

import com.scichart.charting.modifiers.XAxisDragModifier
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.data.model.DoubleRange
import com.scichart.data.model.IRange

class FinanceXAxisDragModifier : XAxisDragModifier() {

    override fun applyScaleToRange(
        applyTo: IRange<*>?,
        xDelta: Float,
        yDelta: Float,
        isSecondHalf: Boolean,
        axis: IAxis
    ) {
        try {
            val interactivityHelper = axis.currentInteractivityHelper

            val pixelsToScroll = if (axis.isHorizontalAxis) xDelta else yDelta

            interactivityHelper.scrollInMaxDirection(applyTo, pixelsToScroll)
            interactivityHelper.scrollInMinDirection(applyTo, -pixelsToScroll)

            (axis.visibleRangeLimit as? DoubleRange)?.let {
                (applyTo as DoubleRange).clipTo(it, axis.visibleRangeLimitMode)
            }
        } catch (_: Exception) {
        }
    }
}
