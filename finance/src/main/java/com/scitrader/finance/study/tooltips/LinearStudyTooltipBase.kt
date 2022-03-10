package com.scitrader.finance.study.tooltips

import android.content.Context
import android.view.ViewGroup
import com.scichart.charting.visuals.layout.LinearViewContainer
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.charting.visuals.renderableSeries.hitTest.HitTestInfo
import com.scichart.charting.visuals.renderableSeries.hitTest.IHitTestInfoUpdatable
import com.scichart.core.framework.IViewContainer
import com.scitrader.finance.study.IStudy
import com.scitrader.finance.study.StudyId
import com.scitrader.finance.utils.toDip

abstract class LinearStudyTooltipBase<TStudy : IStudy>(
    context: Context, protected val study: TStudy
) : LinearViewContainer(context), IStudyTooltip {

    var useInterpolation: Boolean = false

    private val hitTestInfo = HitTestInfo()

    init {
        orientation = VERTICAL
    }

    final override val studyId: StudyId
        get() = study.id

    override var showSeriesTooltips: Boolean = false
        set(value) {
            if(field == value) return

            field = value
            onShowSeriesTooltipsChanged(value)
        }

    protected open fun onShowSeriesTooltipsChanged(showSeriesTooltips: Boolean) {

    }

    /**
     * {@inheritDoc}
     */
    override fun placeInto(viewContainer: IViewContainer) {
        viewContainer.safeAdd(this)

        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.leftMargin = 6f.toDip().toInt()
        this.layoutParams = params
    }

    /**
     * {@inheritDoc}
     */
    override fun removeFrom(viewContainer: IViewContainer) {
        viewContainer.safeRemove(this)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    protected fun IHitTestInfoUpdatable.tryUpdate(x: Float, y: Float): Boolean {
        val rs = this.renderableSeries
        if (isSeriesValid(rs)) {
            // need to prevent updates of data series and render pass data during hit test and update of series info
            val dataSeriesLock = rs.dataSeriesLock
            val renderPassDataLock = rs.renderPassDataLock

            // SC_DROID-391: need to lock in this specific order to prevent deadlock
            dataSeriesLock.readLock()
            renderPassDataLock.readLock()
            try {
                updateHitTestInfo(hitTestInfo, rs, x, y)
                if (isHitPointValid(hitTestInfo)) {
                    this.update(hitTestInfo, useInterpolation)
                    return true
                }
            } finally {
                renderPassDataLock.readUnlock()
                dataSeriesLock.readUnlock()
            }
        }
        return false
    }

    protected open fun updateHitTestInfo(
        hitTestInfo: HitTestInfo,
        rs: IRenderableSeries,
        x: Float,
        y: Float
    ) {
        rs.verticalSliceHitTest(hitTestInfo, x, y)
    }

    protected open fun isHitPointValid(hitTestInfo: HitTestInfo): Boolean {
        val isHitTestPointValid = !hitTestInfo.isEmpty && hitTestInfo.isHit

        // if need to perform interpolation we need hit test point to be within data bounds
        return if (useInterpolation) isHitTestPointValid && hitTestInfo.isWithinDataBounds else isHitTestPointValid
    }

    protected open fun isSeriesValid(series: IRenderableSeries?): Boolean {
        return series != null && series.hasDataSeries()
    }
}
