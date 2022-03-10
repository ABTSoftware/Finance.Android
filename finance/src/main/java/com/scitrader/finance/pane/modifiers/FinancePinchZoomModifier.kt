package com.scitrader.finance.pane.modifiers

import com.scichart.charting.modifiers.PinchZoomModifier
import com.scichart.charting.visuals.axes.IAxis
import com.scitrader.finance.pane.axes.FinanceYAxisBase

class FinancePinchZoomModifier : PinchZoomModifier() {
    override fun getApplicableYAxes(): Iterable<IAxis> {
        return FinanceYAxisBase.selectAxesWithNonSharedRange(super.getApplicableYAxes())
    }
}
