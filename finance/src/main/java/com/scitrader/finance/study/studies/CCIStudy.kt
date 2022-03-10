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

class CCIStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("CCI"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    highValuesId: DataSourceId = DataSourceId.DEFAULT_HIGH_VALUES_ID,
    lowValuesId: DataSourceId = DataSourceId.DEFAULT_LOW_VALUES_ID,
    closeValuesId: DataSourceId = DataSourceId.DEFAULT_CLOSE_VALUES_ID
) : CandleStudyBase(pane, id) {
    private val cciOutputId: DataSourceId = DataSourceId.uniqueId(id, "CCI")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val cciSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val cciIndicator = TALibIndicatorProvider.CCIIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultPeriod,
        highValuesId,
        lowValuesId,
        closeValuesId,
        cciOutputId
    )

    init {
        indicators.add(cciIndicator)

        cciSeries = LineFinanceSeries(
            R.string.cciIndicatorName,
            xValuesId,
            cciIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(cciSeries)
        }
    }

    override fun reset() {
        super.reset()

        cciIndicator.reset()
        cciSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != cciIndicator.inputClose &&
                editable != cciIndicator.inputHigh &&
                editable != cciIndicator.inputLow
    }

    override val title: CharSequence
        get() = "CCI(${cciIndicator.inputHigh} ${cciIndicator.inputLow} ${cciIndicator.inputClose} ${cciIndicator.period})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return CCITooltip(context, this)
    }

    class CCITooltip(context: Context, study: CCIStudy) : StudyTooltipBase<CCIStudy>(context, study) {
        private val cciSeriesTooltip = study.cciSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                cciSeriesTooltip.placeInto(this)
            else
                cciSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            cciSeriesTooltip.tryUpdate(x, y)
        }
    }
}
