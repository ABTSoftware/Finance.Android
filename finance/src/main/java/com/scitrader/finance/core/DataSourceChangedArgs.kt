package com.scitrader.finance.core

data class DataSourceChangedArgs(val changedDataSourceIds: Set<DataSourceId>) {
    constructor(vararg changedDataSourceIds: DataSourceId) : this(changedDataSourceIds.toSet())
}
