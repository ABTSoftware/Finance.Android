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

class EMAStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("EMA"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    yValuesId: DataSourceId = DataSourceId.DEFAULT_Y_VALUES_ID,
) : CandleStudyBase(pane, id) {
    private val emaOutputId: DataSourceId = DataSourceId.uniqueId(id, "EMA")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val emaIndicator = TALibIndicatorProvider.EMAIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultPeriod,
        yValuesId,
        emaOutputId
    )

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val emaSeries: LineFinanceSeries

    init {
        indicators.add(emaIndicator)

        emaSeries = LineFinanceSeries(
            R.string.emaIndicatorName,
            xValuesId,
            emaIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(emaSeries)
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

        emaIndicator.reset()
        emaSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != emaIndicator.input
    }

    override val title: CharSequence
        get() = "EMA(${emaIndicator.input} ${emaIndicator.period})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return EMATooltip(context, this)
    }

    class EMATooltip(context: Context, study: EMAStudy) : StudyTooltipBase<EMAStudy>(context, study) {
        private val emaSeriesTooltip = study.emaSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                emaSeriesTooltip.placeInto(this)
            else
                emaSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            emaSeriesTooltip.tryUpdate(x, y)
        }
    }
}
