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

open class StringEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: String,
    listener: (PropertyId, String) -> Unit
) : EditablePropertyBase<String>(name, parentName, com.scitrader.finance.edit.annotations.PropertyType.StringProperty, initialValue, listener) {
    override fun getPropertyState(): PropertyState {
        return PropertyState().apply {
            writeString(VALUE_PROPERTY, value)
        }
    }

    override fun setPropertyState(propertyValue: PropertyState) {
        propertyValue.readString(VALUE_PROPERTY)?.let {
            trySetValue(it)
        }
    }
}

class StringEditablePropertyViewHolder(view: View) : EditableViewHolder(view) {
    private val titleView: TextView = view.findViewById(R.id.title)
    private val valueView: FinanceEditText = view.findViewById(R.id.value)

    private var property: StringEditableProperty? = null

    init {
        valueView.setTextOnFocusChangeListener { hasFocus, text ->
            if (!hasFocus) {
                val result = property?.trySetValue(text)

                if (result is Result.Fail) {
                    showErrorResultToast(result)
                }
            }

            updateValueView()
        }
    }

    override fun bindItem(item: IEditable) {
        if (item is StringEditableProperty) {
            property = item

            titleView.text = titleView.context.getText(item.name)

            valueView.inputType = InputType.TYPE_CLASS_TEXT
            updateValueView()
        } else
            property = null
    }

    private fun updateValueView() {
        valueView.text = SpannableStringBuilder().append(property?.value.toString())
    }
}
