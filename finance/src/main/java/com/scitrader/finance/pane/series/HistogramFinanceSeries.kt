package com.scitrader.finance.pane.series

import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.visuals.renderableSeries.FastColumnRenderableSeries
import com.scichart.drawing.common.SolidBrushStyle
import com.scichart.drawing.utility.ColorUtil
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.properties.BrushStyleEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.utils.XyDataSeries
import java.util.*

class HistogramFinanceSeries(
    @StringRes name: Int,
    xValues: DataSourceId,
    yValues: DataSourceId,
    yAxisId: AxisId,
    yTooltipName: CharSequence? = null
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
    var fillUpBrushStyle = BrushStyleEditableProperty(R.string.histogramFillUpStyle, name, SolidBrushStyle(
        com.scitrader.finance.pane.series.Constants.DefaultFillUp)) { id, _ ->
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    var fillDownBrushStyle = BrushStyleEditableProperty(R.string.histogramFillDownStyle, name, SolidBrushStyle(
        com.scitrader.finance.pane.series.Constants.DefaultFillDown)) { id, _ ->
        onPropertyChanged(id)
    }

    init {
        renderableSeries.paletteProvider = FinanceSeriesPaletteProvider(
            FastColumnRenderableSeries::class.java,
            arrayOf(yValues)
        ) { map, index ->
            val yValue = map[yValues]?.get(index)!!

            var opacity = opacity.value
            if (yValue < 0)
                ColorUtil.argb(fillDownBrushStyle.value.color, opacity)
            else
                ColorUtil.argb(fillUpBrushStyle.value.color, opacity)
        }
    }

    override fun reset() {
        super.reset()

        fillUpBrushStyle.reset()
        fillDownBrushStyle.reset()
    }
}
