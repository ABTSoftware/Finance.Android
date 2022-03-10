package com.scitrader.finance.edit.properties

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.StringRes
import com.scitrader.finance.R
import com.scitrader.finance.edit.EditableViewHolder
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.PropertyState

open class EnumEditableProperty<T : Enum<T>>(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: T,
    listener: (PropertyId, T) -> Unit
) : EditablePropertyBase<T>(name, parentName, com.scitrader.finance.edit.annotations.PropertyType.EnumProperty, initialValue, listener) {
        fun enumValues() : Array<T> {
            return initialValue.javaClass.enumConstants
        }

    fun setValue(value: Any?) {
        enumValues().firstOrNull { it == value }?.let {
            trySetValue(it)
        }
    }

    override fun getPropertyState(): PropertyState {
        return PropertyState().apply {
            writeString(VALUE_PROPERTY, value.toString())
        }
    }

    override fun setPropertyState(propertyValue: PropertyState) {
        propertyValue.readString(VALUE_PROPERTY)?.let { enumString ->
            enumValues().firstOrNull { it.toString() == enumString }?.let {
                trySetValue(it)
            }
        }
    }
}

class EnumEditablePropertyViewHolder(view: View) : EditableViewHolder(view) {
    private val titleView: TextView = view.findViewById(R.id.title)
    private val optionsPicker: Spinner = view.findViewById(R.id.optionsPicker)

    private var property: EnumEditableProperty<*>? = null

    init {
        optionsPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.let{
                    property?.setValue(parent.getItemAtPosition(position))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    override fun bindItem(item: IEditable) {
        if (item is EnumEditableProperty<*>) {
            property = item

            titleView.text = titleView.context.getText(item.name)
            val items = item.enumValues()
            optionsPicker.adapter = ArrayAdapter(optionsPicker.context,R.layout.text_spinner_item, R.id.value, items)
            optionsPicker.setSelection(items.indexOf(item.value))
        } else
            property = null
    }
}
