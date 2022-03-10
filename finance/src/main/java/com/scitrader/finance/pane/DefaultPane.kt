package com.scitrader.finance.pane

import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import com.scichart.charting.modifiers.ModifierGroup
import com.scichart.charting.visuals.SciChartSurface
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.core.utility.touch.IMotionEventManager
import com.scitrader.finance.ISciFinanceChart
import com.scitrader.finance.R
import com.scitrader.finance.core.event.FinanceChartAnimateRangeEvent
import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.core.event.IFinanceChartEventListener
import com.scitrader.finance.core.ui.ResizeableStackLayout
import com.scitrader.finance.pane.modifiers.FinancePinchZoomModifier
import com.scitrader.finance.pane.modifiers.FinanceZoomPanModifier
import com.scitrader.finance.pane.modifiers.crosshair.CrosshairModifier
import com.scitrader.finance.pane.modifiers.legend.StudyLegend
import com.scitrader.finance.state.PropertyState
import com.scitrader.finance.study.IStudy
import com.scitrader.finance.study.StudyId
import java.util.concurrent.atomic.AtomicInteger

open class DefaultPane(
    override val chart: SciChartSurface,
    override val xAxis: IAxis,
    override val paneId: PaneId,
    private val studyLegend: StudyLegend,
    private val expandButton: View,
    private val sciChartLogo: View,
    private val modifiers: DefaultChartModifiers,
    override val layoutParams: ResizeableStackLayout.LayoutParams
) : IPane, IFinanceChartEventListener  {
    private val studiesCounter = AtomicInteger(0)

    override val rootView: View
        get() = chart

    override var isCursorEnabled: Boolean
        get() = modifiers.isCursorEnabled
        set(value) {
            modifiers.isCursorEnabled = value
            studyLegend.showSeriesTooltips = value
        }

    override var chartTheme: Int
        get() = chart.theme
        set(value) { chart.theme = value }

    override var isXAxisVisible: Boolean
        get() = xAxis.visibility == View.VISIBLE
        set(value) {
            xAxis.visibility = if(value) View.VISIBLE else View.GONE
            updateLogoVisibility(value)
        }

    override var isExpandButtonEnabled: Boolean
        get() = expandButton.isEnabled
        set(value) {
            expandButton.isEnabled = value
            expandButton.alpha = if (value) 1F else 0.3F
        }

    private fun updateLogoVisibility(isXAxisVisible: Boolean) {
        if (isXAxisVisible) {
            placeLogo()
        } else {
            removeLogo()
        }
    }

    private fun placeLogo() {
        with(chart.modifierSurface) {
            safeAdd(sciChartLogo)
        }
    }

    private fun removeLogo() {
        with(chart.modifierSurface) {
            safeRemove(sciChartLogo)
        }
    }

    override fun placeInto(financeChart: ISciFinanceChart) {
        financeChart.addListener(this)

        financeChart.addPane(this)

        // set finance chart as source of touch events for modifier group
        modifiers.modifierGroup.let { modifierGroup ->
            chart.services.getService(IMotionEventManager::class.java)?.unsubscribe(modifierGroup)
            financeChart.services.getService(IMotionEventManager::class.java)?.subscribe(financeChart, modifierGroup)
        }

        expandButton.setOnClickListener {
            val isExpanded = financeChart.toggleFullscreenOnPane(paneId)

            (expandButton as? AppCompatImageButton)?.let {
                it.setImageResource(if (isExpanded) R.drawable.collapse_chart else R.drawable.expand_chart)
            }
        }
    }

    override fun removeFrom(financeChart: ISciFinanceChart) {
        expandButton.setOnClickListener(null)

        removeLogo()

        // remove finance chart subscription for modifier group
        modifiers.modifierGroup.let { modifierGroup ->
            financeChart.services.getService(IMotionEventManager::class.java)?.unsubscribe(modifierGroup)
        }

        financeChart.removeListener(this)

        financeChart.removePane(this)
    }

    override fun addStudy(study: IStudy) {
        studiesCounter.incrementAndGet()
        study.placeInto(this)

        val studyTooltip = study.getStudyTooltip(chart.context)

        studyLegend.addTooltip(studyTooltip)
    }

    override fun removeStudy(study: IStudy) {
        study.removeFrom(this)
        studiesCounter.decrementAndGet()

        studyLegend.removeTooltip(study.id)
    }

    override val hasStudies: Boolean
        get() = studiesCounter.get() > 0

    override fun onStudyChanged(studyId: StudyId) {
        studyLegend.onStudyChanged(studyId)
    }

    override fun onExpandAnimationStart() {}

    override fun onExpandAnimationFinish() {
        modifiers.crosshairModifier.normalizePointToDraw()
    }

    override fun savePropertyStateTo(chartState: PropertyState, paneState: PropertyState) {
        paneState.writeString(paneLayoutParamsKey, ResizeableStackLayout.LayoutParams.convertToJsonString(layoutParams))

        modifiers.crosshairModifier.savePropertyStateTo(chartState, paneState)
    }

    override fun restorePropertyStateFrom(chartState: PropertyState, paneState: PropertyState) {
        paneState.readString(paneLayoutParamsKey)?.let {
            ResizeableStackLayout.LayoutParams.assignLayoutParamsFromJsonString(layoutParams, it)
        }

        modifiers.crosshairModifier.restorePropertyStateFrom(chartState, paneState)
    }

    override fun onEvent(event: IFinanceChartEvent) {
        if (event is FinanceChartAnimateRangeEvent) {
            chart.xAxes.firstOrNull()?.let {
                it.animateVisibleRangeTo(event.range, 200)
            }
        }
    }

    data class DefaultChartModifiers(val modifierGroup: ModifierGroup, val crosshairModifier: CrosshairModifier, val pinchZoomModifier: FinancePinchZoomModifier, val zoomPanModifier: FinanceZoomPanModifier) {
        var isCursorEnabled: Boolean
            get() = crosshairModifier.isEnabled
            set(value) {
                crosshairModifier.isEnabled = value

                zoomPanModifier.isEnabled = !value
            }
    }

    companion object {
        internal const val paneLayoutParamsKey = "paneLayoutParamsKey"
    }
}
