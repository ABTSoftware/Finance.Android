package com.scitrader.finance.core.event

import com.scitrader.finance.core.ui.ResizeableStackLayout

data class FinanceChartLayoutUpdateEvent(val storedLayoutParams: Map<String, ResizeableStackLayout.LayoutParams>) :
    IFinanceChartEvent {
}
