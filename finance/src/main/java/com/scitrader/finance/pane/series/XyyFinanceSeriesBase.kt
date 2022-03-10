package com.scitrader.finance.pane.series

import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.IXyyDataSeries
import com.scichart.charting.visuals.renderableSeries.XyyRenderableSeriesBase
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.IDataManager
import com.scitrader.finance.edit.properties.DataSourceEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import java.util.*

abstract class XyyFinanceSeriesBase<TRenderableSeries : XyyRenderableSeriesBase, TDataSeries : IXyyDataSeries<Date, Double>>(
    @StringRes name: Int,
    xValues: DataSourceId,
    yValues: DataSourceId,
    y1Values: DataSourceId,
    renderableSeries: TRenderableSeries,
    dataSeries: TDataSeries,
    yAxisId: AxisId
) : FinanceSeriesBase<TRenderableSeries, TDataSeries>(name, renderableSeries, dataSeries, yAxisId) {
    val xValues = DataSourceEditableProperty(R.string.xValues, name, xValues) { _, value ->
        dependsOn(R.string.xValues, value)
    }

    val yValues = DataSourceEditableProperty(R.string.yValues, name, yValues) { _, value ->
        dependsOn(R.string.yValues, value)
    }

    val y1Values = DataSourceEditableProperty(R.string.y1Values, name, y1Values) { _, value ->
        dependsOn(R.string.y1Values, value)
    }

    override fun reset() {
        super.reset()

        xValues.reset()
        yValues.reset()
        y1Values.reset()
    }

    override fun onDataDrasticallyChanged(dataManager: IDataManager) {
        val suspender = renderableSeries.suspendUpdates()
        try {
            dataSeries.clear()

            dataManager.readLock {
                val xValues = dataManager.getXValues(xValues.value)
                val yValues = dataManager.getYValues(yValues.value)
                val y1Values = dataManager.getYValues(y1Values.value)

                if(xValues != null && yValues != null && y1Values != null)
                    dataSeries.append(xValues, yValues, y1Values)
            }
        } finally {
            suspender.dispose()
        }
    }
}
