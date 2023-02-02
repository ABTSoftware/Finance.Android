package com.scitrader.finance.pane.axes

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.scichart.charting.numerics.labelProviders.CalendarDateLabelFormatter
import com.scichart.charting.numerics.labelProviders.DateLabelProvider
import com.scichart.charting.visuals.axes.AxisModifierSurface
import com.scichart.charting.visuals.axes.IndexDateAxis
import com.scichart.core.utility.StringUtil
import com.scichart.data.model.DateRange
import java.util.*

open class FinanceDateXAxis(context: Context?) : IndexDateAxis(DateRange(), NonClippingXAxisModifierSurface(context)) {

    init {
        labelProvider = DateLabelProvider(CalendarDateLabelFormatter(Locale.getDefault(), TimeZone.getTimeZone("UTC")))

        // need to set empty string to use CalendarDateLabelFormatter
        textFormattingProperty.setWeakValue(StringUtil.EMPTY)
        cursorTextFormattingProperty.setWeakValue(StringUtil.EMPTY)
    }

    class NonClippingXAxisModifierSurface @JvmOverloads constructor(
        context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : AxisModifierSurface(context, attrs, defStyleAttr) {

        override fun layoutChildAt(child: View?, left: Int, top: Int, right: Int, bottom: Int) {
            when {
                left < 0 -> {
                    super.layoutChildAt(child, 0, top, right - left, bottom)
                }
                right > width -> {
                    super.layoutChildAt(child, left - (right - width), top, width, bottom)
                }
                else -> {
                    super.layoutChildAt(child, left, top, right, bottom)
                }
            }
        }
    }
}
