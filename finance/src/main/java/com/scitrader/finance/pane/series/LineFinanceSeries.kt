package com.scitrader.finance.pane.series

import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.properties.PenStyleEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.utils.SolidPenStyle
import com.scitrader.finance.utils.XyDataSeries
import java.util.*

class LineFinanceSeries(
    @StringRes name: Int,
    xValues: DataSourceId,
    yValues: DataSourceId,
    yAxisId: AxisId,
    yTooltipName: CharSequence? = null
) :
    XyFinanceSeriesBase<FastLineRenderableSeries, XyDataSeries<Date, Double>>(
        name,
        xValues,
        yValues,
        FastLineRenderableSeries(),
        XyDataSeries(),
        yAxisId,
        yTooltipName
    ) {

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val strokeStyle = PenStyleEditableProperty(R.string.strokeStyle, name, SolidPenStyle(com.scitrader.finance.pane.series.Constants.DefaultBlue, com.scitrader.finance.pane.series.Constants.DefaultThickness)) { id, value ->
        renderableSeries.strokeStyle = value
        onPropertyChanged(id)
    }

    override fun reset() {
        super.reset()

        strokeStyle.reset()
    }
}
