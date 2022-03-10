package com.scitrader.finance.edit.properties

import androidx.annotation.StringRes

class FastSlowPeriodEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: Int,
    listener: (PropertyId, Int) -> Unit,
) : IntEditableProperty(name, parentName, initialValue, listener) {
    override fun isValidValue(value: Int): Result {
        val min = 1
        val max = 100000

        return if (value in min..max) {
            Result.Success
        } else {
            Result.Fail("Expected value between $min and $max")
        }
    }
}
