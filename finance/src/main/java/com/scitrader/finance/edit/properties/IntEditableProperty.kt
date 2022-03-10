package com.scitrader.finance.edit.properties

import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.scitrader.finance.R
import com.scitrader.finance.edit.EditableViewHolder
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.PropertyState

open class IntEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: Int,
    listener: (PropertyId, Int) -> Unit
) : EditablePropertyBase<Int>(name, parentName, com.scitrader.finance.edit.annotations.PropertyType.IntegerProperty, initialValue, listener) {
    override fun getPropertyState(): PropertyState {
        return PropertyState().apply {
            writeInt(VALUE_PROPERTY, value)
        }
    }

    override fun setPropertyState(propertyValue: PropertyState) {
        propertyValue.readInt(VALUE_PROPERTY)?.let {
            trySetValue(it)
        }
    }
}

class IntEditablePropertyViewHolder(view: View) : EditableViewHolder(view) {
    private val titleView: TextView = view.findViewById(R.id.title)
    private val valueView: FinanceEditText = view.findViewById(R.id.value)

    private var property: IntEditableProperty? = null

    init {
        valueView.setTextOnFocusChangeListener() { hasFocus, text ->
            if (!hasFocus) {
                text.toIntOrNull()?.let {
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
        if (item is IntEditableProperty) {
            property = item
            titleView.text = titleView.context.getText(item.name)

            valueView.inputType = InputType.TYPE_CLASS_NUMBER
            updateValueView()
        } else
            property = null

    }

    private fun updateValueView() {
        valueView.text = SpannableStringBuilder().append(property?.value.toString())
    }
}
