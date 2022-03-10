package com.scitrader.finance.financeChart

import com.scitrader.finance.core.event.IFinanceChartEvent

data class InstrumentPriceFormatChangedEvent(val priceFormat: String) : IFinanceChartEvent
