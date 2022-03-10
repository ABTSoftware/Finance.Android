package com.scitrader.finance.state

import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.study.StudyId

data class PriceSeriesStudyState(val paneId: PaneId, val studyId: StudyId, val properties: EditablePropertyState)
