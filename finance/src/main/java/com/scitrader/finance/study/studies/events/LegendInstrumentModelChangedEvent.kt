package com.scitrader.finance.study.studies.events

import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.study.tooltips.LegendInstrumentModel

data class LegendInstrumentModelChangedEvent(val model: LegendInstrumentModel) : IFinanceChartEvent
