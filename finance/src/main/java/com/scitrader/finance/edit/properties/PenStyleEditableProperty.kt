package com.scitrader.finance.edit.properties

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.StringRes
import com.scichart.drawing.common.PenStyle
import com.scichart.drawing.common.SolidPenStyle
import com.scichart.drawing.utility.ColorUtil
import com.scitrader.finance.R
import com.scitrader.finance.edit.EditableViewHolder
import com.scitrader.finance.edit.IEditable
import com.scitrader.finance.state.PropertyState
import com.scitrader.finance.utils.DashedLineView
import com.scitrader.finance.utils.decimalInputType
import com.scitrader.finance.utils.toDip

enum class StrokeDashType(val pattern: FloatArray) {
    SolidLine(floatArrayOf(10f.toDip(), 0f)),
    DashMini(floatArrayOf(3f.toDip(), 3f.toDip())),
    DashMiddle(floatArrayOf(7f.toDip(), 7f.toDip())),
    DashLarge(floatArrayOf(15f.toDip(), 15f.toDip()));
}

open class PenStyleEditableProperty(
    @StringRes name: Int,
    @StringRes parentName: Int,
    initialValue: PenStyle,
    listener: (PropertyId, PenStyle) -> Unit
) : EditablePropertyBase<PenStyle>(name, parentName, com.scitrader.finance.edit.annotations.PropertyType.PenStyleProperty, initialValue, listener) {
    override fun getPropertyState(): PropertyState {
        return PropertyState().apply {
            when(val penStyle = value) {
                is SolidPenStyle -> {
                    writeString(VALUE_PROPERTY, SOLID_PEN)
                    writeInt(COLOR, penStyle.color)
                    writeFloat(THICKNESS, penStyle.thickness)
                    writeBool(AA, penStyle.antiAliasing)

                    penStyle.strokeDashArray?.let {
                        writeString(DASH, it.joinToString { it.toString() })
                    }
                }

                // TODO
                else -> throw UnsupportedOperationException("TODO")
            }

        }
    }

    override fun setPropertyState(propertyValue: PropertyState) {
        propertyValue.readString(VALUE_PROPERTY)?.let { propertyType ->
            when(propertyType) {
                SOLID_PEN -> {
                    val color = propertyValue.readInt(COLOR)!!
                    val thickness = propertyValue.readFloat(THICKNESS)!!
                    val antiAliasing = propertyValue.readBool(AA)!!

                    val dash = propertyValue.readString(DASH)
                            ?.split(',')?.map { value -> value.toFloat() }?.toFloatArray()

                    trySetValue(SolidPenStyle(color, antiAliasing, thickness, dash))
                }

                // TODO
                else -> throw UnsupportedOperationException("TODO")
            }
        }
    }

    override fun isValidValue(value: PenStyle): Result {
        return if (value.thickness > 0) {
            Result.Success
        } else {
            Result.Fail("Expected value greater then 0")
        }
    }

    companion object {
        const val SOLID_PEN = "Solid"
        const val COLOR = "color"
        const val THICKNESS = "thickness"
        const val AA = "antiAliasing"
        const val DASH = "dash"
    }
}

class PenStyleEditablePropertyViewHolder(view: View) : EditableViewHolder(view) {
    private val titleView = view.findViewById<TextView>(R.id.title)
    private val colorPicker = view.findViewById<PropertyColorPicker>(R.id.propertyColorPicker)
    private val strokeDashPicker = view.findViewById<Spinner>(R.id.strokeDashPicker)
    private val strokeThickness = view.findViewById<FinanceEditText>(R.id.strokeThickness)

    private var property : PenStyleEditableProperty? = null

    init {
        colorPicker.selectColorListener = { color ->
            property?.let{ p ->
                val currentValue = p.value
                p.trySetValue(
                    SolidPenStyle(
                        color,
                        true,
                        currentValue.thickness,
                        currentValue.strokeDashArray
                    )
                )
            }
        }

        strokeDashPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.let{
                    val item = when (position) {
                        0 -> StrokeDashType.SolidLine
                        1 -> StrokeDashType.DashMini
                        2 -> StrokeDashType.DashMiddle
                        3 -> StrokeDashType.DashLarge
                        else -> StrokeDashType.SolidLine
                    }

                    property?.let{ p ->
                        val currentValue = p.value

                        p.trySetValue(
                            SolidPenStyle(
                                currentValue.color,
                                true,
                                currentValue.thickness,
                                item.pattern
                            )
                        )
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        strokeThickness.setTextOnFocusChangeListener() { hasFocus, text ->
            if (!hasFocus) {
                text.toFloatOrNull()?.let { strokeThickness ->
                    property?.let {
                        val result = it.trySetValue(
                            SolidPenStyle(
                                it.value.color,
                                true,
                                strokeThickness,
                                it.value.strokeDashArray
                            )
                        )

                        if (result is Result.Fail) {
                            showErrorResultToast(result)
                        }
                    }
                } ?: showErrorResultToast(Result.Fail("Invalid input"))

                updateThicknessView()
            }
        }
    }

    override fun bindItem(item: IEditable) {
        if (item is PenStyleEditableProperty) {
            property = item
            titleView.text = titleView.context.getText(item.name)

            colorPicker.setColor(item.value.color)

            strokeDashPicker.adapter = StrokeDashSpinnerArrayAdapter(
                colorPicker.context,
                StrokeDashType.values()
            )

            val dash = item.value.strokeDashArray
            strokeDashPicker.setSelection(
                when {
                    dash.contentEquals(StrokeDashType.SolidLine.pattern) -> 0
                    dash.contentEquals(StrokeDashType.DashMini.pattern) -> 1
                    dash.contentEquals(StrokeDashType.DashMiddle.pattern) -> 2
                    dash.contentEquals(StrokeDashType.DashLarge.pattern) -> 3
                    else -> 0
                }
            )

            strokeThickness.inputType = decimalInputType
            updateThicknessView()
        } else
            property =null
    }

    private fun updateThicknessView() {
        strokeThickness.text = SpannableStringBuilder().append(property?.value?.thickness.toString())
    }
}

class StrokeDashSpinnerArrayAdapter(context: Context, objects: Array<out StrokeDashType>) :
    ArrayAdapter<StrokeDashType>(context, R.layout.stroke_dash_spinner_item, objects) {

    private val dashValues = StrokeDashType.values()

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getViewInternal(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getViewInternal(position, convertView, parent)
    }

    private fun getViewInternal(position: Int, convertView: View?, parent: ViewGroup): View {
        val dashType = dashValues[position]

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.stroke_dash_spinner_item, parent, false)

        view.findViewById<DashedLineView>(R.id.strokeDashView)?.let{
            it.setDash(dashType.pattern)
            it.setStrokeColor(ColorUtil.White)
            it.setStrokeThickness(2f.toDip())
        }

        return view
    }
}
