package com.scitrader.finance.pane.axes

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.NumericAxis
import com.scichart.data.model.DoubleRange

class FinanceVolumeProfileYAxis(
    @StringRes name: Int,
    axisId: AxisId,
    textFormatting: String = NumericAxis.DEFAULT_TEXT_FORMATTING,
    cursorTextFormatting: String = NumericAxis.DEFAULT_TEXT_FORMATTING
): FinanceNumericYAxis(name, axisId, textFormatting, cursorTextFormatting) {

    override fun createAxis(context: Context): NumericAxis {
        return FinanceNumericVolumeAxis(context).apply {
            autoRange = AutoRange.Always
            growBy = DoubleRange(0.0, 3.0)
//            maxAutoTicks = 5
            axisAlignment = AxisAlignment.Top
            axisId = "VolumeProfileYAxis"
            visibility = View.GONE
        }
    }

    class FinanceNumericVolumeAxis(context: Context): FinanceNumericAxis(context){
        override fun onIsPrimaryAxisChanged(isPrimaryAxis: Boolean) {
            super.onIsPrimaryAxisChanged(isPrimaryAxis)

            visibility = View.VISIBLE

        }
    }
}