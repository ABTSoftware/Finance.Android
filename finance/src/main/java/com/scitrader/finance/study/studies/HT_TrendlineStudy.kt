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

class HT_TrendlineStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("HT_TRENDLINE"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    yValuesId: DataSourceId = DataSourceId.DEFAULT_Y_VALUES_ID,
) : CandleStudyBase(pane, id) {
    private val trendlineOutputId: DataSourceId = DataSourceId.uniqueId(id, "HT_TRENDLINE")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val trendlineIndicator = TALibIndicatorProvider.HT_TrendlineIndicator(
        yValuesId,
        trendlineOutputId
    )

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val trendlineSeries: LineFinanceSeries

    init {
        indicators.add(trendlineIndicator)

        trendlineSeries = LineFinanceSeries(
            R.string.rsiIndicatorName,
            xValuesId,
            trendlineIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(trendlineSeries)
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

        trendlineIndicator.reset()
        trendlineSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != trendlineIndicator.input
    }

    override val title: CharSequence
        get() = "HT_TRENDLINE(${trendlineIndicator.input})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return TrendlineTooltip(context, this)
    }

    class TrendlineTooltip(context: Context, study: HT_TrendlineStudy) : StudyTooltipBase<HT_TrendlineStudy>(context, study) {
        private val trendlineSeriesTooltip = study.trendlineSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                trendlineSeriesTooltip.placeInto(this)
            else
                trendlineSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            trendlineSeriesTooltip.tryUpdate(x, y)
        }
    }
}
