package com.scitrader.finance.core

data class OhlcvDataSourceId(
    val xValuesId: DataSourceId,
    val openValuesId: DataSourceId,
    val highValuesId: DataSourceId,
    val lowValuesId: DataSourceId,
    val closeValuesId: DataSourceId,
    val volumeValuesId: DataSourceId,
)  {
    companion object {
        val DEFAULT_OHLCV_VALUES_IDS = OhlcvDataSourceId(
            DataSourceId.DEFAULT_X_VALUES_ID,
            DataSourceId.DEFAULT_OPEN_VALUES_ID,
            DataSourceId.DEFAULT_HIGH_VALUES_ID,
            DataSourceId.DEFAULT_LOW_VALUES_ID,
            DataSourceId.DEFAULT_CLOSE_VALUES_ID,
            DataSourceId.DEFAULT_VOLUME_VALUES_ID
        )
    }
}
