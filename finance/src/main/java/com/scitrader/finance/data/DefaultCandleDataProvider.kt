package com.scitrader.finance.data

import com.scichart.core.model.DateValues
import com.scichart.core.model.DoubleValues
import com.scitrader.finance.core.DataSourceChangedArgs
import com.scitrader.finance.core.IDataManager
import com.scitrader.finance.core.OhlcvDataSourceId

open class DefaultCandleDataProvider(ohlcvDataSourceId: OhlcvDataSourceId = OhlcvDataSourceId.DEFAULT_OHLCV_VALUES_IDS) : com.scitrader.finance.core.DataManagerAttachable(), ICandleDataProvider{
    final override val xValues = DateValues()
    final override val xValuesId = ohlcvDataSourceId.xValuesId

    final override val openValues = DoubleValues()
    final override val highValues = DoubleValues()
    final override val lowValues = DoubleValues()
    final override val closeValues = DoubleValues()

    final override val openValuesId = ohlcvDataSourceId.openValuesId
    final override val highValuesId = ohlcvDataSourceId.highValuesId
    final override val lowValuesId = ohlcvDataSourceId.lowValuesId
    final override val closeValuesId = ohlcvDataSourceId.closeValuesId

    final override val volumeValues = DoubleValues()
    final override val volumeValuesId = ohlcvDataSourceId.volumeValuesId

    private val outputChangedArgs = DataSourceChangedArgs(xValuesId, openValuesId, highValuesId, lowValuesId, closeValuesId, volumeValuesId)

    override fun onDataManagerAttached(dataManager: IDataManager) {
        dataManager.registerXValuesSource(xValuesId, xValues)

        dataManager.registerYValuesSource(openValuesId, openValues)
        dataManager.registerYValuesSource(highValuesId, highValues)
        dataManager.registerYValuesSource(lowValuesId, lowValues)
        dataManager.registerYValuesSource(closeValuesId, closeValues)

        dataManager.registerYValuesSource(volumeValuesId, volumeValues)
    }

    override fun onDataManagerDetached(dataManager: IDataManager) {
        dataManager.unregisterXValuesSource(xValuesId)

        dataManager.unregisterYValuesSource(openValuesId)
        dataManager.unregisterYValuesSource(highValuesId)
        dataManager.unregisterYValuesSource(lowValuesId)
        dataManager.unregisterYValuesSource(closeValuesId)

        dataManager.unregisterYValuesSource(volumeValuesId)
    }

    fun clear() {
        writeLock {
            xValues.clear()

            openValues.clear()
            highValues.clear()
            lowValues.clear()
            closeValues.clear()

            volumeValues.clear()
            onDataProviderDrasticallyChanged()
        }
    }

    fun writeLock(modifyAction: () -> Unit) {
        val dataManager = dataManager

        // if dataManager is available update data using lock,
        // otherwise data provider isn't attached to chart so there is no need to use lock
        // and we just update data stored in data provider
        if(dataManager != null) {
            dataManager.writeLock(modifyAction)
        } else {
            modifyAction.invoke()
        }
    }
    
    open fun onDataProviderDrasticallyChanged() {
        dataManager?.onDataSourceChanged(outputChangedArgs)
    }
}
