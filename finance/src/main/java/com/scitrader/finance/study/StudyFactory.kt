package com.scitrader.finance.study

import com.scitrader.finance.indicators.Indicator
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.state.EditablePropertyState
import com.scitrader.finance.state.IndicatorStudyState
import com.scitrader.finance.state.PriceSeriesStudyState
import com.scitrader.finance.study.studies.*

class StudyFactory : IStudyFactory {
    override fun createDefaultIndicatorStudyFor(indicator: Indicator) : IStudy {

        val paneId = if (indicator.needsSeparatePane) PaneId.uniqueId(indicator.name) else PaneId.DEFAULT_PANE
        return when (indicator) {
            Indicator.MACD -> MacdStudy(paneId)
            Indicator.SMA -> SMAStudy(paneId)
            Indicator.BBANDS -> BBandsStudy(paneId)
            Indicator.RSI -> RSIStudy(paneId)
            Indicator.HT_TRENDLINE -> HT_TrendlineStudy(paneId)
            Indicator.STDDEV -> STDDevStudy(paneId)
            Indicator.EMA -> EMAStudy(paneId)
            Indicator.ADX -> ADXStudy(paneId)
            Indicator.ATR -> ATRStudy(paneId)
            Indicator.CCI -> CCIStudy(paneId)
            Indicator.OBV -> OBVStudy(paneId)
            Indicator.SAR -> SARStudy(paneId)
            Indicator.STOCH -> StochStudy(paneId)
        }
    }

    override fun restoreIndicatorStudyFromState(studyState: IndicatorStudyState): IStudy {
        with(studyState) {
            return when (studyState.indicator) {
                Indicator.MACD -> MacdStudy(paneId, studyId)
                Indicator.SMA -> SMAStudy(paneId, studyId)
                Indicator.BBANDS -> BBandsStudy(paneId, studyId)
                Indicator.RSI -> RSIStudy(paneId, studyId)
                Indicator.HT_TRENDLINE -> HT_TrendlineStudy(paneId, studyId)
                Indicator.STDDEV -> STDDevStudy(paneId, studyId)
                Indicator.EMA -> EMAStudy(paneId, studyId)
                Indicator.ADX -> ADXStudy(paneId, studyId)
                Indicator.ATR -> ATRStudy(paneId, studyId)
                Indicator.CCI -> CCIStudy(paneId, studyId)
                Indicator.OBV -> OBVStudy(paneId, studyId)
                Indicator.SAR -> SARStudy(paneId, studyId)
                Indicator.STOCH -> StochStudy(paneId, studyId)
            }.apply {
                restorePropertyStateFrom(studyState.properties)
            }
        }
    }

    override fun saveIndicatorStudyState(study: IStudy): IndicatorStudyState {
        val indicator = when (study) {
            is MacdStudy -> Indicator.MACD
            is RSIStudy -> Indicator.RSI
            is SMAStudy -> Indicator.SMA
            is BBandsStudy -> Indicator.BBANDS
            is HT_TrendlineStudy -> Indicator.HT_TRENDLINE
            is STDDevStudy -> Indicator.STDDEV
            is EMAStudy -> Indicator.EMA
            is ADXStudy -> Indicator.ADX
            is ATRStudy -> Indicator.ATR
            is CCIStudy -> Indicator.CCI
            is OBVStudy -> Indicator.OBV
            is SARStudy -> Indicator.SAR
            is StochStudy -> Indicator.STOCH

            else -> throw UnsupportedOperationException()
        }

        return IndicatorStudyState(
            indicator,
            study.pane,
            study.id,
            study.toProperties()
        )
    }

    override fun restorePriceSeriesStudyFromState(state: PriceSeriesStudyState): PriceSeriesStudy {
         return with(state) {
             PriceSeriesStudy(paneId, studyId).apply {
                 restorePropertyStateFrom(properties)
             }
         }
    }

    override fun savePriceSeriesStudyState(study: PriceSeriesStudy): PriceSeriesStudyState {
        return with(study) {
            PriceSeriesStudyState(pane, id, toProperties())
        }
    }

    companion object {
        private fun IStudy.toProperties(): EditablePropertyState {
            return EditablePropertyState().also {
                this.savePropertyStateTo(it)
            }
        }
    }
}
