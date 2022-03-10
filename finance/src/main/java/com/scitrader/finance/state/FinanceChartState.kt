package com.scitrader.finance.state

data class FinanceChartState(
    val chartState: PropertyState = PropertyState(),
    val paneStates: MutableMap<String, PropertyState> = HashMap(),
)
