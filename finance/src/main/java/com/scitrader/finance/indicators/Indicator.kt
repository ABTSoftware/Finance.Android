package com.scitrader.finance.indicators

enum class Indicator {
    MACD,
    SMA,
    BBANDS,
    RSI,
    ADX,
    ATR,
    CCI,
    EMA,
    OBV,
    SAR,
    STDDEV,
    STOCH,
    HT_TRENDLINE,
    VOLUME_PROFILE;

    override fun toString(): String {
        return when (this) {
            MACD -> "${name} / Moving Average Convergence/Divergence"
            SMA -> "${name} / Simple Moving Average"
            BBANDS -> "${name} / Bollinger Bands"
            RSI -> "${name} / Relative Strength Index"
            ADX -> "${name} / Average Directional Movement Index"
            ATR -> "${name} / Average True Range"
            CCI -> "${name} / Commodity Channel Index"
            EMA -> "${name} / Exponential Moving Average"
            OBV -> "${name} / On Balance Volume"
            SAR -> "${name} / Parabolic SAR"
            STDDEV -> "${name} / Standard Deviation"
            STOCH -> "${name} / Stochastic"
            HT_TRENDLINE -> "${name} / Hilbert Transform - Instantaneous Trendline"
            VOLUME_PROFILE -> "${name} / Volume Profile Indicator"
        }
    }

    val needsSeparatePane: Boolean
    get() {
        return when (this) {
            SMA, BBANDS, EMA, SAR, HT_TRENDLINE, VOLUME_PROFILE -> false
            else -> true
        }
    }
}
