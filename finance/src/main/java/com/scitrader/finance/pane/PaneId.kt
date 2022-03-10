package com.scitrader.finance.pane

data class PaneId(val id: String) {
    companion object {
        val DEFAULT_PANE = PaneId("DefaultPane")

        fun uniqueId(name: String) : PaneId {
            return PaneId("$name${System.currentTimeMillis()}")
        }
    }
}
