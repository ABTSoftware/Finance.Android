package com.scitrader.finance.edit.properties

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.scichart.drawing.utility.ColorUtil
import com.scitrader.finance.R
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class PropertyColorPicker(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val colorView : View

    var selectColorListener: ((Int)->Unit)? = null

    private var selectedColor: Int = Color.WHITE
        set(value) {
            field = value

            background.setColor(value)
            selectColorListener?.let { it(value) }
        }

    private val background: GradientDrawable
        get() {
            return colorView.background as GradientDrawable
        }

    fun setColor(@ColorInt color: Int) {
        selectedColor = color
    }

    init {
        setOnClickListener { show() }

        LayoutInflater.from(context).inflate(R.layout.color_picker, this, true)

        colorView = findViewById<View>(R.id.colorView)
    }

    private fun show() {
        val dialogBuilder = ColorPickerDialog.Builder(context, R.style.SciChartColorPickerDialogTheme)
            .setPositiveButton("Confirm",
                ColorEnvelopeListener { envelope, _ ->
                    selectedColor = envelope.color
                }
            )
            .setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)

        val bubbleFlag = BubbleFlag(context)
        bubbleFlag.flagMode = FlagMode.ALWAYS
        dialogBuilder.colorPickerView.flagView = bubbleFlag;

        dialogBuilder.colorPickerView.setInitialColor(selectedColor)
        dialogBuilder.create().also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ColorUtil.White);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ColorUtil.White);
            }
            dialog.show()
        }
    }
}
