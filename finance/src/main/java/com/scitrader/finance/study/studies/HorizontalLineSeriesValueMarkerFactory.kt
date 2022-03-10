package com.scitrader.finance.study.studies

import android.content.Context
import android.graphics.drawable.PaintDrawable
import com.scichart.charting.modifiers.SeriesValueModifier
import com.scichart.charting.visuals.ISciChartSurface
import com.scichart.charting.visuals.annotations.AnnotationLabel
import com.scichart.charting.visuals.annotations.HorizontalLineAnnotation
import com.scichart.charting.visuals.annotations.LabelPlacement
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.core.IServiceContainer
import com.scichart.core.common.Predicate
import com.scichart.core.utility.Dispatcher
import com.scichart.core.utility.ListUtil
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.common.SolidPenStyle
import com.scichart.drawing.utility.ColorUtil
import com.scitrader.finance.utils.toDip

class HorizontalLineSeriesValueMarkerFactory(private val isValidSeriesPredicate: Predicate<IRenderableSeries>) :
    SeriesValueModifier.ISeriesValueMarkerFactory {
    override fun createMarkerFor(renderableSeries: IRenderableSeries?): SeriesValueModifier.ISeriesValueMarker {
        return HorizontalLineSeriesValueMarker(renderableSeries, isValidSeriesPredicate)
    }
}


class HorizontalLineSeriesValueMarker(
    renderableSeries: IRenderableSeries?,
    isValidRenderableSeriesPredicate: Predicate<IRenderableSeries>?
) : SeriesValueModifier.SeriesValueMarkerBase(
    renderableSeries,
    isValidRenderableSeriesPredicate
) {
    private var markerAnnotation: HorizontalLineSeriesValueMarkerAnnotation? = null

    /**
     * {@inheritDoc}
     */
    override fun tryRemoveMarkerAnnotation(parentSurface: ISciChartSurface) {
        parentSurface.annotations.remove(markerAnnotation)
    }

    /**
     * {@inheritDoc}
     */
    override fun tryAddMarkerAnnotation(parentSurface: ISciChartSurface) {
        ListUtil.safeAddExact(parentSurface.annotations, markerAnnotation)
    }

    override fun createMarkerAnnotation(context: Context?) {
        markerAnnotation = HorizontalLineSeriesValueMarkerAnnotation(
            context!!,
            HorizontalLineSeriesValueMarkerAnnotationHelper(
                renderableSeries,
                isValidRenderableSeriesPredicate
            )
        ).apply {
            annotationLabels.add(AnnotationLabel(context).apply {
                labelPlacement = LabelPlacement.Axis
            })
        }
    }

    override fun destroyMarkerAnnotation() {
        markerAnnotation = null
    }
}

class HorizontalLineSeriesValueMarkerAnnotationHelper(
    renderableSeries: IRenderableSeries?,
    isValidRenderableSeriesPredicate: Predicate<IRenderableSeries>?
) : SeriesValueModifier.DefaultSeriesValueMarkerAnnotationHelper<HorizontalLineSeriesValueMarkerAnnotation>(
    renderableSeries,
    isValidRenderableSeriesPredicate
) {
    private val lineThickness = 1f.toDip()
    private val dashPattern = floatArrayOf(4f.toDip(), 4f.toDip())

    override fun updateAnnotation(
        annotation: HorizontalLineSeriesValueMarkerAnnotation?,
        lastValue: Comparable<*>?,
        lastColor: Int
    ) {
        super.updateAnnotation(annotation, lastValue, lastColor)

        Dispatcher.postOnUiThread{
            annotation?.let {
                it.stroke = SolidPenStyle(lastColor, false, lineThickness, dashPattern)
                it.annotationLabels.forEach { label ->
                    label.background = PaintDrawable(lastColor)
                    label.fontStyle = FontStyle(12f.toDip(), ColorUtil.getInvertedColor(lastColor))
                }
            }
        }
    }
}

class HorizontalLineSeriesValueMarkerAnnotation(
    context: Context,
    private val seriesValueHelper: SeriesValueModifier.DefaultSeriesValueMarkerAnnotationHelper<HorizontalLineSeriesValueMarkerAnnotation>
) : HorizontalLineAnnotation(context) {
    override fun attachTo(services: IServiceContainer?) {
        val renderableSeries = seriesValueHelper.renderableSeries

        xAxisId = renderableSeries.xAxisId
        yAxisId = renderableSeries.yAxisId

        super.attachTo(services)
    }

    override fun update(xAxis: IAxis?, yAxis: IAxis?) {
        seriesValueHelper.tryUpdateAnnotation(this)

        super.update(xAxis, yAxis)
    }
}
