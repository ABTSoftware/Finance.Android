package com.scitrader.finance.pane.series

import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.charting.visuals.renderableSeries.data.XSeriesRenderPassData
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IFillPaletteProvider
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IStrokePaletteProvider
import com.scichart.charting.visuals.renderableSeries.paletteProviders.PaletteProviderBase
import com.scichart.core.IServiceContainer
import com.scichart.core.model.DoubleValues
import com.scichart.core.model.IntegerValues
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.IDataManager

class FinanceSeriesPaletteProvider<TSeries : IRenderableSeries?>(seriesType: Class<TSeries>, private val inputs: Array<DataSourceId>, private val seriesColorFunction: (Map<DataSourceId, DoubleValues>, Int) -> Int) : PaletteProviderBase<TSeries>(seriesType), IFillPaletteProvider, IStrokePaletteProvider {
    private val fillColors = IntegerValues()
    private val strokeColors = IntegerValues()
    
    private var dataManager: IDataManager? = null

    override fun attachTo(services: IServiceContainer) {
        super.attachTo(services)

        dataManager = services.getService(IDataManager::class.java)
    }

    override fun detach() {
        dataManager = null
        
        super.detach()
    }

    override fun update() {
        renderableSeries?.apply {
            val renderPassData = currentRenderPassData as XSeriesRenderPassData
            val indices = renderPassData.indices

            val size = indices.size()
            fillColors.setSize(size)
            strokeColors.setSize(size)

            dataManager?.run {
                val dataMap = HashMap<DataSourceId, DoubleValues>()
                for (input in inputs) {
                    val yValues = getYValues(input)
                    if (yValues != null)
                        dataMap[input] = yValues
                }

                val indicesArray = indices.itemsArray
                val fillColorsArray = fillColors.itemsArray
                val strokeColorsArray = strokeColors.itemsArray

                readLock {
                    for (index in 0 until size) {
                        val color = seriesColorFunction(dataMap, indicesArray[index])
                        fillColorsArray[index] = color
                        strokeColorsArray[index] = color
                    }
                }
            }
        }
    }

    override fun getFillColors(): IntegerValues {
        return fillColors
    }

    override fun getStrokeColors(): IntegerValues {
        return strokeColors
    }
}
