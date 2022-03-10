package com.scitrader.finance.edit

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.scitrader.finance.edit.properties.Result

abstract class EditableViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bindItem(item: IEditable)

    fun showErrorResultToast(result: Result.Fail) {
        Toast.makeText(itemView.context, result.error, Toast.LENGTH_LONG).show()
    }
}
