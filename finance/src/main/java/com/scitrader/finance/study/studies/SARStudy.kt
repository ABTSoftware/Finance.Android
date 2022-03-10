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

class SARStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("SAR"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    highValuesId: DataSourceId = DataSourceId.DEFAULT_HIGH_VALUES_ID,
    lowValuesId: DataSourceId = DataSourceId.DEFAULT_LOW_VALUES_ID
) : CandleStudyBase(pane, id) {
    private val sarOutputId: DataSourceId = DataSourceId.uniqueId(id, "SAR")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val sarSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val sarIndicator = TALibIndicatorProvider.SARIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultAcceleration,
        com.scitrader.finance.study.studies.Constants.Indicator.defaultMaximum,
        highValuesId,
        lowValuesId,
        sarOutputId
    )

    init {
        indicators.add(sarIndicator)

        sarSeries = LineFinanceSeries(
            R.string.sarIndicatorName,
            xValuesId,
            sarIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(sarSeries)
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

        sarIndicator.reset()
        sarSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != sarIndicator.inputHigh && editable != sarIndicator.inputLow
    }

    override val title: CharSequence
        get() = "SAR(${sarIndicator.inputHigh} ${sarIndicator.inputLow} ${sarIndicator.acceleration} ${sarIndicator.maximum})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return SARTooltip(context, this)
    }

    class SARTooltip(context: Context, study: SARStudy) : StudyTooltipBase<SARStudy>(context, study) {
        private val sarSeriesTooltip = study.sarSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                sarSeriesTooltip.placeInto(this)
            else
                sarSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            sarSeriesTooltip.tryUpdate(x, y)
        }
    }
}
