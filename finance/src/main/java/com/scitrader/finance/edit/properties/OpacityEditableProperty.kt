package com.scitrader.finance.edit.properties

import androidx.annotation.StringRes

class OpacityEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: Float,
    listener: (PropertyId, Float) -> Unit
) : FloatEditableProperty(name, parentName, initialValue, listener) {
    override fun isValidValue(value: Float): Result {
        val min = 0f
        val max = 1f

        return if (value in min..max) {
            Result.Success
        } else {
            Result.Fail("Expected value between $min and $max")
        }
    }
}
