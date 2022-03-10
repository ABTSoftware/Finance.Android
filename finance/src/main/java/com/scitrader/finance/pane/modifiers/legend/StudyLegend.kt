package com.scitrader.finance.pane.modifiers.legend

import android.content.Context
import android.util.AttributeSet
import com.scichart.charting.visuals.layout.LinearViewContainer
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.study.tooltips.IStudyTooltip

class StudyLegend @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearViewContainer(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
    }

    private val studyTooltips = HashMap<StudyId, IStudyTooltip>()

    var showSeriesTooltips: Boolean = false
    set(value) {
        if(field == value) return

        field = value
        for (item in studyTooltips) {
            item.value.showSeriesTooltips = value
        }
    }

    fun onStudyChanged(studyId: StudyId) {
        studyTooltips[studyId]?.update()
    }

    fun removeTooltip(studyId: StudyId) {
        studyTooltips.remove(studyId)?.removeFrom(this)
    }

    fun addTooltip(studyTooltip: IStudyTooltip) {
        studyTooltips[studyTooltip.studyId] = studyTooltip

        studyTooltip.placeInto(this)
        studyTooltip.update()
        studyTooltip.showSeriesTooltips = showSeriesTooltips
    }

    fun tryUpdateTooltips(x: Float, y: Float) {
        for (item in studyTooltips) {
             item.value.update(x, y)
        }
    }

    fun tryUpdateTooltips() {
        for (item in studyTooltips) {
            item.value.update()
        }
    }
}
