package com.scitrader.finance.state

interface IEditablePropertyContainer {
    fun savePropertyStateTo(state: EditablePropertyState)
    fun restorePropertyStateFrom(state: EditablePropertyState)
}
