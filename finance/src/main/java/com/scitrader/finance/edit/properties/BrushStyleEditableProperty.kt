package com.scitrader.finance.edit.properties

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.scichart.drawing.common.BrushStyle
import com.scichart.drawing.common.SolidBrushStyle
import com.scitrader.finance.R
import com.scitrader.finance.edit.EditableViewHolder
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.PropertyState

open class BrushStyleEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: BrushStyle,
    listener: (PropertyId, BrushStyle) -> Unit
) : EditablePropertyBase<BrushStyle>(name, parentName, com.scitrader.finance.edit.annotations.PropertyType.BrushStyleProperty, initialValue, listener) {
    override fun getPropertyState(): PropertyState {
        return PropertyState().apply {
            when(val brush = value) {
                is SolidBrushStyle -> {
                    writeString(VALUE_PROPERTY, SOLID_BRUSH)
                    writeInt(COLOR, brush.color)
                }

                // TODO
                else -> throw UnsupportedOperationException("TODO")
            }

        }
    }

    override fun setPropertyState(propertyValue: PropertyState) {
        propertyValue.readString(VALUE_PROPERTY)?.let { propertyType ->
            when(propertyType) {
                SOLID_BRUSH -> {
                    val color = propertyValue.readInt(COLOR)!!

                    trySetValue(SolidBrushStyle(color))
                }

                // TODO
                else -> throw UnsupportedOperationException("TODO")
            }
        }
    }

    companion object {
        const val SOLID_BRUSH = "Solid"
        const val COLOR = "color"
    }
}

class BrushStyleEditablePropertyViewHolder(view: View) : EditableViewHolder(view) {
    private val title = view.findViewById<TextView>(R.id.title)
    private val colorPicker = view.findViewById<PropertyColorPicker>(R.id.propertyColorPicker)

    private var property : BrushStyleEditableProperty? = null

    init {
        colorPicker.selectColorListener = { color ->
            property?.let{ p ->
                val result = p.trySetValue(SolidBrushStyle(color))

                if (result is Result.Fail) {
                    showErrorResultToast(result)
                }
            }
        }
    }

    override fun bindItem(item: IEditable) {
        if (item is BrushStyleEditableProperty) {
            property = item
            title.text = colorPicker.context.getText(item.name)

            colorPicker.setColor(item.value.color)
        } else
            property =null
    }
}
