package com.scitrader.finance.pane.series

import com.scichart.charting.visuals.renderableSeries.hitTest.SeriesInfo
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IPaletteProvider
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip
import com.scichart.core.framework.IAttachable
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.pane.IPanePlaceable
import com.scitrader.finance.pane.axes.AxisId
import com.scitrader.finance.state.IEditablePropertyContainer

interface IFinanceSeries : IAttachable, IEditable, IPanePlaceable, IEditablePropertyContainer {
    var paletteProvider: IPaletteProvider?
    var yAxisId: AxisId

    fun getSeriesInfo(): SeriesInfo<*>
    fun getTooltip(): ISeriesTooltip
    fun getTooltip(modifierType: Class<*>): ISeriesTooltip
}
