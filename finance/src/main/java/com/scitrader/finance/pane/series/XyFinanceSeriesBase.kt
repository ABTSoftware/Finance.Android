package com.scitrader.finance.pane.series

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.visuals.renderableSeries.XyRenderableSeriesBase
import com.scichart.charting.visuals.renderableSeries.hitTest.DefaultXySeriesInfoProvider
import com.scichart.charting.visuals.renderableSeries.hitTest.HitTestInfo
import com.scichart.charting.visuals.renderableSeries.hitTest.XySeriesInfo
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IFillPaletteProvider
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IStrokePaletteProvider
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip
import com.scichart.charting.visuals.renderableSeries.tooltips.SeriesTooltipBase
import com.scichart.core.model.IntegerValues
import com.scichart.drawing.common.FontStyle
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.IDataManager
import com.scitrader.finance.edit.properties.DataSourceEditableProperty
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.utils.opaqueColor
import java.util.*

abstract class XyFinanceSeriesBase<TRenderableSeries : XyRenderableSeriesBase, TDataSeries : IXyDataSeries<Date, Double>>(
    @StringRes name: Int,
    xValues: DataSourceId,
    yValues: DataSourceId,
    renderableSeries: TRenderableSeries,
    dataSeries: TDataSeries,
    yAxisId: AxisId,
    yTooltipName: CharSequence?
) : FinanceSeriesBase<TRenderableSeries, TDataSeries>(name, renderableSeries, dataSeries, yAxisId) {
    val xValues = DataSourceEditableProperty(R.string.xValues, name, xValues) { _, value ->
        dependsOn(R.string.xValues, value)
    }

    val yValues = DataSourceEditableProperty(R.string.yValues, name, yValues) { _, value ->
        dependsOn(R.string.yValues, value)
    }

    init {
        renderableSeries.seriesInfoProvider = FinanceXySeriesInfoProvider(yTooltipName)
    }

    override fun reset() {
        super.reset()

        xValues.reset()
        yValues.reset()
    }

    override fun onDataDrasticallyChanged(dataManager: IDataManager) {
        val suspender = renderableSeries.suspendUpdates()
        try {
            dataSeries.clear()

            dataManager.readLock {
                val xValues = dataManager.getXValues(xValues.value)
                val yValues = dataManager.getYValues(yValues.value)

                if (xValues != null && yValues != null)
                    dataSeries.append(xValues, yValues)
            }
        } finally {
            suspender.dispose()
        }
    }

    protected open class FinanceXySeriesInfoProvider(private val yTooltipName: CharSequence?) : DefaultXySeriesInfoProvider() {
        override fun getSeriesTooltipInternal(
            context: Context?,
            seriesInfo: XySeriesInfo<out XyRenderableSeriesBase>?,
            modifierType: Class<*>?
        ): ISeriesTooltip {
            return FinanceXySeriesTooltip(context, seriesInfo, yTooltipName)
        }
    }

    protected open class FinanceXySeriesTooltip(
        context: Context?,
        seriesInfo: XySeriesInfo<*>?,
        private val yTooltipName: CharSequence?,
        fontStyle: FontStyle = com.scitrader.finance.pane.series.Constants.DefaultTooltipInfoFontStyle,
    ) :
        SeriesTooltipBase<XySeriesInfo<*>>(context, seriesInfo) {

        private var infoTextColor: Int? = null

        init {
            typeface = fontStyle.typeface
            textSize = fontStyle.textSize
            setTextColor(fontStyle.textColor)
        }

        override fun update(hitTestInfo: HitTestInfo?, interpolate: Boolean) {
            hitTestInfo?.let {
                infoTextColor = tryGetColorFromPaletteProvider(it)
            }

            super.update(hitTestInfo, interpolate)
        }

        override fun internalUpdate(seriesInfo: XySeriesInfo<*>?) {
            seriesInfo?.run {
                text = createString(seriesInfo.formattedYValue)
            }
        }

        private fun createString(yValue: CharSequence) : SpannableStringBuilder {
            val sb = SpannableStringBuilder()

            yTooltipName?.run {
                sb.append("$this: ")
            }
            val textColor = opaqueColor(infoTextColor ?: seriesInfo.seriesColor)
            sb.append(yValue, ForegroundColorSpan(textColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return sb
        }

        private fun tryGetColorFromPaletteProvider(hitTestInfo: HitTestInfo) : Int? {
            var colors: IntegerValues? = null

            val paletteProvider = renderableSeries.paletteProvider
            if (paletteProvider is IFillPaletteProvider)
                colors = paletteProvider.fillColors
            else if (paletteProvider is IStrokePaletteProvider)
                colors = paletteProvider.strokeColors

            colors?.let {
                val index = hitTestInfo.pointSeriesIndex
                if (index < colors.size()) {
                    return colors.get(index)
                }
            }

            return null
        }
    }
}
