package com.scitrader.finance.pane.modifiers

import android.view.MotionEvent
import com.scichart.charting.modifiers.GestureModifierBase

class DoubleTapGestureModifier(
    private val doubleTapAction: (() -> Unit)
): GestureModifierBase() {
    override fun onDoubleTap(e: MotionEvent): Boolean {
        doubleTapAction()

        return super.onDoubleTap(e)
    }
}
