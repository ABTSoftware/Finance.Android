package com.scitrader.finance.examples

data class Candlestick(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val symbol: String
)
