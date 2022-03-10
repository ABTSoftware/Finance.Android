package com.scitrader.finance.state

interface IPanePropertyContainer {
    fun savePropertyStateTo(chartState: PropertyState, paneState: PropertyState)
    fun restorePropertyStateFrom(chartState: PropertyState, paneState: PropertyState)
}
