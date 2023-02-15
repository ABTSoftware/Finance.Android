package com.scitrader.finance.study.studies

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.scichart.charting.modifiers.SeriesValueModifier
import com.scichart.charting.numerics.indexDataProvider.DataSeriesIndexDataProvider
import com.scichart.charting.visuals.axes.IndexDateAxis
import com.scichart.charting.visuals.axes.NumericAxis
import com.scichart.charting.visuals.renderableSeries.FastColumnRenderableSeries
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.utility.ColorUtil
import com.scitrader.finance.R
import com.scitrader.finance.core.OhlcvDataSourceId
import com.scitrader.finance.core.event.IFinanceChartEvent
import com.scitrader.finance.core.event.StudyTitlePressedEvent
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.edit.properties.PropertyId
import com.scitrader.finance.financeChart.InstrumentPriceFormatChangedEvent
import com.scitrader.finance.pane.IPane
import com.scitrader.finance.pane.MainPane
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.pane.axes.FinanceNumericYAxis
import com.scitrader.finance.pane.series.CandlestickFinanceSeries
import com.scitrader.finance.pane.series.ColumnFinanceSeries
import com.scitrader.finance.pane.series.FinanceSeriesPaletteProvider
import com.scitrader.finance.study.CandleStudyBase
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.study.studies.events.LegendInstrumentModelChangedEvent
import com.scitrader.finance.study.tooltips.IStudyTooltip
import com.scitrader.finance.study.tooltips.LegendInstrumentModel
import com.scitrader.finance.study.tooltips.StudyTooltipBase
import java.text.DecimalFormat

class PriceSeriesStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("PriceSeries"),
    ohlcvDataSourceId: OhlcvDataSourceId = OhlcvDataSourceId.DEFAULT_OHLCV_VALUES_IDS
) : CandleStudyBase(pane, id) {
    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val priceSeries: CandlestickFinanceSeries

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val volumeSeries: ColumnFinanceSeries

    private var dataSeriesIndexDataProvider: DataSeriesIndexDataProvider? = null

    private val volumeAxisId = AxisId(pane, id, "VolumeAxis")

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val volumeYAxis = FinanceVolumeYAxis(R.string.studyVolumeYAxis, volumeAxisId)

    private var lastLegendInstrumentModel: LegendInstrumentModel? = null

    override val title: CharSequence
        get() = lastLegendInstrumentModel?.name ?: ""

    init {
        with(ohlcvDataSourceId) {
            priceSeries = CandlestickFinanceSeries(
                R.string.studyPriceSeries,
                xValuesId,
                openValuesId,
                highValuesId,
                lowValuesId,
                closeValuesId,
                yAxisId
            )
            dataSeriesIndexDataProvider = DataSeriesIndexDataProvider(priceSeries.dataSeries)

            volumeSeries = ColumnFinanceSeries(
                R.string.studyVolumeSeries,
                xValuesId,
                volumeValuesId,
                volumeAxisId,
                "Vol"
            ).apply {
                opacity.updateInitialValue(0.2f)
            }

            volumeSeries.paletteProvider = FinanceSeriesPaletteProvider(
                FastColumnRenderableSeries::class.java,
                arrayOf(openValuesId, closeValuesId)
            ) { map, index ->
                val open = map[openValuesId]?.get(index)!!
                val close = map[closeValuesId]?.get(index)!!

                val opacity = volumeSeries.opacity.value
                if (open > close)
                    ColorUtil.argb(priceSeries.fillDownBrushStyle.value.color, opacity)
                else
                    ColorUtil.argb(priceSeries.fillUpBrushStyle.value.color, opacity)
            }
        }

        financeSeries.add(priceSeries)
        financeSeries.add(volumeSeries)
        financeYAxes.add(volumeYAxis)
    }

    override fun onPropertyChanged(propertyId: PropertyId) {
        super.onPropertyChanged(propertyId)

        if (priceSeries.dataPointWidth.propertyId == propertyId) {
            volumeSeries.dataPointWidth.trySetValue(priceSeries.dataPointWidth.value)
        }
    }

    override fun onEvent(event: IFinanceChartEvent) {
        super.onEvent(event)

        if(event is LegendInstrumentModelChangedEvent) {
            lastLegendInstrumentModel = event.model
            invalidateStudy()
        }

        if (event is InstrumentPriceFormatChangedEvent) {
            yAxis.textFormatting.updateInitialValue(event.priceFormat)
            yAxis.cursorTextFormatting.updateInitialValue(event.priceFormat)
        }
    }

    override fun reset() {
        super.reset()

        priceSeries.reset()
        volumeSeries.reset()
    }

    override fun isValidEditableForSettings(editable: IEditable): Boolean {
        return editable != volumeSeries.fillStyle &&
                editable != volumeSeries.strokeStyle &&
                editable != volumeSeries.dataPointWidth
    }

    override fun getStudyTooltip(context: Context): IStudyTooltip {
        return PricesStudyTooltip(context, this)
    }

    private val seriesValueModifier = SeriesValueModifier(HorizontalLineSeriesValueMarkerFactory { rs -> rs === priceSeries.renderableSeries })

    override fun placeInto(pane: IPane) {
        super.placeInto(pane)

        pane.chart.chartModifiers.add(seriesValueModifier)
        (pane.chart.xAxes.firstOrNull() as? IndexDateAxis)?.apply {
            setIndexDataProvider(dataSeriesIndexDataProvider)
        }

        (pane as? MainPane)?.let {
            pane.excludeAutoRangeAxisId(volumeAxisId)
        }
    }

    override fun removeFrom(pane: IPane) {
        pane.chart.chartModifiers.remove(seriesValueModifier)

        (pane as? MainPane)?.let {
            pane.removeExcludedAutoRangeAxisId(volumeAxisId)
        }

        super.removeFrom(pane)
    }

    class FinanceVolumeYAxis(
        @StringRes name: Int,
        axisId: AxisId,
    ) : FinanceNumericYAxis(name, axisId) {
        override fun createAxis(context: Context): NumericAxis {
            return super.createAxis(context).apply {
                growBy = DoubleRange(0.0, 4.0)
            }
        }

        override fun reset() {}
    }

    class PricesStudyTooltip(context: Context, study: PriceSeriesStudy) : StudyTooltipBase<PriceSeriesStudy>(context, study) {
        private val tooltipText = SpannableStringBuilder()

        private val priceFormat = DecimalFormat("0.##")
        private val priceChangeFormat = DecimalFormat("+0.##;-0.##")
        private val priceChangePercentFormat = DecimalFormat("+0.##;-0.##")

        private val upColor = ContextCompat.getColor(context, R.color.ohlc_legend_up_color)
        private val downColor = ContextCompat.getColor(context, R.color.ohlc_legend_down_color)

        private val priceSeriesTooltip = study.priceSeries.getTooltip()
        private val volumeSeriesTooltip = study.volumeSeries.getTooltip()

        init {
            titleTextView.setOnClickListener {
                study.dispatchStudyEvent(StudyTitlePressedEvent(study))
            }
        }

        override fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {
            super.onShowSeriesTooltipsChanged(showSeriesTooltips)

            if (showSeriesTooltips) {
                priceSeriesTooltip.placeInto(this)
                volumeSeriesTooltip.placeInto(this)
            } else {
                priceSeriesTooltip.removeFrom(this)
                volumeSeriesTooltip.removeFrom(this)
            }
        }

        override fun updateTitleView(titleTextView: TextView) {
            study.lastLegendInstrumentModel?.let { legendInstrumentModel ->
                titleTextView.text = tooltipText.apply {
                    clear()

                    val title = study.title

                    val lastPrice = priceFormat.format(legendInstrumentModel.lastPrice)
                    val priceChange = priceChangeFormat.format(legendInstrumentModel.priceChange)
                    val priceChangePercent =
                            priceChangePercentFormat.format(legendInstrumentModel.priceChangePercent)

                    val infoString = " $lastPrice $priceChange ($priceChangePercent%)"
                    append(title).append(infoString)

                    val color = if (legendInstrumentModel.priceChange < 0) downColor else upColor
                    setSpan(
                        ForegroundColorSpan(color),
                        title.length,
                        title.length + infoString.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        override fun update(x: Float, y: Float) {
            super.update(x, y)

            priceSeriesTooltip.tryUpdate(x, y)
            volumeSeriesTooltip.tryUpdate(x, y)
        }
    }
}
