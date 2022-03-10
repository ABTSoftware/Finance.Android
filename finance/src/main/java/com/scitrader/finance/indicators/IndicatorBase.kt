package com.scitrader.finance.indicators

import androidx.annotation.StringRes
import com.scitrader.finance.core.DependableBase
import com.scitrader.finance.state.EditablePropertyState
import kotlin.math.max

abstract class IndicatorBase(@StringRes final override val name: Int) : DependableBase(), IIndicator {
    final override val viewType: Int = com.scitrader.finance.edit.annotations.PropertyType.Indicator

    override fun savePropertyStateTo(state: EditablePropertyState) {
        state.savePropertyValues(this)
    }

    override fun restorePropertyStateFrom(state: EditablePropertyState) {
        state.tryRestorePropertyValues(this)
    }

    fun shouldSkipCalculation(lookback: Int, startIndex: Int, endIndex: Int) : Boolean {
        if (lookback < 0) { return true }
        return max(lookback, startIndex) > endIndex
    }
}
