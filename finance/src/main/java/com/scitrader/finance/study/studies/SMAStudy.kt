package com.scitrader.finance.study.studies

import android.content.Context
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.financeChart.InstrumentPriceFormatChangedEvent
import com.scitrader.finance.indicators.TALibIndicatorProvider
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.series.LineFinanceSeries
import com.scitrader.finance.study.CandleStudyBase
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.study.tooltips.IStudyTooltip
import com.scitrader.finance.study.tooltips.StudyTooltipBase

class SMAStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("SMA"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    yValuesId: DataSourceId = DataSourceId.DEFAULT_Y_VALUES_ID,
) : CandleStudyBase(pane, id) {
    private val smaOutputId: DataSourceId = DataSourceId.uniqueId(id, "SMA")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val smaSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val smaIndicator = TALibIndicatorProvider.SMAIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultPeriod,
        yValuesId,
        smaOutputId
    )

    init {
        indicators.add(smaIndicator)

        smaSeries = LineFinanceSeries(
            R.string.smaIndicatorName,
            xValuesId,
            smaIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(smaSeries)
        }
    }

    override fun onEvent(event: IFinanceChartEvent) {
        super.onEvent(event)

        if (event is InstrumentPriceFormatChangedEvent) {
            yAxis.textFormatting.updateInitialValue(event.priceFormat)
            yAxis.cursorTextFormatting.updateInitialValue(event.priceFormat)
        }
    }

    override fun reset() {
        super.reset()

        smaIndicator.reset()
        smaSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != smaIndicator.input
    }

    override val title: CharSequence
        get() = "SMA(${smaIndicator.input} ${smaIndicator.period})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return SMATooltip(context, this)
    }

    class SMATooltip(context: Context, study: SMAStudy) : StudyTooltipBase<SMAStudy>(context, study) {
        private val smaSeriesTooltip = study.smaSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                smaSeriesTooltip.placeInto(this)
            else
                smaSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            smaSeriesTooltip.tryUpdate(x, y)
        }
    }
}
