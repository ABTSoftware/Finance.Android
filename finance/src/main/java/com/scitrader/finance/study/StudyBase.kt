package com.scitrader.finance.study

import com.scichart.core.IServiceContainer
import com.scichart.core.framework.IAttachable
import com.scichart.core.observable.CollectionChangedEventArgs
import com.scichart.core.utility.Guard
import com.scitrader.finance.ISciFinanceChart
import com.scitrader.finance.core.event.*
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.edit.properties.PropertyId
import com.scitrader.finance.indicators.IndicatorCollection
import com.scitrader.finance.pane.IPane
import com.scitrader.finance.pane.IPanePlaceable
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.axes.FinanceAxisCollection
import com.scitrader.finance.pane.series.FinanceSeriesCollection
import com.scitrader.finance.state.EditablePropertyState
import com.scitrader.finance.state.IEditablePropertyContainer
import com.scitrader.finance.utils.attachTo
import com.scitrader.finance.utils.detach
import java.util.*

abstract class StudyBase(final override val id: StudyId, final override val pane: PaneId) : com.scitrader.finance.core.AttachableBase(), IStudy,
    IFinanceChartEventListener {
    protected var financeChart: ISciFinanceChart? = null

    val financeSeries = FinanceSeriesCollection()
    val financeYAxes = FinanceAxisCollection()
    val indicators = IndicatorCollection()

    private val studyChangeListeners = ArrayList<IStudyEventListener>()

    init {
        services.registerService(IStudy::class.java, this)

        financeSeries.addObserver { _, args -> onObservableCollectionChanged(args) }
        financeYAxes.addObserver { _, args -> onObservableCollectionChanged(args) }
        indicators.addObserver { _, args -> onObservableCollectionChanged(args) }
    }

    private fun onObservableCollectionChanged(args: CollectionChangedEventArgs<out IAttachable>) {
        if (isAttached) {
            args.apply {
                oldItems.detach()
                newItems.attachTo(services)
            }
        }
    }

    override fun attachTo(services: IServiceContainer?) {
        super.attachTo(services)

        this.services.let {
            indicators.attachTo(it)
            financeSeries.attachTo(it)
            financeYAxes.attachTo(it)
        }

        financeChart = this.services.getService(ISciFinanceChart::class.java)
        financeChart?.addListener(this)
    }

    override fun detach() {
        financeSeries.detach()
        financeYAxes.detach()
        // Indicators should be detached after financeSeries
        indicators.detach()

        financeChart?.removeListener(this)
        financeChart = null

        super.detach()
    }

    override fun placeInto(pane: IPane) {
        placeInto(pane, financeYAxes)
        placeInto(pane, financeSeries)
    }

    private fun placeInto(pane: IPane, items: List<IPanePlaceable>) {
        for (placeable in items) {
            placeable.placeInto(pane)
        }
    }

    override fun removeFrom(pane: IPane) {
        removeFrom(pane, financeYAxes)
        removeFrom(pane, financeSeries)
    }

    private fun removeFrom(pane: IPane, items: List<IPanePlaceable>) {
        for (placeable in items) {
            placeable.removeFrom(pane)
        }
    }

    override fun savePropertyStateTo(state: EditablePropertyState) {
        savePropertyStateTo(state, financeYAxes)
        savePropertyStateTo(state, financeSeries)
        savePropertyStateTo(state, indicators)
    }

    private fun savePropertyStateTo(state: EditablePropertyState, items: List<IEditablePropertyContainer>) {
        for (item in items) {
            item.savePropertyStateTo(state)
        }

    }

    override fun restorePropertyStateFrom(state: EditablePropertyState) {
        restorePropertyStateFrom(state, financeYAxes)
        restorePropertyStateFrom(state, financeSeries)
        restorePropertyStateFrom(state, indicators)
    }

    private fun restorePropertyStateFrom(state: EditablePropertyState, items: List<IEditablePropertyContainer>) {
        for (item in items) {
            item.restorePropertyStateFrom(state)
        }
    }

    fun invalidateStudy() {
        financeChart?.onStudyChanged(pane, id)
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return true
    }

    override fun onPropertyChanged(propertyId: PropertyId) {
        dispatchStudyEvent(StudyChangedEvent(this))
    }

    override fun addListener(listener: IStudyEventListener) {
        synchronized(this) {
            Guard.notNull(listener, "listener")
            if (!studyChangeListeners.contains(listener)) studyChangeListeners.add(listener)
        }
    }

    override fun removeListener(listener: IStudyEventListener) {
        synchronized(this) {
            studyChangeListeners.remove(listener)
        }
    }

    override fun dispatchStudyEvent(event: IStudyEvent) {
        synchronized(studyChangeListeners) {
            for (listener in studyChangeListeners) {
                listener.onEvent(event)
            }
        }
    }

    override fun onEvent(event: IFinanceChartEvent) {}
}
