package com.scitrader.finance.pane

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.scichart.charting.modifiers.AxisDragModifierBase
import com.scichart.charting.visuals.ISciChartSurface
import com.scichart.charting.visuals.SciChartSurface
import com.scichart.charting.visuals.axes.IndexDateAxis
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.layout.CanvasLayout
import com.scichart.data.model.DateRange
import com.scichart.data.model.IRangeChangeObserver
import com.scitrader.finance.ISciFinanceChart
import com.scitrader.finance.core.event.FinanceChartWidthChangedEvent
import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.core.event.ReloadChartEvent
import com.scitrader.finance.core.ui.ResizeableStackLayout
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.pane.modifiers.legend.StudyLegend
import com.scitrader.finance.pane.modifiers.yAxisDrag.FinanceYAxisDragModifier
import com.scitrader.finance.utils.toDip
import java.util.*

class MainPane(
    override val chart: SciChartSurface,
    override val xAxis: IAxis,
    override val paneId: PaneId,
    studyLegend: StudyLegend,
    expandButton: View,
    private var xRangeButton: View,
    yAutoRangeButton: AppCompatButton,
    sciChartLogo: View,
    modifiers: DefaultChartModifiers,
    layoutParams: ResizeableStackLayout.LayoutParams
) : DefaultPane(chart, xAxis, paneId, studyLegend, expandButton, sciChartLogo, modifiers, layoutParams), IMainPane, IRangeChangeObserver<Date> {

    private val bottomButtonsContainer: LinearLayout = LinearLayout(chart.context).apply {
        this.layoutParams = CanvasLayout.LayoutParams(
            CanvasLayout.LayoutParams.WRAP_CONTENT,
            CanvasLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val padding = 5f.toDip().toInt()
            setBottom(padding)
            setRightWithAlignment(padding, CanvasLayout.LayoutParams.HORIZONTAL_ALIGNMENT_RIGHT)
        }
            orientation = LinearLayout.VERTICAL

        addView(xRangeButton)
        addView(yAutoRangeButton)
    }

    private val yAxisDragModifier: FinanceYAxisDragModifier = FinanceYAxisDragModifier(yAutoRangeButton).apply {
        dragMode = AxisDragModifierBase.AxisDragMode.Scale
        receiveHandledEvents = true
    }

    private var cachedAxisViewportDimension: Int = 0

    init {
        modifiers.modifierGroup.childModifiers.add(yAxisDragModifier)

        xRangeButton.setOnClickListener {
            (chart.xAxes.firstOrNull() as? IndexDateAxis)?.let { xAxis ->
                val currentRangeDiff = xAxis.visibleRange.diff.time.toDouble()
                val xAxisOffset = currentRangeDiff / 5
                val max = System.currentTimeMillis() + xAxisOffset
                val min = max - currentRangeDiff

                val newRange = DateRange().apply {
                    setMinMaxDouble(min, max)
                }
                xAxis.animateVisibleRangeTo(newRange, 200)
            }
        }
    }

    override var isYAutoRangeButtonEnabled: Boolean
        get() = yAxisDragModifier.isYAutoRangeEnabled
        set(value) {
            yAxisDragModifier.isYAutoRangeEnabled = value
        }

    override fun placeInto(financeChart: ISciFinanceChart) {
        super.placeInto(financeChart)

        financeChart.sharedXRange.addRangeChangeObserver(this)

        with(chart.modifierSurface){
            safeAdd(bottomButtonsContainer)
        }

        xRangeButton.visibility = View.GONE
        setXRangeButtonVisibility(financeChart.sharedXRange.max)

        chart.setRenderedListener { surface, _, _, _ ->
            (surface as? ISciChartSurface)?.xAxes?.firstOrNull()?.let {
                val axisViewportDimension = it.axisViewportDimension

                if (cachedAxisViewportDimension != axisViewportDimension) {
                    financeChart.dispatchFinanceChartEvent(FinanceChartWidthChangedEvent(axisViewportDimension))
                    cachedAxisViewportDimension = axisViewportDimension
                }
            }
        }
    }

    override fun removeFrom(financeChart: ISciFinanceChart) {
        super.removeFrom(financeChart)

        with(chart.modifierSurface) {
            safeRemove(bottomButtonsContainer)
        }

        financeChart.sharedXRange.removeRangeChangeObserver(this)

        chart.setRenderedListener(null)
    }

    fun excludeAutoRangeAxisId(axisId: AxisId) {
        yAxisDragModifier.excludeAxisIds.add(axisId)
    }

    fun removeExcludedAutoRangeAxisId(axisId: AxisId) {
        yAxisDragModifier.excludeAxisIds.remove(axisId)
    }

    override fun onEvent(event: IFinanceChartEvent) {
        super.onEvent(event)

        if (event is ReloadChartEvent) {
            yAxisDragModifier.isYAutoRangeEnabled = true
        }
    }

    override fun onRangeChanged(oldMin: Date?, oldMax: Date?, newMin: Date?, newMax: Date?, changedProperty: Int) {
        newMax?.let {
            setXRangeButtonVisibility(it)
        }
    }

    private fun setXRangeButtonVisibility(maxRange: Date) {
        xRangeButtonIsVisible = maxRange.time < System.currentTimeMillis()
    }

    private var xRangeButtonIsVisible: Boolean = false
    set(value) {
        if (field == value) return

        field = value

        if (field) {
            xRangeButton.visibility = View.VISIBLE
            xRangeButton.alpha = 0f
            xRangeButton
                .animate()
                .alpha(1f)
                .setListener(null)
        } else {
            xRangeButton
                .animate()
                .alpha(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        xRangeButton.visibility = View.GONE
                    }
                })
        }
    }
}
