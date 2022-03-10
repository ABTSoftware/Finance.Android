package com.scitrader.finance.edit.properties

import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.PropertyState

interface IEditableProperty<T> : IEditable {
    val value: T

    val propertyId : PropertyId

    fun isValidValue(value: T): Result
    fun trySetValue(value: T) : Result
    fun getPropertyState() : PropertyState
    fun setPropertyState(propertyValue: PropertyState)
}

data class PropertyId(val entityId: Int, val propertyName: Int)
