package com.scitrader.finance.pane.axes

import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.study.StudyId

data class AxisId(val pane: PaneId, val study: StudyId, val axisName: String) {
    override fun toString(): String {
        return "${pane.id}${separator}${study}${separator}${axisName}"
    }

    companion object {
        private val separator: Char
        get() = '⎯'

        fun fromString(axisId: String) : AxisId{
            val split = axisId.split(separator)
            if(split.size == 3) {
                return AxisId(PaneId(split[0]), StudyId(split[1]), split[2])
            } else
                throw UnsupportedOperationException("$axisId can't be converted to AxisId instance")
        }
    }
}
