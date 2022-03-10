package com.scitrader.finance.study.studies

import android.content.Context
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.indicators.TALibIndicatorProvider
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.series.LineFinanceSeries
import com.scitrader.finance.study.CandleStudyBase
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.study.tooltips.IStudyTooltip
import com.scitrader.finance.study.tooltips.StudyTooltipBase

class RSIStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("RSI"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    yValuesId: DataSourceId = DataSourceId.DEFAULT_Y_VALUES_ID,
) : CandleStudyBase(pane, id) {
    private val rsiOutputId: DataSourceId = DataSourceId.uniqueId(id, "RSI")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val rsiIndicator = TALibIndicatorProvider.RSIIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultPeriod,
        yValuesId,
        rsiOutputId
    )

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val rsiSeries: LineFinanceSeries

    init {
        indicators.add(rsiIndicator)

        rsiSeries = LineFinanceSeries(
            R.string.rsiIndicatorName,
            xValuesId,
            rsiIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(rsiSeries)
        }
    }

    override fun reset() {
        super.reset()

        rsiIndicator.reset()
        rsiSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != rsiIndicator.input
    }

    override val title: CharSequence
        get() = "RSI(${rsiIndicator.input} ${rsiIndicator.period})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return RSITooltip(context, this)
    }

    class RSITooltip(context: Context, study: RSIStudy) : StudyTooltipBase<RSIStudy>(context, study) {
        private val rsiSeriesTooltip = study.rsiSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                rsiSeriesTooltip.placeInto(this)
            else
                rsiSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            rsiSeriesTooltip.tryUpdate(x, y)
        }
    }
}
