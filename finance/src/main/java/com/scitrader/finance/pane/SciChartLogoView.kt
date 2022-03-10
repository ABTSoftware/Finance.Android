package com.scitrader.finance.pane

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.scitrader.finance.R

class SciChartLogoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val logoWord: View
    init {
        layoutTransition = LayoutTransition()

        LayoutInflater.from(context).inflate(R.layout.scichart_logo_layout, this, true)

        logoWord = findViewById<View>(R.id.scichart_word)

        setOnClickListener {
            logoWord.visibility = if(logoWord.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }
}
