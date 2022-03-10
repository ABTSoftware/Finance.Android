package com.scitrader.finance.edit.properties

import androidx.annotation.StringRes

class PositiveDoubleEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: Double,
    listener: (PropertyId, Double) -> Unit
) : DoubleEditableProperty(name, parentName, initialValue, listener) {
    override fun isValidValue(value: Double): Result {
        return if (value > 0) {
            Result.Success
        } else {
            Result.Fail("Expected value greater then 0")
        }
    }
}
