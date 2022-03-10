package com.scitrader.finance.pane.series

import androidx.annotation.StringRes
import com.scichart.charting.model.dataSeries.IDataSeries
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.charting.visuals.renderableSeries.hitTest.SeriesInfo
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IPaletteProvider
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip
import com.scichart.core.IServiceContainer
import com.scitrader.finance.R
import com.scitrader.finance.core.DependableBase
import com.scitrader.finance.core.IDataManager
import com.scitrader.finance.core.IDataManagerObserver
import com.scitrader.finance.edit.properties.OpacityEditableProperty
import com.scitrader.finance.pane.IPane
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.state.EditablePropertyState
import java.util.*

abstract class FinanceSeriesBase<TRenderableSeries : IRenderableSeries, TDataSeries : IDataSeries<Date, Double>>(
    @StringRes final override val name: Int,
    val renderableSeries: TRenderableSeries,
    val dataSeries: TDataSeries,
    yAxisId: AxisId
) : DependableBase(), IFinanceSeries, IDataManagerObserver {
    final override val viewType: Int = com.scitrader.finance.edit.annotations.PropertyType.FinanceSeries

    init {
        renderableSeries.dataSeries = dataSeries
        renderableSeries.yAxisId = yAxisId.toString()
    }

    final override var paletteProvider: IPaletteProvider?
        get() = renderableSeries.paletteProvider
        set(value) {
            renderableSeries.paletteProvider = value
        }

    final override var yAxisId: AxisId = yAxisId
        set(value) {
            field = value
            renderableSeries.yAxisId = value.toString()
        }

    @get:com.scitrader.finance.edit.annotations.EditableProperty
    val opacity = OpacityEditableProperty(
        R.string.financeSeriesOpacity,
        name,
        renderableSeries.opacity
    ) { id, value ->
        renderableSeries.opacity = value
        onPropertyChanged(id)
    }

    override fun attachTo(services: IServiceContainer?) {
        super.attachTo(services)

        services?.let {
            // add data manager so it can be used by FinancePaletteProvider
            renderableSeries.services.registerService(IDataManager::class.java, dataManager)
        }
    }

    override fun detach() {
        renderableSeries.services.deregisterService(IDataManager::class.java)

        super.detach()
    }

    override fun placeInto(pane: IPane) {
        pane.chart.renderableSeries.add(renderableSeries)
    }

    override fun removeFrom(pane: IPane) {
        pane.chart.renderableSeries.remove(renderableSeries)
    }

    override fun savePropertyStateTo(state: EditablePropertyState) {
        state.savePropertyValues(this)
    }

    override fun restorePropertyStateFrom(state: EditablePropertyState) {
        state.tryRestorePropertyValues(this)
    }

    override fun getSeriesInfo(): SeriesInfo<*> {
        return renderableSeries.seriesInfoProvider.seriesInfo
    }

    override fun getTooltip(): ISeriesTooltip {
        return renderableSeries.seriesInfoProvider.seriesTooltip
    }

    override fun getTooltip(modifierType: Class<*>): ISeriesTooltip {
        return renderableSeries.seriesInfoProvider.getSeriesTooltip(modifierType)
    }

    override fun reset() {
        opacity.reset()
    }
}
