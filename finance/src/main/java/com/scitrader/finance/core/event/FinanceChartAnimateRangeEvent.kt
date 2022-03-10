package com.scitrader.finance.core.event

import com.scichart.data.model.IRange

data class FinanceChartAnimateRangeEvent(val range: IRange<*>) : IFinanceChartEvent {
}
