package com.scitrader.finance.state

import androidx.annotation.StringRes
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.edit.properties.IEditableProperty

class EditablePropertyState : HashMap<Int, PropertyState?> {

    constructor(m: Map<out Int, PropertyState?>) : super(m)
    constructor() : super()

    fun savePropertyValue(@StringRes parentName: Int, @StringRes propertyName: Int, value: PropertyState?) {
        val propertyId = propertyHash(parentName, propertyName)
        this[propertyId] = value
    }

    fun savePropertyValue(@StringRes parentName: Int, property: IEditableProperty<*>) {
        savePropertyValue(parentName, property.name, property.getPropertyState())
    }

    fun savePropertyValues(editable: IEditable) {
        editable::class.java.methods.forEach { it ->
            val annotation = it.getAnnotation(com.scitrader.finance.edit.annotations.EditableProperty::class.java)
            if (annotation != null) {
                val childItem = it.invoke(editable) as? IEditable

                if (childItem is IEditableProperty<*>) {
                    savePropertyValue(editable.name, childItem)
                }
            }
        }
    }

    fun tryGetPropertyValue(@StringRes parentName: Int, @StringRes propertyName: Int): PropertyState?{
        val propertyId = propertyHash(parentName, propertyName)
        return this[propertyId]
    }

    fun tryRestorePropertyValue(@StringRes parentName: Int, property: IEditableProperty<*>) {
        tryGetPropertyValue(parentName, property.name)?.let {
            property.setPropertyState(it)
        }
    }

    fun tryRestorePropertyValues(editable: IEditable) {
        editable::class.java.methods.forEach { it ->
            val annotation = it.getAnnotation(com.scitrader.finance.edit.annotations.EditableProperty::class.java)
            if (annotation != null) {
                val childItem = it.invoke(editable) as? IEditable

                if (childItem is IEditableProperty<*>) {
                    tryRestorePropertyValue(editable.name, childItem)
                }
            }
        }
    }

    private fun propertyHash(@StringRes parentName: Int, @StringRes propertyName: Int) : Int {
        return 31 * parentName + propertyName
    }
}
