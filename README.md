# Finance Charts Android SDK

**Finance Charts SDK** adds professional-grade financial charting capabilities for native iOS & Android apps. 

It is built on top of the [SciChart](https://www.scichart.com/) – an award-winning, high-performance chart SDK for iOS, Android, Windows, and JavaScript apps. 

Our SDK library may be licensed for inclusion in your trading apps, exchange apps, or investment/brokerage apps, with pricing on the application.

<img alt="Android Finance SDK" src="https://user-images.githubusercontent.com/18321399/162439690-f6c7cad9-235e-41ac-8e8c-45a6040f77b9.png" alt="drawing" width="500"/>

> **_NOTE_:** To see what could be done with Finance and SciChart, please try our [**SciTrader**](https://www.scitrader.io) [Android](https://play.google.com/store/apps/details?id=com.scitrader) and [iOS](https://apps.apple.com/gb/app/scitrader/id1584140348) app.

## What is the Android Finance SDK?

* Native **Android (Kotlin)** Financial Chart Library
* **Build Trading Apps**, Exchange Apps, or Investment/Brokerage Apps
* A single View or Control to display a **multi-pane stock or financial chart**
* Built for CryptoCurrency markets but also suitable for Equities, FX
* Support for **20 built-in Technical Indicators** (MACD, RSI, Moving Average, etc)
* Automatic Management of Panels
* Interactive **Cursors** and **Legends**
* **Auto-fit** zoom or manual fit data
* Data management and **reactive updates from exchange**
* **Resizing** of panels
* Rich, Touch **Zoom, Pan interaction**
* **Ultra High performance**: using gaming technology, achieve fast & smooth charts even on low-end mobile and desktop devices
* **Theming, styling**
* **Whitelabel** the Android Finance SDK into your app

## Table of Contents
1. [Integration](#integrating-finance-sdk-manually)
    1. [Manual](#integrating-finance-sdk-manually)
    2. [Maven](#integrating-finance-sdk-using-maven)
2. [QuickStart](#quickstart)
3. [Studies](#studies)
4. [FinanceSeries](#finance-series)
5. [Panes](#panes)
6. [DataProvider and DataManager](#data-provider-and-data-manager)

## Integrating Finance SDK Manually
Please follow these steps to integrate Android Finance SDK:

1. Clone the [Finance.Android repo](https://github.com/ABTSoftware/Finance.Android)
2. In your project **File/New/Import module** and select **finance** folder from in the Finance.Android cloned repo.
3. Add SciChart maven URL.
> **_NOTE_:** Finance SDK is built on top of the [SciChart](https://www.scichart.com/) library. It means you also need to get SciChart lib dependency. 

Add maven URL to your **repositories** in the **settings:
```gradle
maven { url 'https://www.myget.org/F/abtsoftware/maven' }
```
> **_NOTE_:** To integrate SciChart manually, please follow the [corresponding article](https://www.scichart.com/documentation/android/latest/articles/UserManual/Integrating%20SciChart%20Libraries.html) in the [SciChart documentation](https://www.scichart.com/documentation/android/latest/).

Also, make sure, the line `include ':finance'` is added in the same file.

4. Add `implementation project(path: ":finance")` to the **dependencies** in your module **build.gradle** file.
5. Sync project. 

## Integrating Finance SDK Using Maven
1. Add `maven { url 'https://www.myget.org/F/abtsoftware/maven' }` to your **repositories** in the **settings.gradle** file
2. Add **Finance** and **SciChart** implementations in your module **build.gradle** file.
```maven
// Declare Finance as module dependencies
implementation project(path: ":finance")

// Declare SciChart libraries as module dependencies
def sciChartVersion = '4.3.0.4686'
api(group: 'com.scichart.library', name: 'core', version: "$sciChartVersion", ext: 'aar')
api(group: 'com.scichart.library', name: 'data', version: "$sciChartVersion", ext: 'aar')
api(group: 'com.scichart.library', name: 'drawing', version: "$sciChartVersion", ext: 'aar')
api(group: 'com.scichart.library', name: 'charting', version: "$sciChartVersion", ext: 'aar')
```
> **_NOTE_:** Finance SDK is built on top of the [SciChart](https://www.scichart.com/) library. It means you also need to get SciChart lib dependency.

3. Sync project. 

## Quickstart

> **_NOTE_:** Finance SDK is built on top of the [SciChart](https://www.scichart.com/) library. To use it, you must have an active SciChart license. For more details, please, follow the [SciChart Android Licensing](https://www.scichart.com/licensing-scichart-android/) page.

Finance SDK allows you to create a full-blown finance chart app with a few lines of code.

The central place of the Finance SDK is the `SciFinanceChart`. It holds [studies](#studies), [panes](#panes), [data provider](#data-provider-and-data-manager), [chart modifiers](#pane-factory-and-chart-modifiers), such as **cursor**, **legend** etc.

Let's see how you can create a chart just in a few steps:
1. Add a **SciFinanceChart** view to your .xml file:
```xml
<com.scitrader.finance.SciFinanceChart
    android:id="@+id/financeChart"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```
2. Create DataProvider.
```kotlin
val candleDataProvider = DefaultCandleDataProvider()
chart.candleDataProvider = candleDataProvider

// fill the dataProvider with your candlestick data
fillDataProvider(candleDataProvider, StubDataManager.getCandles())
```

> **_NOTE_:** Please follow the [DataProvider and DataManager](#data-provider-and-data-manager) section for more details.

3. Create Studies.
**Study** is the object that is responsible for charting business logic. Here you decide what your chart should display and how it should be visualized - as a candlestick, line, column, mountain, etc.

As an example, let's add two studies - **PriceSeriesStudy** to show candlesticks and **RSIStudy** to show RSI(Relative Strength Index) indicator:
```kotlin
val priceSeriesStudy = PriceSeriesStudy(PaneId.DEFAULT_PANE)
val rsiStudy = RSIStudy(PaneId.uniqueId("RSI"))
        
chart.studies.add(priceSeriesStudy)
chart.studies.add(rsiStudy)
```
> **_NOTE:_** Creating our priceSeriesStudy with a `PaneId.DEFAULT_PANE` **pane id** means that we want to place our study on the main pane. 
> If you want it to be a separate pane, create your study with some **unique id**, as we did with the **rsiStudy**. 
> Please follow the [Pane](#panes) article for more details.

> **_NOTE:_** Of course, you can change colors, indicator inputs, and everything else. Please follow the [Modify Study Properties](#modify-study-properties) section for more details.

4. Also, let's enable a **cursor**:
```kotlin
chart.isCursorEnabled = true
```
> **_NOTE:_** By default, the chart has already a few gesture modifiers, like Zoom, Pan, Y-Axis drag, etc. Please follow the [Chart Modifiers](#pane-factory-and-chart-modifiers) section for more details.

5. Enter the license key.
> **_NOTE:_** As mentioned above, Finance SDK is built on top of the [SciChart](https://www.scichart.com/) library. To see a chart, you must have an active SciChart license. For more details, please, follow the [SciChart Android Licensing](https://www.scichart.com/licensing-scichart-android/) page.

After you obtain your license key, use it in your project, like this:
```kotlin
SciChartSurface.setRuntimeLicenseKey("YOUR_LICENSE_KEY")
```
> **_NOTE:_** Please, follow the [Applying the Runtime License in Your App](https://www.scichart.com/licensing-scichart-android/) article for more details.

That's it. You've just created a finance chart with candles, indicators, legend, modifiers, resizing, and saved tons of development time.

![Finance SDK Demo](https://user-images.githubusercontent.com/18321399/163128600-5b62ce46-e325-49c1-a80c-b7b1f0ecbf80.gif)

## Studies

**Study** is the object that is responsible for charting business logic. Here you decide what your chart should display and how it should be visualized - as a candlestick, line, column, mountain, etc.

### Modify Study Properties

Each of our built-in studies has editable properties, depending on the [Finance Series](#finance-series) type and indicators included in that study. These editable properties are annotated with the `@EditableProperty` annotation to allow you to collect all of them in one place, for example, for a study settings view.

> **_NOTE:_** To see it in action, please try our [SciTrader Android](https://play.google.com/store/apps/details?id=com.scitrader) app.

As an example, let's take our `RSIStudy` and try to modify its properties. It has two of them - `RSIIndicator` and `LineFinanceSeries`:
1. `RSIIndicator` has also two properties: **period** and **input**.
By default, **period == 14** and **input == "close"** which means that we want to calulate our RSI based on **close** prices from our candlesticks.
Let's change our period to 50 and input to "open". It will produce the following result:

| period == 14, input == "close"    | period == 50, input == "open"   |
| --------------------------------- | ------------------------------- |
| ![RSI-14-close](https://user-images.githubusercontent.com/18321399/162398838-a4e66dd2-c0bf-48b1-b6b5-c4155e89b344.png) | ![RSI-50-open](https://user-images.githubusercontent.com/18321399/162398844-8a9f7797-4870-405d-924e-e7393f51384a.png) | 

2. `LineFinanceSeries` has its own editable properties: **opacity** and **strokeStyle** with **color**, **antiAliasing**, **thickness** and **strokeDashArray**.

> **_NOTE:_** You can find more about `PenStyle`, `BrushStyle` and `FontStyle` in the [SciChart Android Documentation](https://www.scichart.com/documentation/android/latest/articles/chart2d/stylingAndTheming/PenStyle%2C%20BrushStyle%20and%20FontStyle.html)

> **_NOTE:_** All of these properties have some default values to have a nice chart out-of-the-box. Feel free to edit them to match your design requirements. 

After some changes, your `RSIStudy` might look, like this:

| color == Constants.DefaultBlue<br>thickness == 5.5f<br>strokeDashArray == null | color == Constants.DefaultRed<br>thickness == 5.5f<br>strokeDashArray == floatArrayOf(3f.toDip(), 3f.toDip()) |
| -------- | ----------- |
| ![blue-4-dash-null](https://user-images.githubusercontent.com/18321399/162400382-6ecb5234-067c-4c0a-b55a-81e55535bb05.png) | ![red-4-dash20-20](https://user-images.githubusercontent.com/18321399/162400506-7c9a788c-b1f8-4448-a46a-122a20942a0b.png) |

### Built-in Studies

Finance SDK contains 14 built-in studies, available out of the box. There is on special `PriceSeriesStudy` that displays candlesticks and volume bars. Others represent technical indicators from the world's most famous [TA-Lib (Technical Analysis) Library](https://ta-lib.org). Here are all of them with some images:

| PriceSeriesStudy | ADXStudy |
| -------- | ----------- |
| ![PriceSeriesStudy](https://user-images.githubusercontent.com/18321399/162404367-0159ad45-01f6-4ce8-aa74-d176c9f01eaa.png) | ![ADXStudy](https://user-images.githubusercontent.com/18321399/162404343-302436d8-56e4-42c1-980a-3952cf3b0aff.png) |

| ATRStudy | BBandsStudy |
| -------- | ----------- |
| ![ATRStudy](https://user-images.githubusercontent.com/18321399/162404352-de024249-8365-485f-9048-9b4540c5d2d9.png) | ![BBandsStudy](https://user-images.githubusercontent.com/18321399/162404356-bbd6a61a-f051-4d26-986c-5da511e1449a.png) |

| CCIStudy | EMAStudy |
| -------- | ----------- |
| ![CCIStudy](https://user-images.githubusercontent.com/18321399/162404359-602f1910-13b3-43ad-b256-13dcfab188b2.png) | ![EMAStudy](https://user-images.githubusercontent.com/18321399/162404362-db2ef2c9-97d6-4251-861c-1b60b56b3130.png) |

| HT_TrendlineStudy | MacdStudy |
| -------- | ----------- |
| ![HT_TrendlineStudy](https://user-images.githubusercontent.com/18321399/162404379-8d12f65c-4e2a-48ca-a8d6-a3b89ef5ee2f.png) | ![MacdStudy](https://user-images.githubusercontent.com/18321399/162404364-ce8287c6-58c6-4fa5-a293-49449e8c22e0.png) |

| OBVStudy | RSIStudy |
| -------- | ----------- |
| ![OBVStudy](https://user-images.githubusercontent.com/18321399/162404366-f269a290-8c0e-49ee-87a0-b9f5eca9be24.png) | ![RSIStudy](https://user-images.githubusercontent.com/18321399/162404369-cf37d43e-b5e0-4a79-aa9d-665a2ff6dd77.png) |

| SARStudy | SMAStudy |
| -------- | ----------- |
| ![SARStudy](https://user-images.githubusercontent.com/18321399/162404370-91e9aea0-5c7a-48a4-bc0a-1458978554dc.png) | ![SMAStudy](https://user-images.githubusercontent.com/18321399/162404371-d041a5eb-bd9f-4b87-853b-b422799c792d.png) |

| STDDevStudy | StochStudy |
| -------- | ----------- |
| ![STDDevStudy](https://user-images.githubusercontent.com/18321399/162404374-2d3fd37f-34da-4ad8-99e4-90b7b128347b.png) | ![StochStudy](https://user-images.githubusercontent.com/18321399/162404376-9cfe27af-e259-4891-b458-8700b8a7bcf1.png) |

## Finance Series

There are a few built-in Finance Series types that are responsible for a visual representation of your data on the chart. 

> **_NOTE:_** Finance Series corresponds to the SciChart RenderableSeries. For more details, please read the [RenderableSeries API](https://www.scichart.com/documentation/ios/current/2D%20Chart%20Types.html) article in the [SciChart Android documentation](https://www.scichart.com/documentation/android/latest/)

Each of them has its editable properties, like **stroke**, **fill**, **opacity**, etc. Also, it has its tooltip and Info provider, which is responsible for providing data for your tooltips, legend, etc. 

> **_NOTE:_** For more info, please refer to the [Tooltips Customization](https://www.scichart.com/documentation/android/latest/articles/chart2d/chartModifierAPIs/interactivity/Tooltips%20Customization.html?tabs=java) documentation.

Each of our [Studies](#studies) uses one or few Finance Series to visualize prices or TA-lib indicators outputs. Here you can see their usage:

| CandlestickFinanceSeries and ColumnFinanceSeries | BandFinanceSeries |
| -------- | ----------- |
| ![CandlestickFinanceSeries](https://user-images.githubusercontent.com/18321399/162404367-0159ad45-01f6-4ce8-aa74-d176c9f01eaa.png) | ![BandFinanceSeries](https://user-images.githubusercontent.com/18321399/162404356-bbd6a61a-f051-4d26-986c-5da511e1449a.png) |

| LineFinanceSeries | HistogramFinanceSeries |
| -------- | ----------- |
| ![LineFinanceSeries](https://user-images.githubusercontent.com/18321399/162404369-cf37d43e-b5e0-4a79-aa9d-665a2ff6dd77.png) | ![HistogramFinanceSeries](https://user-images.githubusercontent.com/18321399/162404364-ce8287c6-58c6-4fa5-a293-49449e8c22e0.png) |

> **_NOTE:_** Please refer to [Modify Study Properties]() section for more details.

## Panes

**Pane** is an object that holds a chart, legend, and all other subviews that you might want to place on top of the chart. Also, it is responsible for chart resizing.

#### Pane Factory and Chart Modifiers
`SciFinanceChart` has an `IPaneFactory` object, responsible for creating panes. There is one concrete implementation, called `DefaultPaneFactory` which creates two types of panes - `DefaultPane` and `MainPane`. This factory is responsible for creating **Chart modifiers** - objects, that can be added to a chart to give it a certain behavior, like zooming, panning operations, tooltips, legends, selection of points or lines, etc. There are many different ChartModifiers provided by SciChart, so you might wanna take a look at the [Chart Modifier APIs](https://www.scichart.com/documentation/android/latest/articles/chart2d/chartModifierAPIs/Chart%20Modifier%20APIs.html) article in the [SciChart Android Documentation](https://www.scichart.com/documentation/android/latest/index.html) for more details.

Here you can take a quick look at how they work:

| ZoomPanModifier | PinchZoomModifier |
| -- | -- |
| ![ZoomPanModifier](https://user-images.githubusercontent.com/18321399/163135651-a8e1bae1-e520-4861-939c-c5395b087e81.gif) | ![PinchZoomModifier](https://user-images.githubusercontent.com/18321399/163135630-a8aa1d3c-748e-445b-9257-e4bb5eea8d03.gif)  |

| SeriesValueModifier | DoubleTapGestureModifier |
| -- | -- |
| ![SeriesValueModifier](https://user-images.githubusercontent.com/18321399/163135638-c46dbd48-b230-463d-99da-adb6fb93cde0.gif) | ![DoubleTapGestureModifier](https://user-images.githubusercontent.com/18321399/163135622-c6ee527a-6138-497e-8b34-82ea65369d8b.gif) |

| YAxisDragModifier | XRangeModifier |
| -- | -- |
| ![YAxisDragModifier](https://user-images.githubusercontent.com/18321399/163135647-b3978ab0-6b26-4141-a5ea-1648b8aaef37.gif) | ![XRangeModifier](https://user-images.githubusercontent.com/18321399/163135645-af9b582b-1a6a-4de0-9bf7-4e7cb62c3b8f.gif) |

| CrosshairModifier | StudyLegend |
| -- | -- |
| ![CrosshairModifier](https://user-images.githubusercontent.com/18321399/163135596-512742e2-f122-4021-97b4-b1ade3ffcd6e.gif) | ![StudyLegend](https://user-images.githubusercontent.com/18321399/163135642-c04bb4e0-3cdf-4275-b4ff-c4bf89afdd53.gif) |

### Panes Resizing
Our built-in panes know how to resize. Assuming, you have more than one pane, you can tap between panes and drag to resize:

![PaneResizing](https://user-images.githubusercontent.com/18321399/163135626-3d82ca2a-0557-4911-8541-cae738da266d.gif)

## Data Provider and Data Manager
`DefaultCandleDataProvider` and `DataManager` are objects that simplify data manipulation in your app. 

`DataManager` is responsible for storing data and accessing it by the `DataSourceId`. First, you register your **ID**, then put some values under it. Later, you can access those values where needed. 

`DefaultCandleDataProvider` is responsible for adding candlesticks. It has **xValues**, **openValues**, **highValues**, **lowValues**, **closeValues** and **volumeValues** along with corresponding **id's** that you can use to get access to those values later.

Let's take a look, at how the data flow works on the `RSIStudy` example. 

1. At the beginning, create a `DefaultCandleDataProvider` instance and set it to `SciFinanceChart.candleDataProvider` property. `DefaultCandleDataProvider` registers all candlesticks IDs in the `DataManager` and stores corresponding values under the hood.
```kotlin
val chart = findViewById<SciFinanceChart>(R.id.financeChart)
val candleDataProvider = DefaultCandleDataProvider()
chart.candleDataProvider = candleDataProvider
```

2. Then, use **dataProvider** to fill the data manager with some candles:
```kotlin
// Assume you get some candles from a server
for (candlestick in candles) {
    dataProvider.xValues.addTime(candlestick.openTime)
    dataProvider.openValues.add(candlestick.open)
    dataProvider.highValues.add(candlestick.high)
    dataProvider.lowValues.add(candlestick.low)
    dataProvider.closeValues.add(candlestick.close)
    dataProvider.volumeValues.add(candlestick.volume)
}
```

3. When we create our [Studies](#studies), we specify **Values ID's**, that we will need to get access to. Since `RSIStudy` need **xValues** and **close values** we pass as a constructor parameters corresponding IDs, that have been registered previously in our `DefaultCandleDataProvider` - **DataSourceId("xValues")** and **DataSourceId("close")**:
```kotlin
class RSIStudy(
    pane: PaneId,
    id: StudyId = StudyId.uniqueId("RSI"),
    xValuesId: DataSourceId = DataSourceId("xValues"),
    yValuesId: DataSourceId = DataSourceId("close"),
) : CandleStudyBase(pane, id) {
    ...
}
```

4. Next we send **yValuesId** to the `RSIIndicator`. It gets **yValues** from the `DataManager` and uses it to perform RSI calculations. When the job is done it saves the  **outputValues** to the data manager under specific **rsiOutputId**. Here is how it looks in code in short form:

```kotlin
// create rsiIndicator with yValuesId and rsiOutputId
val rsiIndicator = TALibIndicatorProvider.RSIIndicator(defaultPeriod, yValuesId, rsiOutputId)

// register outputValues under yValuesId in DataManager
dataManager.registerYValuesSource(rsiOutputId, outputValues)

// calculate RSI and write computed values to the outputValues
core.rsi(startIndex, endIndex, inputValues, period.value, outStart, outLength, outputValues)
```

5. In order to send RSI values to the chart we create a `LineFinanceSeries` with our **xValuesId** and **rsiOutputId** alongside with other parameters. FinanceSeries gets corresponding values from the DataManager and adds it to the **renderableSeries.dataSeries**:
```kotlin
// create rsiSeries with xValuesId and rsiIndicator.outputId
val rsiSeries = LineFinanceSeries(R.string.rsiIndicatorName, xValuesId, rsiIndicator.outputId, yAxisId)

// get values from the DataManager
val xValues = dataManager.getXValues(xValues.value)
val yValues = dataManager.getYValues(yValues.value)

// Add values to the dataSeries
dataSeries.append(xValues, yValues)
```

That's it. The rest is handled by the SciChart library and finally, our nice blue RSI line appears on the screen:

![RSI-14-close](https://user-images.githubusercontent.com/18321399/162404369-cf37d43e-b5e0-4a79-aa9d-665a2ff6dd77.png)

> **_NOTE_:** Most likely, you'd want to display some real-time data, coming from the WebSocket. To see it in action, please try our [**SciTrader**](https://www.scitrader.io) [Android](https://play.google.com/store/apps/details?id=com.scitrader) and [iOS](https://apps.apple.com/gb/app/scitrader/id1584140348) app.

![SciTrader Real-Time](https://user-images.githubusercontent.com/18321399/163135636-640ad5ca-b28c-4640-961b-9cd1108c3089.gif)
