package com.scitrader.finance.core

import com.scichart.charting.utility.IReadWriteLock
import com.scichart.charting.utility.ReadWriteLock
import com.scichart.core.model.DateValues
import com.scichart.core.model.DoubleValues
import java.util.concurrent.ConcurrentHashMap

open class DataManager() : com.scitrader.finance.core.IDataManager {
    private val observers = HashSet<com.scitrader.finance.core.IDataManagerObserver>()

    private val xValuesMap = ConcurrentHashMap<com.scitrader.finance.core.DataSourceId, DateValues>()
    private val yValuesMap = ConcurrentHashMap<com.scitrader.finance.core.DataSourceId, DoubleValues>()

    override val lock: IReadWriteLock = ReadWriteLock()

    override fun writeLock(modifyAction: () -> Unit) {
        try {
            lock.writeLock()

            modifyAction.invoke()
        } finally {
            lock.writeUnlock()
        }
    }

    override fun readLock(modifyAction: () -> Unit) {
        try {
            lock.readLock()

            modifyAction.invoke()
        } finally {
            lock.readUnlock()
        }
    }

    /**
     * Adds the [IDataManagerObserver] instance into the list to notify if this instance changes
     * @param observer The observer to add
     */
    override fun addDataManagerObserver(observer: com.scitrader.finance.core.IDataManagerObserver) {
        synchronized(observers) {
            observers.add(observer)
        }
    }

    /**
     * Removes the [IDataManagerObserver] instance from the list to notify if this instance changes
     * @param observer The observer to remove
     */
    override fun removeDataManagerObserver(observer: com.scitrader.finance.core.IDataManagerObserver) {
        synchronized(observers){
            observers.remove(observer)
        }
    }

    final override fun getYValues(id: com.scitrader.finance.core.DataSourceId): DoubleValues? {
        return yValuesMap[id]
    }

    final override fun getXValues(id: com.scitrader.finance.core.DataSourceId): DateValues? {
        return xValuesMap[id]
    }

    final override fun registerYValuesSource(id: com.scitrader.finance.core.DataSourceId, values: DoubleValues) {
        yValuesMap[id] = values
    }

    final override fun registerXValuesSource(id: com.scitrader.finance.core.DataSourceId, values: DateValues) {
        xValuesMap[id] = values
    }

    final override fun unregisterYValuesSource(id: com.scitrader.finance.core.DataSourceId) {
        yValuesMap.remove(id)
    }

    final override fun unregisterXValuesSource(id: com.scitrader.finance.core.DataSourceId) {
        xValuesMap.remove(id)
    }

    final override fun onDataSourceChanged(args: com.scitrader.finance.core.DataSourceChangedArgs) {
        synchronized(observers){
            for (observer in observers) {
                observer.onDataSourceChanged(this, args)
            }
        }
    }
}
