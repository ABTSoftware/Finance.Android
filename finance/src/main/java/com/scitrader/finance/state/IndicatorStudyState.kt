package com.scitrader.finance.state

import com.scitrader.finance.indicators.Indicator
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.study.StudyId

data class IndicatorStudyState(
    val indicator: Indicator,
    val paneId: PaneId,
    val studyId: StudyId,
    val properties: EditablePropertyState,
)
