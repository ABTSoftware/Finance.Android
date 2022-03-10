package com.scitrader.finance.pane

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.scichart.charting.ClipMode
import com.scichart.charting.ClipModeTarget
import com.scichart.charting.modifiers.ModifierGroup
import com.scichart.charting.visuals.layout.CanvasLayout
import com.scichart.core.utility.ComparableUtil
import com.scichart.data.model.RangeFactory
import com.scitrader.finance.ISciFinanceChart
import com.scitrader.finance.R
import com.scitrader.finance.SciFinanceChart
import com.scitrader.finance.core.event.FinanceChartDoubleTapEvent
import com.scitrader.finance.core.ui.ResizeableStackLayout
import com.scitrader.finance.financeChart.CanvasSciChartSurface
import com.scitrader.finance.pane.axes.FinanceDateXAxis
import com.scitrader.finance.pane.axes.FinanceNumericYAxis
import com.scitrader.finance.pane.modifiers.DoubleTapGestureModifier
import com.scitrader.finance.pane.modifiers.FinancePinchZoomModifier
import com.scitrader.finance.pane.modifiers.FinanceZoomPanModifier
import com.scitrader.finance.pane.modifiers.crosshair.CrosshairModifier
import com.scitrader.finance.pane.modifiers.legend.StudyLegend
import com.scitrader.finance.state.FinanceChartState
import com.scitrader.finance.state.PropertyState
import com.scitrader.finance.utils.toDip

interface IPaneFactory {
    val mainPaneHeightRatio: Float
    val secondaryPaneHeightRatio: Float

    fun createPane(financeChart: ISciFinanceChart, paneId: PaneId) : IPane
}

class DefaultPaneFactory(
    private val chartState: FinanceChartState = FinanceChartState(),
    override val mainPaneHeightRatio: Float = 0.3f,
    override val secondaryPaneHeightRatio: Float = 0.2f,
    ) : IPaneFactory{

    override fun createPane(financeChart: ISciFinanceChart, paneId: PaneId): IPane {
        val context = financeChart.context

        val globalState = chartState.chartState
        val paneState = chartState.paneStates[paneId.id] ?: PropertyState()

        val paneLayoutParams = generateDefaultPaneLayoutParams(paneId, mainPaneHeightRatio, secondaryPaneHeightRatio)

        val xAxis = FinanceDateXAxis(context).apply {
            maxAutoTicks = 4
            axisInfoProvider = FinanceNumericYAxis.FinanceNumericAxisInfoProvider()

            // set VisibleRangeLimit to prevent scrolling of chart far beyond current data edges
            setDataRangeChangeListener {
                val clone = RangeFactory.clone(it.dataRange)

                val diff = ComparableUtil.toDouble(it.visibleRange.diff) * 0.95

                clone.setMinMaxDouble(clone.minAsDouble - diff, clone.maxAsDouble + diff)

                it.visibleRangeLimit = clone
            }
        }

        val studyLegend = StudyLegend(context)

        val zoomPanModifier = FinanceZoomPanModifier().apply {
            clipModeX = ClipMode.ClipAtExtents
            clipModeTargetX = ClipModeTarget.VisibleRangeLimit
        }
        val pinchZoomModifier = FinancePinchZoomModifier().apply {
            receiveHandledEvents = true
        }
        val crosshairModifier = CrosshairModifier(studyLegend).apply {
            receiveHandledEvents = true
        }
        val doubleTapModifier = DoubleTapGestureModifier {
            financeChart.dispatchFinanceChartEvent(FinanceChartDoubleTapEvent())
        }.apply {
            receiveHandledEvents = true
        }
        val modifierGroup = ModifierGroup(
            zoomPanModifier,
            pinchZoomModifier,
            crosshairModifier,
            doubleTapModifier
        )

        val expandButton = LayoutInflater.from(context).inflate(R.layout.expand_chart_button_layout, null).apply {
            layoutParams = CanvasLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                val padding = 8f.toDip().toInt()
                setTop(padding)
                setRightWithAlignment(padding, CanvasLayout.LayoutParams.HORIZONTAL_ALIGNMENT_RIGHT)
            }
        }

        val sciChartLogo = SciChartLogoView(context).apply {
            layoutParams = CanvasLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                val padding = 0f.toDip().toInt()
                setBottom(padding)
                setLeftWithAlignment(padding, CanvasLayout.LayoutParams.HORIZONTAL_ALIGNMENT_LEFT)
            }
        }

        val xRangeButton = LayoutInflater.from(context).inflate(R.layout.x_range_button_layout, null).apply {
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                bottomMargin = 10F.toDip().toInt()
            }
        }

        val yAutoRangeButton: AppCompatButton = (LayoutInflater.from(context).inflate(R.layout.auto_y_range_chart_button_layout, null) as AppCompatButton).apply {
            layoutParams = LinearLayout.LayoutParams(30f.toDip().toInt(), 30f.toDip().toInt())
        }

        val chart = CanvasSciChartSurface(context).apply {
            xAxes.add(xAxis)
            chartModifiers.add(modifierGroup)

            modifierSurface.safeAdd(studyLegend)
            modifierSurface.safeAdd(expandButton)
        }

        val paneModifiers = DefaultPane.DefaultChartModifiers(modifierGroup, crosshairModifier, pinchZoomModifier, zoomPanModifier)

        val pane: IPane = if (paneId == PaneId.DEFAULT_PANE) {
            MainPane(
                chart,
                xAxis,
                paneId,
                studyLegend,
                expandButton,
                xRangeButton,
                yAutoRangeButton,
                sciChartLogo,
                paneModifiers,
                paneLayoutParams
            )
        } else {
            DefaultPane(
                chart,
                xAxis,
                paneId,
                studyLegend,
                expandButton,
                sciChartLogo,
                paneModifiers,
                paneLayoutParams
            )
        }

        return pane.also {
            it.restorePropertyStateFrom(globalState, paneState)

            globalState.readBool(SciFinanceChart.isCursorEnabledKey)?.let { value ->
                it.isCursorEnabled = value
            }
        }
    }

    private fun generateDefaultPaneLayoutParams(
        paneId: PaneId,
        mainPaneHeightRatio: Float,
        secondaryPaneHeightRatio: Float
    ) : ResizeableStackLayout.LayoutParams {
        val isMainPane = paneId == PaneId.DEFAULT_PANE

        return ResizeableStackLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            minHeightRatio = if (isMainPane) mainPaneHeightRatio else secondaryPaneHeightRatio
            heightRatio = if (isMainPane) mainPaneHeightRatio else secondaryPaneHeightRatio
        }
    }
}
