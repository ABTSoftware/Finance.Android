package com.scitrader.finance.examples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.scichart.charting.visuals.SciChartSurface
import com.scitrader.finance.SciFinanceChart
import com.scitrader.finance.data.DefaultCandleDataProvider
import com.scitrader.finance.data.ICandleDataProvider
import com.scitrader.finance.pane.PaneId
import com.scitrader.finance.study.studies.PriceSeriesStudy
import com.scitrader.finance.study.studies.RSIStudy

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        SciChartSurface.setRuntimeLicenseKey("")

        val chart = findViewById<SciFinanceChart>(R.id.financeChart)
        val candleDataProvider = DefaultCandleDataProvider()
        chart.candleDataProvider = candleDataProvider
        fillDataProvider(candleDataProvider, DataManager.getCandles())

        chart.studies.add(PriceSeriesStudy(PaneId.DEFAULT_PANE))
        chart.studies.add(RSIStudy(PaneId.uniqueId("RSI")))
        chart.isCursorEnabled = true
    }

    private fun fillDataProvider(dataProvider: ICandleDataProvider, candles: List<Candlestick>) {
        for (candlestick in candles) {
            dataProvider.xValues.addTime(candlestick.openTime)
            dataProvider.openValues.add(candlestick.open)
            dataProvider.highValues.add(candlestick.high)
            dataProvider.lowValues.add(candlestick.low)
            dataProvider.closeValues.add(candlestick.close)
            dataProvider.volumeValues.add(candlestick.volume)
        }
    }
}
