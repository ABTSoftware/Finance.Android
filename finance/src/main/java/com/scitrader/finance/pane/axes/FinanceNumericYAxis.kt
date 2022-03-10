package com.scitrader.finance.pane.axes

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StringRes
import com.scichart.charting.visuals.axes.*
import com.scichart.charting.visuals.axes.NumericAxis
import com.scichart.charting.visuals.axes.rangeCalculators.NumericRangeCalculationHelper
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.data.model.DoubleRange
import com.scitrader.finance.R
import com.scitrader.finance.pane.modifiers.crosshair.CrosshairModifier

open class FinanceNumericYAxis(
    @StringRes name: Int,
    axisId: AxisId,
    textFormatting: String = NumericAxis.DEFAULT_TEXT_FORMATTING,
    cursorTextFormatting: String = NumericAxis.DEFAULT_TEXT_FORMATTING
) : FinanceYAxisBase<NumericAxis>(name, axisId, textFormatting, cursorTextFormatting) {

    override fun createAxis(context: Context): NumericAxis {
        return FinanceNumericAxis(context).apply {
            autoRange = AutoRange.Always
            growBy = DoubleRange(0.2, 0.2)
            maxAutoTicks = 5
        }
    }

    override fun reset() {}

    protected class FinanceNumericAxis(context: Context?) : NumericAxis(DoubleRange(0.0, 10.0), NonClippingYAxisModifierSurface(context)) {
        init {
            axisInfoProvider = FinanceNumericAxisInfoProvider()

            setRangeCalculationHelper(FinanceRangeCalculationHelper())
        }

        override fun onIsPrimaryAxisChanged(isPrimaryAxis: Boolean) {
            super.onIsPrimaryAxisChanged(isPrimaryAxis)

            // bind visibility of axis to IsPrimaryAxis property
            visibility = if(isPrimaryAxis) View.VISIBLE else View.GONE
        }

        class NonClippingYAxisModifierSurface @JvmOverloads constructor(
            context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
        ) : AxisModifierSurface(context, attrs, defStyleAttr) {

            override fun layoutChildAt(child: View?, left: Int, top: Int, right: Int, bottom: Int) {
                when {
                    top < 0 -> {
                        super.layoutChildAt(child, left, 0, right, bottom - top)
                    }

                    bottom > height -> {
                        super.layoutChildAt(child, left, top - (bottom - height), right, height)
                    }

                    else -> {
                        super.layoutChildAt(child, left, top, right, bottom)
                    }
                }
            }
        }
    }

    protected class FinanceRangeCalculationHelper : NumericRangeCalculationHelper() {
        override fun isValidSeriesForYAxis(rs: IRenderableSeries, yAxisId: String): Boolean {
            val axisId = AxisId.fromString(axis.axisId)
            val rsYAxisID = AxisId.fromString(rs.yAxisId)

            return shouldShareVisibleRange(axisId, rsYAxisID) && rs.isVisible && rs.isValidForUpdate
        }
    }

    class FinanceNumericAxisInfoProvider : DefaultAxisInfoProvider() {
        override fun getAxisTooltipInternal(
            context: Context?,
            axisInfo: AxisInfo?,
            modifierType: Class<*>?
        ): IAxisTooltip {
            return when(modifierType) {
                CrosshairModifier::class.java -> FinanceNumericAxisTooltip(context, axisInfo)
                else -> super.getAxisTooltipInternal(context, axisInfo, modifierType)
            }
        }
    }

    class FinanceNumericAxisTooltip(context: Context?, axisInfo: AxisInfo?) : AxisTooltip(
        context,
        axisInfo
    ) {
        init {
            setTooltipBackground(R.drawable.crosshair_axis_tooltip_background)
        }

        override fun updateInternal(axisInfo: AxisInfo?): Boolean {
            return super.updateInternal(axisInfo)
        }
    }
}
