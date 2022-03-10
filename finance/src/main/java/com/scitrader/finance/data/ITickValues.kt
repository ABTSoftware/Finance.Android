package com.scitrader.finance.data

import com.scichart.core.model.DateValues
import com.scitrader.finance.core.DataSourceId

interface ITickValues {
    val xValues: DateValues

    val xValuesId: DataSourceId

    val size : Int
        get() = xValues.size()
}
