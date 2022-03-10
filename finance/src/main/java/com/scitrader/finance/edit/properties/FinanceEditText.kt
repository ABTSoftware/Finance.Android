package com.scitrader.finance.edit.properties

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo

class FinanceEditText(
    context: Context,
    attrs: AttributeSet
): androidx.appcompat.widget.AppCompatEditText(context, attrs) {
    private var listener: ((Boolean, String) -> Unit)? = null

    fun setTextOnFocusChangeListener(listener: (Boolean, String) -> Unit) {
        this.listener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }
            false
        }

        setOnFocusChangeListener { _, hasFocus ->
            listener?.invoke(hasFocus, text.toString())
        }
    }

    override fun onDetachedFromWindow() {
        setOnEditorActionListener(null)
        onFocusChangeListener = null

        super.onDetachedFromWindow()
    }
}
