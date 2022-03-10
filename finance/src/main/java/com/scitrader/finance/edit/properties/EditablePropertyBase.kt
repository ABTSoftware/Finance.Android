package com.scitrader.finance.edit.properties

import androidx.annotation.StringRes

abstract class EditablePropertyBase<T>(
    @StringRes final override val name: Int,
    @StringRes parentName: Int,
    override val viewType: Int,
    protected var initialValue: T,
    private val listener: (PropertyId, T) -> Unit
) : IEditableProperty<T> {

    override val propertyId: PropertyId = PropertyId(parentName, name)

    final override var value: T = initialValue
        private set(value) {
            if (field == value) return

            field = value
            listener(propertyId, value)
        }

    init {
        listener(propertyId, value)
    }

    override fun trySetValue(value: T): Result {
        val result = isValidValue(value)
        if (result == Result.Success) {
            this.value = value
        }

        return result
    }

    fun updateInitialValue(value: T) {
        initialValue = value
        this.value = value
    }

    override fun reset() {
        value = initialValue
    }

    override fun toString(): String {
        return "$value"
    }

    companion object {
        const val VALUE_PROPERTY = "value"
    }

    override fun isValidValue(value: T): Result {
        return Result.Success
    }
}

sealed class Result {
    data class Fail(val error: String): Result()
    object Success: Result()
}
