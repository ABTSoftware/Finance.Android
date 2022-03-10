package com.scitrader.finance.pane.series

import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.IOhlcDataSeries
import com.scichart.charting.visuals.renderableSeries.OhlcRenderableSeriesBase
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.IDataManager
import com.scitrader.finance.edit.properties.DataSourceEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import java.util.*

abstract class OhlcFinanceSeriesBase<TRenderableSeries : OhlcRenderableSeriesBase, TDataSeries : IOhlcDataSeries<Date, Double>>(
    @StringRes name: Int,
    xValues: DataSourceId,
    open: DataSourceId,
    high: DataSourceId,
    low: DataSourceId,
    close: DataSourceId,
    renderableSeries: TRenderableSeries,
    dataSeries: TDataSeries,
    yAxisId: AxisId
) : FinanceSeriesBase<TRenderableSeries, TDataSeries>(name, renderableSeries, dataSeries, yAxisId) {
    val xValues = DataSourceEditableProperty(R.string.xValues, name, xValues) { _, value ->
        dependsOn(R.string.xValues, value)
    }

    val open = DataSourceEditableProperty(R.string.openValues, name, open) { _, value ->
        dependsOn(R.string.openValues, value)
    }

    val high = DataSourceEditableProperty(R.string.highValues, name, high) { _, value ->
        dependsOn(R.string.highValues, value)
    }

    val low = DataSourceEditableProperty(R.string.lowValues, name, low) { _, value ->
        dependsOn(R.string.lowValues, value)
    }

    val close = DataSourceEditableProperty(R.string.closeValues, name, close) { _, value ->
        dependsOn(R.string.closeValues, value)
    }

    override fun reset() {
        super.reset()

        xValues.reset()
        open.reset()
        high.reset()
        low.reset()
        close.reset()
    }

    override fun onDataDrasticallyChanged(dataManager: IDataManager) {
        val suspender = renderableSeries.suspendUpdates()
        try {
            dataSeries.clear()

            dataManager.readLock {
                val xValues = dataManager.getXValues(xValues.value)
                val openValues = dataManager.getYValues(open.value)
                val highValues = dataManager.getYValues(high.value)
                val lowValues = dataManager.getYValues(low.value)
                val closeValues = dataManager.getYValues(close.value)

                if (xValues != null && openValues != null && highValues != null && lowValues != null && closeValues != null)
                    dataSeries.append(xValues, openValues, highValues, lowValues, closeValues)
            }
        } finally {
            suspender.dispose()
        }
    }
}
