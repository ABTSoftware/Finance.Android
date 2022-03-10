package com.scitrader.finance.pane.axes

import android.content.Context
import androidx.annotation.StringRes
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.data.model.DoubleRange
import com.scitrader.finance.R
import com.scitrader.finance.edit.properties.StringEditableProperty
import com.scitrader.finance.pane.IPane
import com.scitrader.finance.state.EditablePropertyState

abstract class FinanceYAxisBase<TAxis : IAxis>(
    @StringRes final override val name: Int,
    axisId: AxisId,
    textFormatting: String,
    cursorTextFormatting: String
) : com.scitrader.finance.core.AttachableBase(), IFinanceAxis {
    final override val viewType: Int = com.scitrader.finance.edit.annotations.PropertyType.YAxis

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val textFormatting = StringEditableProperty(R.string.textFormattingAxis, R.string.textFormattingAxis, textFormatting) { _, textFormatting ->
        yAxis?.let { axis ->
            axis.textFormatting = textFormatting
        }
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val cursorTextFormatting = StringEditableProperty(R.string.cursorTextFormatting, R.string.cursorTextFormatting, cursorTextFormatting) { _, cursorTextFormatting ->
        yAxis?.let { axis ->
            axis.cursorTextFormatting = cursorTextFormatting
        }
    }

    final override var axisId: AxisId = axisId
        set(value) {
            field = value

            yAxis?.axisId = value.toString()
        }

    private var yAxis : TAxis? = null

    override fun placeInto(pane: IPane) {
        val chart = pane.chart

        val yAxis = createAxis(chart.context).also {
            initAxis(it)
        }

        chart.yAxes.let { yAxes ->
            val thisAxisId = axisId
            yAxes.firstOrNull { axis ->
                shouldShareVisibleRange(thisAxisId, AxisId.fromString(axis.axisId))
            }?.let { duplicateAxis ->
                yAxis.visibleRange = duplicateAxis.visibleRange
            }

            yAxes.add(yAxis)
        }

        this.yAxis = yAxis
    }

    override fun removeFrom(pane: IPane) {
        val chart = pane.chart
        
        yAxis?.let {
            it.visibleRange = DoubleRange()
            chart.yAxes.remove(it)
        }
        yAxis = null
    }

    abstract fun createAxis(context: Context) : TAxis

    protected open fun initAxis(axis: TAxis) {
        axis.axisId = axisId.toString()
        axis.textFormatting = textFormatting.value
        axis.cursorTextFormatting = cursorTextFormatting.value
    }

    override fun savePropertyStateTo(state: EditablePropertyState) {
        state.savePropertyValues(this)
    }

    override fun restorePropertyStateFrom(state: EditablePropertyState) {
        state.tryRestorePropertyValues(this)
    }

    companion object {
        // if axis is placed inside same pane and have same name it should share same VisibleRange
        // e.g. if MA is placed within same chart as price series
        fun shouldShareVisibleRange(thisId: AxisId, thatId: AxisId): Boolean {
            return thisId.axisName == thatId.axisName && thisId.pane == thatId.pane
        }

        fun selectAxesWithNonSharedRange(yAxes: Iterable<IAxis>) : Iterable<IAxis> {
            // group yAxes by pane and axisName ( excluding study ) to get list of yAxes with shared VisibleRange
            // and select only one axis from each group to prevent bugs with modifiers which change VisibleRange
            return yAxes.groupBy {
                val axisId = AxisId.fromString(it.axisId)

                "${axisId.pane}${axisId.axisName}"
            }.values.mapNotNull { it.firstOrNull() }
        }
    }

}
