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

class ATRStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("ATR"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    highValuesId: DataSourceId = DataSourceId.DEFAULT_HIGH_VALUES_ID,
    lowValuesId: DataSourceId = DataSourceId.DEFAULT_LOW_VALUES_ID,
    closeValuesId: DataSourceId = DataSourceId.DEFAULT_CLOSE_VALUES_ID
) : CandleStudyBase(pane, id) {
    private val atrOutputId: DataSourceId = DataSourceId.uniqueId(id, "ATR")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val atrSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val atrIndicator = TALibIndicatorProvider.ATRIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultPeriod,
        highValuesId,
        lowValuesId,
        closeValuesId,
        atrOutputId
    )

    init {
        indicators.add(atrIndicator)

        atrSeries = LineFinanceSeries(
            R.string.atrIndicatorName,
            xValuesId,
            atrIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(atrSeries)
        }
    }

    override fun reset() {
        super.reset()

        atrIndicator.reset()
        atrSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != atrIndicator.inputClose &&
                editable != atrIndicator.inputHigh &&
                editable != atrIndicator.inputLow
    }

    override val title: CharSequence
        get() = "ATR(${atrIndicator.inputHigh} ${atrIndicator.inputLow} ${atrIndicator.inputClose} ${atrIndicator.period})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return ATRTooltip(context, this)
    }

    class ATRTooltip(context: Context, study: ATRStudy) : StudyTooltipBase<ATRStudy>(context, study) {
        private val atrSeriesTooltip = study.atrSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                atrSeriesTooltip.placeInto(this)
            else
                atrSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            atrSeriesTooltip.tryUpdate(x, y)
        }
    }
}
