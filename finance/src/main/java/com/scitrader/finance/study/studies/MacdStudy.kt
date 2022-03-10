package com.scitrader.finance.study.studies

import android.content.Context
import com.scichart.drawing.utility.ColorUtil
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.indicators.TALibIndicatorProvider
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.series.HistogramFinanceSeries
import com.scitrader.finance.pane.series.LineFinanceSeries
import com.scitrader.finance.study.CandleStudyBase
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.study.tooltips.IStudyTooltip
import com.scitrader.finance.study.tooltips.StudyTooltipBase
import com.scitrader.finance.utils.SolidPenStyle
import com.scitrader.finance.pane.series.Constants as FinanceConstants
import com.scitrader.finance.study.studies.Constants as SciTraderConstants

class MacdStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("MACD"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    yValuesId: DataSourceId = DataSourceId.DEFAULT_Y_VALUES_ID,
) : CandleStudyBase(pane, id) {
    private val macdId: DataSourceId = DataSourceId.uniqueId(id, "MACD")
    private val macdSignalId: DataSourceId = DataSourceId.uniqueId(id, "MACD_signal")
    private val macdHistId: DataSourceId = DataSourceId.uniqueId(id, "MACD_hist")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val histogram: HistogramFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val macd: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val signal: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val macdIndicator = TALibIndicatorProvider.MacdIndicator(
        yValuesId,
        macdId,
        macdSignalId,
        macdHistId,
        SciTraderConstants.Indicator.defaultSlow,
        SciTraderConstants.Indicator.defaultFast,
        SciTraderConstants.Indicator.defaultSignal
    )

    init {
        indicators.add(macdIndicator)

        val red = ColorUtil.rgb(0xFF, 0x55, 0x55)
        val green = ColorUtil.rgb(0x55, 0xFF, 0x55)

        histogram = HistogramFinanceSeries(
            R.string.macdHistogramId,
            xValuesId,
            macdIndicator.macdHistId,
            yAxisId
        )

        macd = LineFinanceSeries(
            R.string.macdId,
            xValuesId,
            macdIndicator.macdId,
            yAxisId
        )

        signal = LineFinanceSeries(
            R.string.macdSignalId,
            xValuesId,
            macdIndicator.macdSignalId,
            yAxisId
        ).apply {
            strokeStyle.updateInitialValue(SolidPenStyle(FinanceConstants.DefaultRed, FinanceConstants.DefaultThickness))
        }

        financeSeries.apply {
            add(histogram)
            add(macd)
            add(signal)
        }
    }

    override fun reset() {
        super.reset()

        macdIndicator.reset()
        histogram.reset()
        macd.reset()
        signal.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != macdIndicator.input
    }

    override val title: CharSequence
        get() = "MACD(${macdIndicator.input} ${macdIndicator.slow} ${macdIndicator.fast} ${macdIndicator.signal})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return MacdTooltip(context, this)
    }

    class MacdTooltip(context: Context, study: MacdStudy) :
        StudyTooltipBase<MacdStudy>(context, study) {

        private val macdSeriesTooltip = study.macd.getTooltip()
        private val signalSeriesTooltip = study.signal.getTooltip()
        private val histogramSeriesTooltip = study.histogram.getTooltip()


        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips) {
                macdSeriesTooltip.placeInto(this)
                signalSeriesTooltip.placeInto(this)
                histogramSeriesTooltip.placeInto(this)
            } else {
                macdSeriesTooltip.removeFrom(this)
                signalSeriesTooltip.removeFrom(this)
                histogramSeriesTooltip.removeFrom(this)
            }
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            macdSeriesTooltip.tryUpdate(x, y)
            signalSeriesTooltip.tryUpdate(x, y)
            histogramSeriesTooltip.tryUpdate(x, y)
        }
    }
}
