package com.scitrader.finance.pane.series

import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.visuals.renderableSeries.FastColumnRenderableSeries
import com.scichart.drawing.common.SolidBrushStyle
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.properties.BrushStyleEditableProperty
import com.scitrader.finance.edit.properties.DataPointWidthEditableProperty
import com.scitrader.finance.edit.properties.PenStyleEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.utils.SolidPenStyle
import com.scitrader.finance.utils.XyDataSeries
import java.util.*

class ColumnFinanceSeries(
    @StringRes name: Int,
    xValues: DataSourceId,
    yValues: DataSourceId,
    yAxisId: AxisId,
    yTooltipName: CharSequence?
) :
    XyFinanceSeriesBase<FastColumnRenderableSeries, XyDataSeries<Date, Double>>(
        name,
        xValues,
        yValues,
        FastColumnRenderableSeries(),
        XyDataSeries(),
        yAxisId,
        yTooltipName
    ) {

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val strokeStyle = PenStyleEditableProperty(R.string.strokeStyle, name, SolidPenStyle(com.scitrader.finance.pane.series.Constants.DefaultGreen, com.scitrader.finance.pane.series.Constants.LightThickness)) { id, value ->
        renderableSeries.strokeStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val fillStyle = BrushStyleEditableProperty(R.string.fillStyle, name, SolidBrushStyle(com.scitrader.finance.pane.series.Constants.DefaultGreen)) { id, value ->
        renderableSeries.fillBrushStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    var dataPointWidth = DataPointWidthEditableProperty(
        R.string.candlestickDataPointWidth,
        name,
        com.scitrader.finance.pane.series.Constants.DefaultCandleStickDataPointWidth
    ) { id, value ->
        renderableSeries.dataPointWidth = value
        onPropertyChanged(id)
    }

    override fun reset() {
        super.reset()

        strokeStyle.reset()
        fillStyle.reset()
    }
}
