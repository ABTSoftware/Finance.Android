package com.scitrader.finance.study.tooltips

import com.scichart.charting.visuals.ITooltip
import com.scitrader.finance.study.StudyId

interface IStudyTooltip : ITooltip{
    val studyId: StudyId

    fun update()
    fun update(x: Float, y: Float)

    var showSeriesTooltips: Boolean
}
