package com.scitrader.finance.core

import com.scichart.core.IServiceContainer

abstract class DataManagerAttachable : com.scitrader.finance.core.AttachableBase() {
    protected var dataManager: com.scitrader.finance.core.IDataManager? = null

    override fun attachTo(services: IServiceContainer?) {
        super.attachTo(services)

        dataManager = this.services.getService(com.scitrader.finance.core.IDataManager::class.java)
        dataManager?.let {
            onDataManagerAttached(it)
        }
    }

    protected abstract fun onDataManagerAttached(dataManager: com.scitrader.finance.core.IDataManager)

    override fun detach() {
        dataManager?.let {
            onDataManagerDetached(it)
        }
        dataManager = null

        super.detach()
    }

    protected abstract fun onDataManagerDetached(dataManager: com.scitrader.finance.core.IDataManager)

}
