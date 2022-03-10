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
import com.scitrader.finance.utils.SolidPenStyle
import com.tictactec.ta.lib.MAType
import com.scitrader.finance.pane.series.Constants as FinanceConstants
import com.scitrader.finance.study.studies.Constants as SciTraderConstants

class StochStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("STOCH"),
    xValuesId: DataSourceId = DataSourceId.DEFAULT_X_VALUES_ID,
    highValuesId: DataSourceId = DataSourceId.DEFAULT_HIGH_VALUES_ID,
    lowValuesId: DataSourceId = DataSourceId.DEFAULT_LOW_VALUES_ID,
    closeValuesId: DataSourceId = DataSourceId.DEFAULT_CLOSE_VALUES_ID
) : CandleStudyBase(pane, id) {
    private val slowKOutputId: DataSourceId = DataSourceId.uniqueId(id, "SlowK")
    private val slowDOutputId: DataSourceId = DataSourceId.uniqueId(id, "SlowD")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val slowKSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val slowDSeries: LineFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val stochIndicator = TALibIndicatorProvider.StochIndicator(
        SciTraderConstants.Indicator.defaultFast,
        SciTraderConstants.Indicator.defaultSlow,
        SciTraderConstants.Indicator.defaultSlow,
        MAType.Sma,
        MAType.Sma,
        highValuesId,
        lowValuesId,
        closeValuesId,
        slowKOutputId,
        slowDOutputId
    )

    init {
        indicators.add(stochIndicator)

        slowKSeries = LineFinanceSeries(
            R.string.stochSlowKId,
            xValuesId,
            stochIndicator.slowKId,
            yAxisId
        )

        slowDSeries = LineFinanceSeries(
            R.string.stochSlowDId,
            xValuesId,
            stochIndicator.slowDId,
            yAxisId
        ).apply {
            strokeStyle.updateInitialValue(SolidPenStyle(FinanceConstants.DefaultRed, FinanceConstants.DefaultThickness))
        }

        financeSeries.apply {
            add(slowKSeries)
            add(slowDSeries)
        }
    }

    override fun reset() {
        super.reset()

        stochIndicator.reset()
        slowKSeries.reset()
        slowDSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != stochIndicator.inputClose &&
                editable != stochIndicator.inputHigh &&
                editable != stochIndicator.inputLow
    }

    override val title: CharSequence
        get() = "STOCH(${stochIndicator.slowK_maType} ${stochIndicator.fastK} ${stochIndicator.slowK} ${stochIndicator.slowD_maType} ${stochIndicator.slowD})"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return StochTooltip(context, this)
    }

    class StochTooltip(context: Context, study: StochStudy) : StudyTooltipBase<StochStudy>(context, study) {
        private val slowKTooltip = study.slowKSeries.getTooltip()
        private val slowDTooltip = study.slowDSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips) {
                slowKTooltip.placeInto(this)
                slowDTooltip.placeInto(this)
            } else {
                slowKTooltip.removeFrom(this)
                slowDTooltip.removeFrom(this)
            }
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            slowKTooltip.tryUpdate(x, y)
            slowDTooltip.tryUpdate(x, y)
        }
    }
}
