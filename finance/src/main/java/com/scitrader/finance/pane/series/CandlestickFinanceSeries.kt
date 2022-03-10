package com.scitrader.finance.pane.series

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.OhlcDataSeries
import com.scichart.charting.visuals.renderableSeries.FastCandlestickRenderableSeries
import com.scichart.charting.visuals.renderableSeries.hitTest.DefaultOhlcSeriesInfoProvider
import com.scichart.charting.visuals.renderableSeries.hitTest.OhlcSeriesInfo
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip
import com.scichart.charting.visuals.renderableSeries.tooltips.SeriesTooltipBase
import com.scichart.core.utility.ComparableUtil
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.common.SolidBrushStyle
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.properties.BrushStyleEditableProperty
import com.scitrader.finance.edit.properties.DataPointWidthEditableProperty
import com.scitrader.finance.edit.properties.PenStyleEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.utils.OhlcDataSeries
import com.scitrader.finance.utils.SolidPenStyle
import com.scitrader.finance.utils.opaqueColor
import java.util.*

class CandlestickFinanceSeries(
    @StringRes name: Int,
    xValues: DataSourceId,
    open: DataSourceId,
    high: DataSourceId,
    low: DataSourceId,
    close: DataSourceId,
    yAxisId: AxisId,
    openTooltipName: CharSequence? = "O",
    highTooltipName: CharSequence? = "H",
    lowTooltipName: CharSequence? = "L",
    closeTooltipName: CharSequence? = "C"
) : OhlcFinanceSeriesBase<FastCandlestickRenderableSeries, OhlcDataSeries<Date, Double>>(
    name,
    xValues,
    open,
    high,
    low,
    close,
    FastCandlestickRenderableSeries(),
    OhlcDataSeries(),
    yAxisId
) {
    init {
        renderableSeries.seriesInfoProvider = FinanceOhlcSeriesInfoProvider(openTooltipName, highTooltipName, lowTooltipName, closeTooltipName)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val strokeUpStyle = PenStyleEditableProperty(R.string.candlestickStrokeUpStyle, name, SolidPenStyle(
        com.scitrader.finance.pane.series.Constants.DefaultStrokeUp, com.scitrader.finance.pane.series.Constants.LightThickness)) { id, value ->
        renderableSeries.strokeUpStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val strokeDownStyle = PenStyleEditableProperty(R.string.candlestickStrokeDownStyle, name, SolidPenStyle(
        com.scitrader.finance.pane.series.Constants.DefaultStrokeDown, com.scitrader.finance.pane.series.Constants.LightThickness)) { id, value ->
        renderableSeries.strokeDownStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val fillUpBrushStyle = BrushStyleEditableProperty(R.string.candlestickFillUpStyle, name, SolidBrushStyle(
        com.scitrader.finance.pane.series.Constants.DefaultFillUp)) { id, value ->
        renderableSeries.fillUpBrushStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    var fillDownBrushStyle = BrushStyleEditableProperty(R.string.candlestickFillDownStyle, name, SolidBrushStyle(
        com.scitrader.finance.pane.series.Constants.DefaultFillDown)) { id, value ->
        renderableSeries.fillDownBrushStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    var dataPointWidth = DataPointWidthEditableProperty(
        R.string.candlestickDataPointWidth,
        name,
        com.scitrader.finance.pane.series.Constants.DefaultCandleStickDataPointWidth
    ) { id, value ->
        renderableSeries.dataPointWidth = value
        onPropertyChanged(id)
    }

    override fun reset() {
        super.reset()

        strokeUpStyle.reset()
        strokeDownStyle.reset()
        fillUpBrushStyle.reset()
        fillDownBrushStyle.reset()
        dataPointWidth.reset()
    }

    protected open class FinanceOhlcSeriesInfoProvider(
        private val openTooltipName: CharSequence?,
        private val highTooltipName: CharSequence?,
        private val lowTooltipName: CharSequence?,
        private val closeTooltipName: CharSequence?
    ) : DefaultOhlcSeriesInfoProvider() {
        override fun getSeriesTooltipInternal(
            context: Context?,
            seriesInfo: OhlcSeriesInfo?,
            modifierType: Class<*>?
        ): ISeriesTooltip {
            return FinanceOhlcSeriesTooltip(context, seriesInfo, openTooltipName, highTooltipName, lowTooltipName, closeTooltipName)
        }
    }

    protected open class FinanceOhlcSeriesTooltip(
        context: Context?,
        seriesInfo: OhlcSeriesInfo?,
        private val openTooltipName: CharSequence?,
        private val highTooltipName: CharSequence?,
        private val lowTooltipName: CharSequence?,
        private val closeTooltipName: CharSequence?,
        fontStyle: FontStyle = com.scitrader.finance.pane.series.Constants.DefaultTooltipInfoFontStyle
    ) :
        SeriesTooltipBase<OhlcSeriesInfo>(context, seriesInfo) {

        init {
            typeface = fontStyle.typeface
            textSize = fontStyle.textSize
            setTextColor(fontStyle.textColor)
        }

        override fun internalUpdate(seriesInfo: OhlcSeriesInfo?) {
            seriesInfo?.run {
                val sb = SpannableStringBuilder()

                val open = seriesInfo.formattedOpenValue
                val high = seriesInfo.formattedHighValue
                val low = seriesInfo.formattedLowValue
                val close = seriesInfo.formattedCloseValue

                var color = seriesInfo.seriesColor

                (seriesInfo.renderableSeries as? FastCandlestickRenderableSeries)?.run {
                    val downColor = this.fillDownBrushStyle.color
                    val upColor = this.fillUpBrushStyle.color

                    val openValue = ComparableUtil.toDouble(openValue)
                    val closeValue = ComparableUtil.toDouble(closeValue)

                    color = if (openValue > closeValue) downColor else upColor
                }

                color = opaqueColor(color)

                openTooltipName?.run {
                    sb.append("$this: ")
                }
                sb.append(open, ForegroundColorSpan(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                highTooltipName?.run {
                    sb.append(" $this:")
                }
                sb.append(" $high", ForegroundColorSpan(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                lowTooltipName?.run {
                    sb.append(" $this:")
                }
                sb.append(" $low", ForegroundColorSpan(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                closeTooltipName?.run {
                    sb.append(" $this:")
                }
                sb.append(" $close", ForegroundColorSpan(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                text = sb
            }
        }
    }
}
