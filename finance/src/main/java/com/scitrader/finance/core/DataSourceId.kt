package com.scitrader.finance.core

import com.scitrader.finance.study.StudyId

data class DataSourceId(val id: String) {
    override fun toString(): String {
        return id
    }

    companion object {
        fun uniqueId(studyId: StudyId, name: String) : DataSourceId{
            return DataSourceId("$studyId:$name")
        }

        val DEFAULT_X_VALUES_ID = DataSourceId("xValues")

        val DEFAULT_OPEN_VALUES_ID = DataSourceId("open")
        val DEFAULT_HIGH_VALUES_ID = DataSourceId("high")
        val DEFAULT_LOW_VALUES_ID = DataSourceId("low")
        val DEFAULT_CLOSE_VALUES_ID = DataSourceId("close")
        val DEFAULT_VOLUME_VALUES_ID = DataSourceId("volume")

        val DEFAULT_Y_VALUES_ID = DEFAULT_CLOSE_VALUES_ID
    }
}
