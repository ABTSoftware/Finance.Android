package com.scitrader.finance.study

import android.content.Context
import com.scichart.core.IServiceProvider
import com.scichart.core.framework.IAttachable
import com.scitrader.finance.core.event.IStudyEvent
import com.scitrader.finance.core.event.IStudyEventListener
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.edit.properties.PropertyId
import com.scitrader.finance.pane.IPanePlaceable
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.state.IEditablePropertyContainer
import com.scitrader.finance.study.tooltips.IStudyTooltip

interface IStudy : IPanePlaceable, IServiceProvider, IAttachable, IEditablePropertyContainer {
    val title: CharSequence
    val pane: PaneId
    val id: StudyId

    fun getStudyTooltip(context: Context): IStudyTooltip
    fun reset()

    fun isValidEditableForSettings(editable: IEditable) : Boolean

    fun onPropertyChanged(propertyId: PropertyId)

    fun addListener(listener: IStudyEventListener)
    fun removeListener(listener: IStudyEventListener)
    fun dispatchStudyEvent(event: IStudyEvent)
}
