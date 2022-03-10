package com.scitrader.finance.edit

import androidx.annotation.StringRes

interface IEditable {
    val viewType: Int

    @get:StringRes
    val name: Int

    fun reset()
}
