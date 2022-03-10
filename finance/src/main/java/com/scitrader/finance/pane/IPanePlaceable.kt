package com.scitrader.finance.pane

interface IPanePlaceable {
    fun placeInto(pane: IPane)
    fun removeFrom(pane: IPane)
}
