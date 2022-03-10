package com.scitrader.finance.edit.properties

import android.view.View
import androidx.annotation.StringRes
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.EditableViewHolder
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.PropertyState

open class DataSourceEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: DataSourceId,
    listener: (PropertyId, DataSourceId) -> Unit
) : EditablePropertyBase<DataSourceId>(name, parentName, com.scitrader.finance.edit.annotations.PropertyType.DataSourceIdProperty, initialValue, listener) {
    override fun getPropertyState(): PropertyState {
        return PropertyState().apply {
            writeString(VALUE_PROPERTY, value.id)
        }
    }

    override fun setPropertyState(propertyValue: PropertyState) {
        propertyValue.readString(VALUE_PROPERTY)?.let {
            trySetValue(DataSourceId(it))
        }
    }
}

class DataSourcePropertyViewHolder(view: View) : EditableViewHolder(view) {
//    private val titleView: TextView = view.findViewById(R.id.title)
//    private val valueView: Spinner = view.findViewById(R.id.value)

    private var property: DataSourceEditableProperty? = null

    override fun bindItem(item: IEditable) {
        if (item is DataSourceEditableProperty) {
            property = item

//            titleView.text = titleView.context.getText(item.name)
//            valueView. = SpannableStringBuilder().append(item.value.id)
        } else
            property = null
    }
}
