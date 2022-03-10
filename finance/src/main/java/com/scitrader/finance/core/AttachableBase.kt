package com.scitrader.finance.core

import com.scichart.core.AttachableServiceContainer
import com.scichart.core.IServiceContainer
import com.scichart.core.IServiceProvider
import com.scichart.core.framework.IAttachable

abstract class AttachableBase() : IAttachable, IServiceProvider {
    private val services = AttachableServiceContainer()

    override fun attachTo(services: IServiceContainer?) {
        this.services.attachTo(services)
    }

    override fun detach() {
        this.services.detach()
    }

    final override fun isAttached(): Boolean = services.isAttached

    final override fun getServices(): IServiceContainer = services
}

