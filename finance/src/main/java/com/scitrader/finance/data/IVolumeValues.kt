package com.scitrader.finance.data

import com.scichart.core.model.DoubleValues
import com.scitrader.finance.core.DataSourceId

interface IVolumeValues : ITickValues{
    val volumeValues: DoubleValues

    val volumeValuesId: DataSourceId
}
