package com.scitrader.finance.study

import androidx.annotation.StringRes
import com.scichart.charting.visuals.axes.AxisBase
import com.scitrader.finance.R
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.pane.axes.FinanceNumericYAxis

abstract class CandleStudyBase(
    pane: PaneId,
    id: StudyId,
    @StringRes yAxisName: Int = R.string.defaultYAxis,
    yAxisId: String = AxisBase.DEFAULT_AXIS_ID
) : StudyBase(
    id,
    pane
) {
    protected val yAxisId = AxisId(pane, id, yAxisId)

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val yAxis = FinanceNumericYAxis(yAxisName, this.yAxisId)

    init {
        financeYAxes.add(yAxis)
    }

    override fun reset() {
        yAxis.reset()
    }
}
