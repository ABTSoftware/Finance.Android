package com.scitrader.finance.pane.series

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.XyyDataSeries
import com.scichart.charting.visuals.renderableSeries.FastBandRenderableSeries
import com.scichart.charting.visuals.renderableSeries.hitTest.BandSeriesInfo
import com.scichart.charting.visuals.renderableSeries.hitTest.DefaultBandSeriesInfoProvider
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip
import com.scichart.charting.visuals.renderableSeries.tooltips.SeriesTooltipBase
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.common.SolidBrushStyle
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.edit.properties.BrushStyleEditableProperty
import com.scitrader.finance.edit.properties.PenStyleEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.utils.SolidPenStyle
import com.scitrader.finance.utils.XyyDataSeries
import com.scitrader.finance.utils.opaqueColor
import java.util.*

class BandFinanceSeries(
    @StringRes name: Int,
    xValues: DataSourceId,
    yValues: DataSourceId,
    y1Values: DataSourceId,
    yAxisId: AxisId,
    yTooltipName: CharSequence? = null,
    y1TooltipName: CharSequence? = null
) :
    XyyFinanceSeriesBase<FastBandRenderableSeries, XyyDataSeries<Date, Double>>(
        name,
        xValues,
        yValues,
        y1Values,
        FastBandRenderableSeries(),
        XyyDataSeries(),
        yAxisId
    ) {
    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val strokeStyle = PenStyleEditableProperty(R.string.strokeStyle, name, SolidPenStyle(com.scitrader.finance.pane.series.Constants.DefaultBlue, com.scitrader.finance.pane.series.Constants.LightThickness)) { id, value ->
        renderableSeries.strokeStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val strokeY1Style = PenStyleEditableProperty(R.string.bandsY1StrokeStyle, name, SolidPenStyle(
        com.scitrader.finance.pane.series.Constants.DefaultBlue, com.scitrader.finance.pane.series.Constants.LightThickness)) { id, value ->
        renderableSeries.strokeY1Style = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val fillBrushStyle = BrushStyleEditableProperty(R.string.fillStyle, name, SolidBrushStyle(com.scitrader.finance.pane.series.Constants.DefaultBand)) { id, value ->
        renderableSeries.fillBrushStyle = value
        onPropertyChanged(id)
    }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val fillY1BrushStyle = BrushStyleEditableProperty(R.string.bandsY1FillStyle, name, SolidBrushStyle(
        com.scitrader.finance.pane.series.Constants.DefaultBand)) { id, value ->
        renderableSeries.fillY1BrushStyle = value
        onPropertyChanged(id)
    }

    init {
        renderableSeries.seriesInfoProvider = FinanceBandSeriesInfoProvider(yTooltipName, y1TooltipName)
    }

    override fun reset() {
        super.reset()

        strokeStyle.reset()
        strokeY1Style.reset()
        fillBrushStyle.reset()
        fillY1BrushStyle.reset()
    }

    protected open class FinanceBandSeriesInfoProvider(
        private val yTooltipName: CharSequence?,
        private val y1TooltipName: CharSequence?
    ) : DefaultBandSeriesInfoProvider() {
        override fun getSeriesTooltipInternal(
            context: Context?,
            seriesInfo: BandSeriesInfo?,
            modifierType: Class<*>?
        ): ISeriesTooltip {
            return FinanceBandSeriesTooltip(context, seriesInfo, yTooltipName, y1TooltipName)
        }
    }

    protected open class FinanceBandSeriesTooltip(
        context: Context?,
        seriesInfo: BandSeriesInfo?,
        private val yTooltipName: CharSequence?,
        private val y1TooltipName: CharSequence?,
        fontStyle: FontStyle = com.scitrader.finance.pane.series.Constants.DefaultTooltipInfoFontStyle
    ) :
        SeriesTooltipBase<BandSeriesInfo>(context, seriesInfo) {

        init {
            typeface = fontStyle.typeface
            textSize = fontStyle.textSize
            setTextColor(fontStyle.textColor)
        }

        override fun internalUpdate(seriesInfo: BandSeriesInfo?) {
            seriesInfo?.run {
                val sb = SpannableStringBuilder()

                val yValue = seriesInfo.formattedYValue
                val y1Value = seriesInfo.formattedY1Value

                val yColor = opaqueColor(seriesInfo.renderableSeries.strokeStyle.color)
                val y1Color = opaqueColor(seriesInfo.renderableSeries.strokeY1Style.color)

                y1TooltipName?.run {
                    sb.append("$this: ")
                }
                sb.append(y1Value, ForegroundColorSpan(y1Color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                yTooltipName?.run {
                    sb.append(" $this:")
                }
                sb.append(" $yValue", ForegroundColorSpan(yColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                text = sb
            }
        }
    }
}
