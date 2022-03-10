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

class ADXStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("ADX"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    highValuesId: DataSourceId = DataSourceId.DEFAULT_HIGH_VALUES_ID,
    lowValuesId: DataSourceId = DataSourceId.DEFAULT_LOW_VALUES_ID,
    closeValuesId: DataSourceId = DataSourceId.DEFAULT_CLOSE_VALUES_ID
) : CandleStudyBase(pane, id) {
    private val adxOutputId: DataSourceId = DataSourceId.uniqueId(id, "ADX")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val adxSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val adxIndicator = TALibIndicatorProvider.ADXIndicator(
        com.scitrader.finance.study.studies.Constants.Indicator.defaultPeriod,
        highValuesId,
        lowValuesId,
        closeValuesId,
        adxOutputId
    )

    init {
        indicators.add(adxIndicator)

        adxSeries = LineFinanceSeries(
            R.string.adxIndicatorName,
            xValuesId,
            adxIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(adxSeries)
        }
    }

    override val title: CharSequence
        get() = "ADX(${adxIndicator.inputHigh} ${adxIndicator.inputLow} ${adxIndicator.inputClose} ${adxIndicator.period})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return ADXTooltip(context, this)
    }

    override fun reset() {
        super.reset()

        adxIndicator.reset()
        adxSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != adxIndicator.inputClose &&
                editable != adxIndicator.inputHigh &&
                editable != adxIndicator.inputLow
    }

    class ADXTooltip(context: Context, study: ADXStudy) : StudyTooltipBase<ADXStudy>(context, study) {
        private val adxSeriesTooltip = study.adxSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                adxSeriesTooltip.placeInto(this)
            else
                adxSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            adxSeriesTooltip.tryUpdate(x, y)
        }
    }
}
