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

class OBVStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("OBV"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    closeValuesId: DataSourceId = DataSourceId.DEFAULT_CLOSE_VALUES_ID,
    volumeValuesId: DataSourceId = DataSourceId.DEFAULT_CLOSE_VALUES_ID
) : CandleStudyBase(pane, id) {
    private val obvOutputId: DataSourceId = DataSourceId.uniqueId(id, "OBV")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val obvSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val obvIndicator = TALibIndicatorProvider.OBVIndicator(
        closeValuesId,
        volumeValuesId,
        obvOutputId
    )

    init {
        indicators.add(obvIndicator)

        obvSeries = LineFinanceSeries(
            R.string.obvIndicatorName,
            xValuesId,
            obvIndicator.outputId,
            yAxisId
        )

        financeSeries.apply {
            add(obvSeries)
        }
    }

    override fun reset() {
        super.reset()

        obvIndicator.reset()
        obvSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != obvIndicator.inputClose && editable != obvIndicator.inputVolume
    }

    override val title: CharSequence
        get() = "OBV(${obvIndicator.inputClose} ${obvIndicator.inputVolume})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return OBVTooltip(context, this)
    }

    class OBVTooltip(context: Context, study: OBVStudy) : StudyTooltipBase<OBVStudy>(context, study) {
        private val obvSeriesTooltip = study.obvSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                obvSeriesTooltip.placeInto(this)
            else
                obvSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            obvSeriesTooltip.tryUpdate(x, y)
        }
    }
}
