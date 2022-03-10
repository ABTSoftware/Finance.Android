package com.scitrader.finance.data

import com.scichart.core.model.DoubleValues
import com.scitrader.finance.core.DataSourceId

interface IOhlcValues : ITickValues{
    val openValues: DoubleValues
    val highValues: DoubleValues
    val lowValues: DoubleValues
    val closeValues: DoubleValues

    val openValuesId: DataSourceId
    val highValuesId: DataSourceId
    val lowValuesId: DataSourceId
    val closeValuesId: DataSourceId
}
