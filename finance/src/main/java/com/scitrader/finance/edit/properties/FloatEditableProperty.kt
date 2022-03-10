package com.scitrader.finance.edit.properties

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.scitrader.finance.R
import com.scitrader.finance.edit.EditableViewHolder
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.PropertyState
import com.scitrader.finance.utils.decimalInputType

open class FloatEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: Float,
    listener: (PropertyId, Float) -> Unit
) : EditablePropertyBase<Float>(
    name,
    parentName,
    com.scitrader.finance.edit.annotations.PropertyType.FloatProperty,
    initialValue,
    listener
) {
    override fun getPropertyState(): PropertyState {
        return PropertyState().apply {
            writeFloat(VALUE_PROPERTY, value)
        }
    }

    override fun setPropertyState(propertyValue: PropertyState) {
        propertyValue.readFloat(VALUE_PROPERTY)?.let {
            trySetValue(it)
        }
    }
}

class FloatEditablePropertyViewHolder(view: View) : EditableViewHolder(view) {
    private val titleView: TextView = view.findViewById(R.id.title)
    private val valueView: FinanceEditText = view.findViewById(R.id.value)

    private var property: FloatEditableProperty? = null

    init {
        valueView.setTextOnFocusChangeListener() { hasFocus, text ->
            if (!hasFocus) {
                text.toFloatOrNull()?.let {
                    val result = property?.trySetValue(it)

                    if (result is Result.Fail) {
                        showErrorResultToast(result)
                    }
                } ?: showErrorResultToast(Result.Fail("Invalid input"))

                updateValueView()
            }
        }
    }

    override fun bindItem(item: IEditable) {
        if (item is FloatEditableProperty) {
            property = item

            titleView.text = titleView.context.getText(item.name)

            valueView.inputType = decimalInputType
            updateValueView()
        } else
            property = null
    }

    private fun updateValueView() {
        valueView.text = SpannableStringBuilder().append(property?.value.toString())
    }
}
