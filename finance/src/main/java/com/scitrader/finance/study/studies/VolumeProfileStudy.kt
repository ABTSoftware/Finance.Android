package com.scitrader.finance.study.studies

import android.content.Context
import android.util.Log
import com.scichart.charting.visuals.axes.NumericAxis
import com.scichart.charting.visuals.renderableSeries.FastColumnRenderableSeries
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.SolidBrushStyle
import com.scichart.drawing.utility.ColorUtil
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.OhlcvDataSourceId
import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.edit.properties.BrushStyleEditableProperty
import com.scitrader.finance.financeChart.InstrumentPriceFormatChangedEvent
import com.scitrader.finance.indicators.TALibIndicatorProvider
import com.scitrader.finance.pane.IPane
import com.scitrader.finance.pane.MainPane
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.pane.axes.FinanceVolumeProfileYAxis
import com.scitrader.finance.pane.series.ColumnNumericSeries
import com.scitrader.finance.pane.series.Constants
import com.scitrader.finance.pane.series.FinanceSeriesPaletteProvider
import com.scitrader.finance.study.CandleStudyBase
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.study.tooltips.IStudyTooltip
import com.scitrader.finance.study.tooltips.StudyTooltipBase

class VolumeProfileStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("VOLUME_PROFILE"),
    closeValuesId: DataSourceId = DataSourceId.DEFAULT_CLOSE_VALUES_ID,
    volumeValuesId: DataSourceId = DataSourceId.DEFAULT_VOLUME_VALUES_ID
) : CandleStudyBase(pane, id) {
    private val xValuesId: DataSourceId = DataSourceId.uniqueId(id, "xVol")
    private val yValuesId: DataSourceId = DataSourceId.uniqueId(id, "yVol")

    private val volumeYAxisId = AxisId(pane, id, "VolumeProfileYAxis")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val volumeYAxis = FinanceVolumeProfileYAxis(R.string.studyVolumeYAxis, volumeYAxisId)

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val volumeProfileIndicator = TALibIndicatorProvider.VolumeProfileIndicator(
        R.string.volumeProfileIndicatorName,
        50,
        closeValuesId,
        volumeValuesId,
        xValuesId, yValuesId
    )

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val volumeProfileSeries: ColumnNumericSeries

    init {

        indicators.add(volumeProfileIndicator)

        volumeProfileSeries = ColumnNumericSeries(
            R.string.volumeProfileIndicatorName,
            volumeProfileIndicator.xValuesId,
            volumeProfileIndicator.yValuesId,
            volumeYAxisId,
            "Volume Profile"
        ).apply {
            opacity.updateInitialValue(0.2f)
        }

        volumeProfileSeries.paletteProvider = FinanceSeriesPaletteProvider(
            FastColumnRenderableSeries::class.java,
            arrayOf(xValuesId, yValuesId)
        ) { map, index ->
            ColorUtil.argb(0xFF28996F.toInt(), volumeProfileSeries.opacity.value)
        }
        
        financeSeries.add(volumeProfileSeries)
        financeYAxes.add(volumeYAxis)
    }

    override fun onEvent(event: IFinanceChartEvent) {
        super.onEvent(event)

        if (event is InstrumentPriceFormatChangedEvent) {
            yAxis.textFormatting.updateInitialValue(event.priceFormat)
            yAxis.cursorTextFormatting.updateInitialValue(event.priceFormat)
        }
    }

    override fun placeInto(pane: IPane) {
        super.placeInto(pane)
        (pane as? MainPane)?.let {
//            pane.excludeAutoRangeAxisId(volumeYAxisId)
        }
        pane.chart.yAxes.forEach {
            if(it.axisId.contains("PriceSeries") && it.axisId.contains(yAxisId.axisName)){
                it.setVisibleRangeChangeListener { axis, oldRange, newRange, isAnimating ->
                    volumeProfileIndicator.setVisibleRange(newRange as DoubleRange)
                }
            }
        }
    }

    override fun reset() {
        super.reset()

        volumeProfileSeries.reset()
        volumeProfileIndicator.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != volumeProfileIndicator.inputClose && editable != volumeProfileIndicator.inputVolume
    }

    override val title: CharSequence
        get() = "Volume Profile"

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return VolumeProfileTooltip(context, this)
    }

    class VolumeProfileTooltip(context: Context, study: VolumeProfileStudy) : StudyTooltipBase<VolumeProfileStudy>(context, study) {
        private val volumeProfileSeriesTooltip = study.volumeProfileSeries.getTooltip()

        init {
            orientation = HORIZONTAL
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips)
                volumeProfileSeriesTooltip.placeInto(this)
            else
                volumeProfileSeriesTooltip.removeFrom(this)
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            volumeProfileSeriesTooltip.tryUpdate(x, y)
        }
    }
}
