package com.scitrader.finance.financeChart

import android.content.Context
import android.util.AttributeSet
import com.scichart.charting.visuals.SciChartSurface
import com.scichart.drawing.canvas.RenderSurface
import com.scichart.drawing.common.IRenderSurface

open class CanvasSciChartSurface @JvmOverloads constructor(
    context: Context, attrs:
    AttributeSet? = null,
    defStyleAttr: Int = 0
) : SciChartSurface(context, attrs, defStyleAttr) {
    override fun getDefaultRenderSurface(context: Context): IRenderSurface {
        return RenderSurface(context)
    }
}
