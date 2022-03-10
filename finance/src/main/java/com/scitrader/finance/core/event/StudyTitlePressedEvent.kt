package com.scitrader.finance.core.event

import com.scitrader.finance.study.IStudy

data class StudyTitlePressedEvent(val study: IStudy) : IStudyEvent {
}
