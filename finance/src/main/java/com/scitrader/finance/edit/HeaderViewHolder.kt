package com.scitrader.finance.edit

import android.view.View
import android.widget.TextView
import com.scitrader.finance.R

class HeaderViewHolder(view: View) : EditableViewHolder(view) {
    private val header: TextView = view.findViewById(R.id.itemHeader)

    override fun bindItem(item: IEditable) {
        header.text = header.context.getText(item.name)
    }
}
