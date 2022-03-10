package com.scitrader.finance.study

import com.scitrader.finance.indicators.Indicator
import com.scitrader.finance.state.IndicatorStudyState
import com.scitrader.finance.state.PriceSeriesStudyState
import com.scitrader.finance.study.studies.PriceSeriesStudy

interface IStudyFactory {
    fun createDefaultIndicatorStudyFor(indicator: Indicator) : IStudy

    fun restoreIndicatorStudyFromState(studyState: IndicatorStudyState): IStudy
    fun saveIndicatorStudyState(study: IStudy): IndicatorStudyState

    fun restorePriceSeriesStudyFromState(state: PriceSeriesStudyState): PriceSeriesStudy
    fun savePriceSeriesStudyState(study: PriceSeriesStudy): PriceSeriesStudyState
}
