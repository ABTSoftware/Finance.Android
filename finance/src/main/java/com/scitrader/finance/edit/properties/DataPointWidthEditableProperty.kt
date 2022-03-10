package com.scitrader.finance.edit.properties

import androidx.annotation.StringRes

class DataPointWidthEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: Double,
    listener: (PropertyId, Double) -> Unit
) : DoubleEditableProperty(name, parentName, initialValue, listener) {
    override fun isValidValue(value: Double): Result {
        val min = 0.0
        val max = 1.0

        return if (value in min..max) {
            Result.Success
        } else {
            Result.Fail("Expected value between $min and $max")
        }
    }
}
