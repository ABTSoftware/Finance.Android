package com.scitrader.finance.core

import android.util.SparseArray
import com.scichart.core.IServiceContainer
import com.scitrader.finance.edit.properties.PropertyId
import com.scitrader.finance.study.IStudy
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

abstract class DependableBase : com.scitrader.finance.core.DataManagerAttachable(), IDataManagerObserver {
    private val dependsOn = SparseArray<DataSourceId>()

    private val propertyChanged = AtomicBoolean()

    //TODO: Consider refactoring WeakReference approach with IDependable with method that attaches to parent study
    private var parentStudy: WeakReference<IStudy?> = WeakReference(null)

    override fun attachTo(services: IServiceContainer?) {
        super.attachTo(services)

        parentStudy = WeakReference(services?.getService(IStudy::class.java))
    }

    protected fun dependsOn(propertyId: Int, value: DataSourceId) {
        dependsOn.put(propertyId, value)
    }

    private fun tryOnDataDrasticallyChanged(dataManager: IDataManager?) {
        dataManager?.let {
            onDataDrasticallyChanged(it)
        }
    }

    override fun onDataManagerAttached(dataManager: IDataManager) {
        dataManager.addDataManagerObserver(this)

        onDataDrasticallyChanged(dataManager)
    }

    override fun onDataManagerDetached(dataManager: IDataManager) {
        onDataDrasticallyChanged(dataManager)
        dataManager.removeDataManagerObserver(this)
    }

    override fun onDataSourceChanged(dataManager: IDataManager, args: DataSourceChangedArgs) {
        synchronized(dependsOn) {
            val needToUpdate = args.changedDataSourceIds.any { dependsOn.indexOfValue(it) >= 0 } || propertyChanged.getAndSet(false)
            if (needToUpdate) {
                onDataDrasticallyChanged(dataManager)
            }
        }
    }

    abstract fun onDataDrasticallyChanged(dataManager: IDataManager)

    protected fun onDataSourceChanged(args: DataSourceChangedArgs) {
        // set is dirty flag to update this instance even if inputs aren't changed
        // then notify all dependencies that output of this instance changed
        propertyChanged.set(true)

        // lock data manager to during data updates caused by changing property
        dataManager?.run {
            writeLock {
                onDataSourceChanged(args)
            }
        }
    }

    protected fun onPropertyChanged(propertyId: PropertyId) {
        parentStudy.get()?.onPropertyChanged(propertyId)
    }

    protected fun onDataProviderChanged(args: DataSourceChangedArgs) {
        dataManager?.onDataSourceChanged(args)
    }
}
