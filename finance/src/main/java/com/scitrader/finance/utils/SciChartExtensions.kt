package com.scitrader.finance.utils

import android.content.res.Resources
import android.text.InputType
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.scichart.charting.model.dataSeries.OhlcDataSeries
import com.scichart.charting.model.dataSeries.UniformHeatmapDataSeries
import com.scichart.charting.model.dataSeries.XyDataSeries
import com.scichart.charting.model.dataSeries.XyyDataSeries
import com.scichart.core.IServiceContainer
import com.scichart.core.framework.IAttachable
import com.scichart.drawing.common.SolidPenStyle
import com.scichart.drawing.utility.ColorUtil
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.round

inline fun <reified TX : Comparable<TX>, reified TY : Comparable<TY>> XyDataSeries(): XyDataSeries<TX, TY> {
    return XyDataSeries(TX::class.javaObjectType, TY::class.javaObjectType)
}

inline fun <reified TX : Comparable<TX>, reified TY : Comparable<TY>> XyyDataSeries(): XyyDataSeries<TX, TY> {
    return XyyDataSeries(TX::class.javaObjectType, TY::class.javaObjectType)
}

inline fun <reified TX : Comparable<TX>, reified TY : Comparable<TY>> OhlcDataSeries(): OhlcDataSeries<TX, TY> {
    return OhlcDataSeries(TX::class.javaObjectType, TY::class.javaObjectType)
}

inline fun <reified TX : Comparable<TX>, reified TY : Comparable<TY>, reified TZ : Comparable<TZ>> UniformHeatmapDataSeries(
    xSize: Int,
    ySize: Int
): UniformHeatmapDataSeries<TX, TY, TZ> {
    return UniformHeatmapDataSeries(TX::class.javaObjectType, TY::class.javaObjectType, TZ::class.javaObjectType, xSize, ySize)
}

fun SolidPenStyle(@ColorInt color: Int, thickness: Float): SolidPenStyle {
    return SolidPenStyle(color, true, thickness.toDip(), null)
}

fun Float.toDip(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
}

fun Float.toSp(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)
}

fun List<IAttachable>.attachTo(services: IServiceContainer) {
    for (item in this) {
        item.attachTo(services)
    }
}

fun List<IAttachable>.detach() {
    for (item in this) {
        item.detach()
    }
}

fun opaqueColor(color: Int) : Int {
    val red = ColorUtil.red(color)
    val blue = ColorUtil.blue(color)
    val green = ColorUtil.green(color)
    return ColorUtil.rgb(red, green, blue)
}

private val priceFormat = DecimalFormat()

fun Double.formattedPrice(format: String) : String {
    priceFormat.applyPattern(format)

    return priceFormat.format(this)
}

val Double.fractionZerosCount : Int
get() {
    if (this == 0.0) return 0

    return max(-floor(ln(this) / ln(10.0) + 1.0), 0.0).toInt()
}

val decimalInputType : Int
    get() = InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}
