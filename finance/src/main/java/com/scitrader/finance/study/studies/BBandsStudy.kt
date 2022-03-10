package com.scitrader.finance.study.studies

import android.content.Context
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.financeChart.InstrumentPriceFormatChangedEvent
import com.scitrader.finance.indicators.TALibIndicatorProvider
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.series.BandFinanceSeries
import com.scitrader.finance.pane.series.LineFinanceSeries
import com.scitrader.finance.study.CandleStudyBase
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.study.tooltips.IStudyTooltip
import com.scitrader.finance.study.tooltips.StudyTooltipBase
import com.scitrader.finance.utils.SolidPenStyle
import com.scitrader.finance.pane.series.Constants as FinanceConstants
import com.scitrader.finance.study.studies.Constants as SciTraderConstants

class BBandsStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("BBands"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    yValuesId: DataSourceId = DataSourceId.DEFAULT_Y_VALUES_ID,
) : CandleStudyBase(pane, id) {
    private val lowerBandId = DataSourceId.uniqueId(id, "LowerBand")
    private val middleBandId = DataSourceId.uniqueId(id, "MiddleBand")
    private val upperBandId = DataSourceId.uniqueId(id, "UpperBand")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val bBandsIndicator = TALibIndicatorProvider.BBandsIndicator(
        SciTraderConstants.Indicator.defaultPeriod,
        SciTraderConstants.Indicator.defaultDev,
        SciTraderConstants.Indicator.defaultDev,
        SciTraderConstants.Indicator.defaultMaType,
        yValuesId,
        lowerBandId,
        middleBandId,
        upperBandId,
    )

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val bbandsBand: BandFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val midBBands: LineFinanceSeries

    init {
        indicators.add(bBandsIndicator)

        midBBands = LineFinanceSeries(
            R.string.bBandsMidId,
            xValuesId,
            bBandsIndicator.middleBandId,
            yAxisId
        ).apply {
            strokeStyle.updateInitialValue(SolidPenStyle(FinanceConstants.DefaultRed, FinanceConstants.DefaultThickness))
        }

        bbandsBand = BandFinanceSeries(
            R.string.bBandsBandId,
            xValuesId,
            lowerBandId,
            upperBandId,
            yAxisId
        )

        financeSeries.apply {
            add(midBBands)
            add(bbandsBand)
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

        bBandsIndicator.reset()
        midBBands.reset()
        bbandsBand.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != bbandsBand.fillY1BrushStyle && editable != bBandsIndicator.input
    }

    override val title: CharSequence
        get() = "BB(${bBandsIndicator.period} ${bBandsIndicator.input} ${bBandsIndicator.devUp} ${bBandsIndicator.devDown} ${bBandsIndicator.maType})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return BBandsTooltip(context, this)
    }

    class BBandsTooltip(context: Context, study: BBandsStudy) : StudyTooltipBase<BBandsStudy>(context, study) {
        private val bbandsBandTooltip = study.bbandsBand.getTooltip()
        private val midBandsTooltip = study.midBBands.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips) {
                midBandsTooltip.placeInto(this)
                bbandsBandTooltip.placeInto(this)
            } else {
                midBandsTooltip.removeFrom(this)
                bbandsBandTooltip.removeFrom(this)
            }
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            bbandsBandTooltip.tryUpdate(x, y)
            midBandsTooltip.tryUpdate(x, y)
        }
    }
}
