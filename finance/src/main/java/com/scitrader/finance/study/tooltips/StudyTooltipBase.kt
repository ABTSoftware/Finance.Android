package com.scitrader.finance.study.tooltips

import android.content.Context
import android.widget.TextView
import com.scichart.drawing.common.FontStyle
import com.scitrader.finance.study.IStudy
import com.scitrader.finance.utils.toDip

abstract class StudyTooltipBase<TStudy : IStudy>(
    context: Context,
    study: TStudy,
    titleFontStyle: FontStyle = com.scitrader.finance.pane.series.Constants.DefaultTooltipTitleFontStyle
) : LinearStudyTooltipBase<TStudy>(
    context,
    study
) {
    val titleTextView = TextView(context)

    init {
        with(titleTextView) {
            val padding = 3f.toDip().toInt()
            setPadding(padding, padding, padding, padding)
            typeface = titleFontStyle.typeface
            textSize = titleFontStyle.textSize
            setTextColor(titleFontStyle.textColor)

        }

        safeAdd(
            titleTextView, LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    override fun update() {
        updateTitleView(titleTextView)
    }

    protected open fun updateTitleView(titleTextView: TextView) {
        titleTextView.text = study.title
    }

    override fun update(x: Float, y: Float) {
        updateTitleView(titleTextView)
    }
}
