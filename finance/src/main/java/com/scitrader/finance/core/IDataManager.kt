package com.scitrader.finance.core

import com.scichart.charting.utility.IReadWriteLock
import com.scichart.core.model.DateValues
import com.scichart.core.model.DoubleValues

interface IDataManager {
    val lock: IReadWriteLock

    fun writeLock(modifyAction: () -> Unit)
    fun readLock(modifyAction: () -> Unit)

    fun getYValues(id: DataSourceId) : DoubleValues?
    fun getXValues(id: DataSourceId) : DateValues?

    fun registerYValuesSource(id: DataSourceId, values: DoubleValues)
    fun registerXValuesSource(id: DataSourceId, values: DateValues)

    fun unregisterYValuesSource(id: DataSourceId)
    fun unregisterXValuesSource(id: DataSourceId)

    fun addDataManagerObserver(observer: IDataManagerObserver)
    fun removeDataManagerObserver(observer: IDataManagerObserver)

    fun onDataSourceChanged(args: DataSourceChangedArgs)
}

interface IDataManagerObserver {
    fun onDataSourceChanged(dataManager: IDataManager, args: DataSourceChangedArgs)
}
