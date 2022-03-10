package com.scitrader.finance

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import com.scichart.charting.visuals.SciChartSurface
import com.scichart.charting.visuals.synchronization.SciChartVerticalGroup
import com.scichart.core.IServiceContainer
import com.scichart.core.IServiceProvider
import com.scichart.core.ServiceContainer
import com.scichart.core.framework.IContextProvider
import com.scichart.core.framework.IHitTestable
import com.scichart.core.utility.Guard
import com.scichart.core.utility.ViewUtil
import com.scichart.core.utility.touch.IMotionEventDispatcher
import com.scichart.core.utility.touch.IMotionEventManager
import com.scichart.core.utility.touch.IPublishMotionEvents
import com.scichart.core.utility.touch.MotionEventManager
import com.scichart.data.model.DateRange
import com.scitrader.finance.core.IDataManager
import com.scitrader.finance.core.event.FinanceChartLayoutUpdateEvent
import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.core.event.IFinanceChartEventListener
import com.scitrader.finance.core.ui.ResizeableStackLayout
import com.scitrader.finance.data.ICandleDataProvider
import com.scitrader.finance.pane.*
import com.scitrader.finance.state.FinanceChartState
import com.scitrader.finance.state.PropertyState
import com.scitrader.finance.study.IStudy
import com.scitrader.finance.study.StudyCollection
import com.scitrader.finance.study.StudyId
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.set

interface ISciFinanceChart : IServiceProvider, IPublishMotionEvents, IContextProvider, IHitTestable{
    val studies: StudyCollection
    var candleDataProvider: ICandleDataProvider?

    val sharedXRange: DateRange

    fun onStudyChanged(paneId: PaneId, studyId: StudyId)

    /**
     * Register specified [IFinanceChartEventListener] instance as chart listener
     * @param listener The listener instance to add
     */
    fun addListener(listener: IFinanceChartEventListener)

    /**
     * Removes specified [IFinanceChartEventListener] instance from list of chart listeners
     * @param listener The listener instance to remove
     */
    fun removeListener(listener: IFinanceChartEventListener)

    /**
     * Send specified {@code event} to all subscribed [IFinanceChartEventListener] instances
     */
    fun dispatchFinanceChartEvent(event: IFinanceChartEvent)

    fun addPane(pane: IPane)
    fun removePane(pane: IPane)

    fun toggleFullscreenOnPane(paneId: PaneId) : Boolean

    fun saveChartStateTo(state: FinanceChartState)
    fun restoreChartStateFrom(state: FinanceChartState)
}

open class SciFinanceChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ResizeableStackLayout(context, attrs, defStyleAttr), ISciFinanceChart {

    private val dataManager = com.scitrader.finance.core.DataManager()
    private val motionEventDispatchers = ArrayList<IMotionEventDispatcher>()

    private val chartChangeListeners = ArrayList<IFinanceChartEventListener>()

    private val services = ServiceContainer().apply {
        registerService(ISciFinanceChart::class.java, this@SciFinanceChart)
        registerService(IDataManager::class.java, dataManager)
        registerService(Context::class.java, context)
        registerService(IMotionEventManager::class.java, MotionEventManager())
    }

    private val verticalGroup = SciChartVerticalGroup()

    final override val sharedXRange = getDefaultXRange()

    var paneFactory: IPaneFactory = DefaultPaneFactory()
    var hasSpaceForNewPane: Boolean = true

    private var fullscreenPaneId: PaneId? = null

    final override val studies = StudyCollection()

    final override var candleDataProvider : ICandleDataProvider? = null
    set(value) {
        field?.detach()

        field = value

        field?.attachTo(services)
    }

    var minimalZoomConstrain: Long? = null
    set(value) {
        field = value

        for (pane in paneMap) {
            pane.value.xAxis.minimalZoomConstrain = value
        }
    }

    private val paneMap = LinkedHashMap<PaneId, IPane>()

    @StyleRes
    var chartTheme: Int = R.style.SciChart_SciChartv4DarkStyle
        set(value) {
            field = value
            for (pane in paneMap.values) {
                pane.chartTheme = value
            }
        }

    var isCursorEnabled : Boolean = false
    set(value) {
        field = value

        for (pane in paneMap.values) {
            pane.isCursorEnabled = value
        }
    }

    var isYAutoRangeEnabled: Boolean = true
    set(value) {
        field = value

        for (pane in paneMap.values.filterIsInstance<IMainPane>()) {
            pane.isYAutoRangeButtonEnabled = value
        }
    }

    init {
        studies.addObserver { _, args ->
            args.apply {
                for (study in oldItems) {
                    detachStudy(study)
                }

                for (study in newItems) {
                    attachStudy(study)
                }
            }
        }
    }

    private val transitionListener: LayoutTransition.TransitionListener
            get() = object : LayoutTransition.TransitionListener {
        override fun startTransition(
            p0: LayoutTransition?,
            p1: ViewGroup?,
            p2: View?,
            p3: Int
        ) {
            for (pane in paneMap) {
                pane.value.onExpandAnimationStart()
            }
        }

        override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {
            println("endTransition")
            for (pane in paneMap) {
                pane.value.onExpandAnimationFinish()
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (fullscreenPaneId != null) {
            val view = paneMap[fullscreenPaneId]?.rootView

            val height = b - t

            view?.layout(l, 0, r, height)
        } else {
            super.onLayout(changed, l, t, r, b)

            updateHasSpaceForNewPane()
        }
    }

    private fun updateHasSpaceForNewPane() {
        if (childCount > 0) {
            val firstChild = getChildAt(0)
            val hasSpaceForNewPane = (firstChild.height - height * paneFactory.secondaryPaneHeightRatio) > height * paneFactory.mainPaneHeightRatio

            if (this.hasSpaceForNewPane != hasSpaceForNewPane) {
                this.hasSpaceForNewPane = hasSpaceForNewPane
            }
        }
    }

    protected open fun attachStudy(study: IStudy) {
        study.attachTo(services)
        if (isAttachedToWindow)
            attachToLayout(study)
    }

    protected open fun detachStudy(study: IStudy) {
        if (isAttachedToWindow)
            detachFromLayout(study)
        study.detach()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        for (study in studies) {
            attachToLayout(study)
        }

        layoutTransition.addTransitionListener(transitionListener)
    }

    private fun attachToLayout(study: IStudy) {
        val pane = getPaneById(study.pane)

        pane?.addStudy(study)
    }

    override fun onDetachedFromWindow() {
        for (study in studies) {
            detachFromLayout(study)
        }
        layoutTransition.removeTransitionListener(transitionListener)

        super.onDetachedFromWindow()
    }

    private fun detachFromLayout(study: IStudy) {
        paneMap[study.pane]?.let{ pane ->
            pane.removeStudy(study)

            if(!pane.hasStudies) {
                paneMap.remove(study.pane)

                detachPane(pane)
            }
        }
    }

    private fun getPaneById(paneId: PaneId): IPane? {
        return paneMap[paneId] ?: createNewPane(paneId)
    }

    private fun createNewPane(paneId: PaneId): IPane? {
        val newPane = paneFactory.createPane(this, paneId)
        paneMap[paneId] = newPane
        attachPane(newPane)

        return newPane
    }

    private fun attachPane(pane: IPane) {
        pane.xAxis.visibleRange = sharedXRange
        pane.isCursorEnabled = isCursorEnabled

        if(pane is IMainPane)
            pane.isYAutoRangeButtonEnabled = isYAutoRangeEnabled

        verticalGroup.addSurfaceToGroup(pane.chart as SciChartSurface)
        pane.chartTheme = chartTheme

        pane.placeInto(this)

        updateXAxisVisibility()
        updateExpandButtonIsEnabled()

        pane.xAxis.minimalZoomConstrain = minimalZoomConstrain
    }

    private fun detachPane(pane: IPane) {
        pane.removeFrom(this)

        verticalGroup.removeSurfaceFromGroup(pane.chart as SciChartSurface?)
        pane.xAxis.visibleRange = getDefaultXRange()

        updateXAxisVisibility()
        updateExpandButtonIsEnabled()
    }

    private fun getDefaultXRange() : DateRange = DateRange()

    private fun updateXAxisVisibility() {
        // make xAxis in all panes except last one invisible
        val panes = paneMap.values
        val lastIndex = panes.size - 1
        panes.forEachIndexed { index, pane ->
            pane.isXAxisVisible = index == lastIndex
        }
    }

    private fun updateExpandButtonIsEnabled() {
        val isEnabled = paneMap.size > 1
        paneMap.values.forEach { pane ->
            pane.isExpandButtonEnabled = isEnabled
        }
    }

    override fun toggleFullscreenOnPane(paneId: PaneId) : Boolean {
        if(fullscreenPaneId != null) {
            // if there is fullscreen pane then we need to restore all panes,
            // to do this need to make all panes visible and make xAxis visible on bottom pane
            for (entry in paneMap) {
                entry.value.rootView.visibility = View.VISIBLE
            }

            updateXAxisVisibility()

            fullscreenPaneId = null
        } else {
            // make all panes invisible except fullscreen one
            // for fullscreen pane we need to ensure that xAxis is visible
            for (entry in paneMap) {
                val pane = entry.value
                if(entry.key == paneId) {
                    pane.isXAxisVisible = true
                } else {
                    pane.rootView.visibility = View.GONE
                }
            }

            fullscreenPaneId = paneId
        }

        return fullscreenPaneId != null
    }

    final override fun getServices(): IServiceContainer = services

    override fun onStudyChanged(paneId: PaneId, studyId: StudyId) {
        paneMap[paneId]?.onStudyChanged(studyId)
    }

    override fun addListener(listener: IFinanceChartEventListener) {
        synchronized(this) {
            Guard.notNull(listener, "listener")
            if (!chartChangeListeners.contains(listener)) chartChangeListeners.add(listener)
        }
    }

    override fun removeListener(listener: IFinanceChartEventListener) {
        synchronized(this) {
            chartChangeListeners.remove(listener)
        }
    }

    override fun dispatchFinanceChartEvent(event: IFinanceChartEvent) {
        synchronized(chartChangeListeners) {
            for (listener in chartChangeListeners) {
                listener.onEvent(event)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun addMotionEventDispatcher(dispatcher: IMotionEventDispatcher) {
        Guard.notNull(dispatcher, "dispatcher")
        synchronized(motionEventDispatchers) { motionEventDispatchers.add(dispatcher) }
    }

    /**
     * {@inheritDoc}
     */
    override fun removeMotionEventDispatcher(dispatcher: IMotionEventDispatcher) {
        synchronized(motionEventDispatchers) { motionEventDispatchers.remove(dispatcher) }
    }

    /**
     * {@inheritDoc}
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = super.onTouchEvent(event)
        synchronized(motionEventDispatchers) {
            val size = motionEventDispatchers.size
            for (i in 0 until size) {
                val dispatcher = motionEventDispatchers[i]
                result = result or dispatcher.onTouchEvent(event)
            }
        }
        return result
    }

    override fun onChildLayoutParamsUpdate() {
        dispatchFinanceChartEvent(FinanceChartLayoutUpdateEvent(storedLayoutParams))
    }

    private val storedLayoutParams : Map<String, LayoutParams>
        get() {
            val result = mutableMapOf<String, LayoutParams>()
            for (entry in paneMap) {
                result[entry.key.id] = entry.value.layoutParams
            }

            return result
        }

    /**
     * {@inheritDoc}
     */
    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        var result = super.onGenericMotionEvent(event)
        synchronized(motionEventDispatchers) {
            val size = motionEventDispatchers.size
            for (i in 0 until size) {
                val dispatcher = motionEventDispatchers[i]
                result = result or dispatcher.onGenericMotionEvent(event)
            }
        }
        return result
    }

    /**
     * {@inheritDoc}
     */
    final override fun isPointWithinBounds(x: Float, y: Float, hitTestable: IHitTestable): Boolean {
        return ViewUtil.isPointWithinBounds(this, x, y, hitTestable)
    }

    /**
     * {@inheritDoc}
     */
    final override fun isPointWithinBounds(x: Float, y: Float): Boolean {
        return ViewUtil.isPointWithinBounds(this, x, y)
    }

    /**
     * {@inheritDoc}
     */
    final override fun getBoundsRelativeTo(bounds: Rect, hitTestable: IHitTestable): Boolean {
        return ViewUtil.getBoundsRelativeTo(this, hitTestable, bounds)
    }

    /**
     * {@inheritDoc}
     */
    final override fun getBoundsRelativeTo(bounds: RectF, hitTestable: IHitTestable): Boolean {
        return ViewUtil.getBoundsRelativeTo(this, hitTestable, bounds)
    }

    /**
     * {@inheritDoc}
     */
    final override fun translatePoint(point: PointF, hitTestable: IHitTestable): Boolean {
        return ViewUtil.translatePoint(this, point, hitTestable)
    }

    /**
     * {@inheritDoc}
     */
    override fun getView(): View = this

    override fun addPane(pane: IPane) {
        addView(pane.rootView, pane.layoutParams)
    }

    override fun removePane(pane: IPane) {
        removeView(pane.rootView)
    }

    override fun saveChartStateTo(state: FinanceChartState) {
        state.chartState.run {
            writeBool(isCursorEnabledKey, isCursorEnabled)
        }

        for (entry in paneMap) {
            state.paneStates[entry.key.id] = PropertyState().also { paneState ->
                entry.value.savePropertyStateTo(state.chartState, paneState)
            }
        }
    }

    override fun restoreChartStateFrom(state: FinanceChartState) {
        paneFactory = DefaultPaneFactory(state)

        for (entry in paneMap) {
            state.paneStates[entry.key.id]?.let{ paneState ->
                entry.value.restorePropertyStateFrom(state.chartState, paneState)
            }
        }

        state.chartState.run {
            readBool(isCursorEnabledKey)?.let {
                isCursorEnabled = it
            }
        }
    }

    companion object {
        internal const val isCursorEnabledKey = "isCursorEnabled"
    }
}
