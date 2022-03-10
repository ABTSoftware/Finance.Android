package com.scitrader.finance.pane

import android.view.View
import com.scichart.charting.visuals.ISciChartSurface
import com.scichart.charting.visuals.axes.IAxis
import com.scitrader.finance.ISciFinanceChart
import com.scitrader.finance.core.ui.ResizeableStackLayout
import com.scitrader.finance.state.IPanePropertyContainer
import com.scitrader.finance.study.IStudy
import com.scitrader.finance.study.StudyId

interface IPane : IPanePropertyContainer{
    val layoutParams : ResizeableStackLayout.LayoutParams
    val rootView: View
    val paneId: PaneId
    val chart: ISciChartSurface
    val xAxis: IAxis

    var isCursorEnabled: Boolean

    var chartTheme: Int
    var isXAxisVisible: Boolean

    var isExpandButtonEnabled: Boolean

    fun placeInto(financeChart: ISciFinanceChart)
    fun removeFrom(financeChart: ISciFinanceChart)

    fun addStudy(study: IStudy)
    fun removeStudy(study: IStudy)

    val hasStudies: Boolean

    fun onStudyChanged(studyId: StudyId)

    fun onExpandAnimationStart()
    fun onExpandAnimationFinish()
}
