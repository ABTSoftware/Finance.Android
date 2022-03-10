package com.scitrader.finance.core.event

import com.scitrader.finance.study.IStudy

data class StudyChangedEvent(val study: IStudy) : IStudyEvent
