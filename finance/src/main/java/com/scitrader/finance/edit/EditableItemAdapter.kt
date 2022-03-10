package com.scitrader.finance.edit

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scitrader.finance.edit.properties.IEditableProperty
import com.scitrader.finance.study.IStudy

class EditableItemAdapter(private val editableList: List<IEditable>, private val viewHolderFactory: IEditableItemViewHolderFactory) : RecyclerView.Adapter<EditableViewHolder>() {
    override fun getItemCount(): Int {
        return editableList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditableViewHolder {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: EditableViewHolder, position: Int) {
        holder.bindItem(editableList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return editableList[position].viewType
    }

    companion object {
        fun IStudy.toEditableItemsAdapter(
            viewType: Int? = null,
            factory: IEditableItemViewHolderFactory = DefaultEditableItemViewHolderFactory()
        ) : EditableItemAdapter {
            val editableList = if (viewType != null) {
                val viewTypeList = getStudyEditableList(this, viewType)

                val newList = arrayListOf<IEditable>()
                viewTypeList.forEach {
                    if (viewTypeList.size > 1) {
                        newList.add(it)
                    }
                    newList.addAll(getEditableList(it, this))
                }
                newList
            } else {
                getEditableList(this, this)
            }

            return EditableItemAdapter(editableList, factory)
        }

        private fun getStudyEditableList(study: IStudy, viewType: Int) : List<IEditable> {
            val editableItems = mutableListOf<IEditable>()

            study::class.java.methods.forEach { it ->
                val annotation = it.getAnnotation(com.scitrader.finance.edit.annotations.EditableProperty::class.java)
                if (annotation != null) {
                    val childItem = it.invoke(study) as? IEditable

                    if (childItem != null && childItem.viewType == viewType && study.isValidEditableForSettings(childItem)) {
                        editableItems.add(childItem)
                    }
                }
            }

            return editableItems
        }

        private fun getEditableList(item: Any, study: IStudy) : List<IEditable> {
            val editableProperties = ArrayList<IEditable>()

            getEditablePropertiesRecursive(item, study, editableProperties)

            return editableProperties
        }

        private fun getEditablePropertiesRecursive(
            item: Any,
            study: IStudy,
            editableItems: ArrayList<IEditable>
        ) {
            item::class.java.methods.forEach { it ->
                val annotation = it.getAnnotation(com.scitrader.finance.edit.annotations.EditableProperty::class.java)
                if (annotation != null) {
                    val childItem = it.invoke(item) as? IEditable

                    if (childItem != null && study.isValidEditableForSettings(childItem)) {
                        editableItems.add(childItem)
                        if(childItem !is IEditableProperty<*>)
                            getEditablePropertiesRecursive(childItem, study, editableItems)
                    }
                }
            }
        }
    }
}

interface IEditableItemViewHolderFactory {
    fun createViewHolder(parent: ViewGroup, viewType: Int) : EditableViewHolder
}
