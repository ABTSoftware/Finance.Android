package com.scitrader.finance.pane.axes

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.scichart.charting.visuals.axes.AxisModifierSurface
import com.scichart.charting.visuals.axes.NumericAxis
import com.scichart.data.model.DoubleRange

open class FinanceNumericAxis(context: Context?) : NumericAxis(DoubleRange(0.0, 10.0), NonClippingYAxisModifierSurface(context)) {
    init {
        axisInfoProvider = FinanceNumericYAxis.FinanceNumericAxisInfoProvider()

        setRangeCalculationHelper(FinanceNumericYAxis.FinanceRangeCalculationHelper())
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