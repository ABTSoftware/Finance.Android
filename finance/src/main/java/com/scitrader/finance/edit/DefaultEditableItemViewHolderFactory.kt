package com.scitrader.finance.edit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.scitrader.finance.R
import com.scitrader.finance.edit.properties.*

class DefaultEditableItemViewHolderFactory : IEditableItemViewHolderFactory {
    override fun createViewHolder(parent: ViewGroup, viewType: Int): EditableViewHolder {
        return when(viewType) {
            com.scitrader.finance.edit.annotations.PropertyType.FinanceSeries, com.scitrader.finance.edit.annotations.PropertyType.Indicator, com.scitrader.finance.edit.annotations.PropertyType.YAxis -> HeaderViewHolder(inflate(parent, R.layout.header_item))

            com.scitrader.finance.edit.annotations.PropertyType.PenStyleProperty -> PenStyleEditablePropertyViewHolder(inflate(parent, R.layout.pen_style_property_item))
            com.scitrader.finance.edit.annotations.PropertyType.BrushStyleProperty -> BrushStyleEditablePropertyViewHolder(inflate(parent, R.layout.brush_style_property_item))

            com.scitrader.finance.edit.annotations.PropertyType.IntegerProperty -> IntEditablePropertyViewHolder(inflate(parent, R.layout.number_property_item))
            com.scitrader.finance.edit.annotations.PropertyType.FloatProperty -> FloatEditablePropertyViewHolder(inflate(parent, R.layout.number_property_item))
            com.scitrader.finance.edit.annotations.PropertyType.DoubleProperty -> DoubleEditablePropertyViewHolder(inflate(parent, R.layout.number_property_item))

            com.scitrader.finance.edit.annotations.PropertyType.StringProperty -> StringEditablePropertyViewHolder(inflate(parent, R.layout.string_property_item))

            com.scitrader.finance.edit.annotations.PropertyType.DataSourceIdProperty -> DataSourcePropertyViewHolder(inflate(parent, R.layout.spinner_property_item))

            com.scitrader.finance.edit.annotations.PropertyType.EnumProperty -> EnumEditablePropertyViewHolder(inflate(parent, R.layout.spinner_property_item))

            else -> throw UnsupportedOperationException()
        }
    }

    private inline fun inflate(parent: ViewGroup, @LayoutRes layoutId: Int): View {
        return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    }
}
