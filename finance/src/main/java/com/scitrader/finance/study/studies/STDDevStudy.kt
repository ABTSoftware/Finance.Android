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

class STDDevStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("STDDEV"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    yValuesId: DataSourceId = DataSourceId.DEFAULT_Y_VALUES_ID,
) : CandleStudyBase(pane, id) {
    private val stdDevOutputId: DataSourceId = DataSourceId.uniqueId(id, "STDDEV")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val stdDevIndicator = TALibIndicatorProvider.STDDevIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultPeriod,
        com.scitrader.finance.study.studies.Constants.Indicator.defaultDev,
        yValuesId,
        stdDevOutputId
    )

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val stdDevSeries: LineFinanceSeries

    init {
        indicators.add(stdDevIndicator)

        stdDevSeries = LineFinanceSeries(
            R.string.stdDevIndicatorName,
            xValuesId,
            stdDevIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(stdDevSeries)
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

        stdDevIndicator.reset()
        stdDevSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != stdDevIndicator.input
    }

    override val title: CharSequence
        get() = "STDDEV(${stdDevIndicator.input} ${stdDevIndicator.period} ${stdDevIndicator.dev})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return STDDevTooltip(context, this)
    }

    class STDDevTooltip(context: Context, study: STDDevStudy) : StudyTooltipBase<STDDevStudy>(context, study) {
        private val stdDevSeriesTooltip = study.stdDevSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                stdDevSeriesTooltip.placeInto(this)
            else
                stdDevSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            stdDevSeriesTooltip.tryUpdate(x, y)
        }
    }
}
