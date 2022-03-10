package com.scitrader.finance.indicators

import androidx.annotation.StringRes
import com.scichart.core.model.DoubleValues
import com.scitrader.finance.R
import com.scitrader.finance.core.DataSourceChangedArgs
import com.scitrader.finance.core.DataSourceId
import com.scitrader.finance.core.IDataManager
import com.scitrader.finance.edit.properties.*
import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MAType
import com.tictactec.ta.lib.MInteger
import java.util.*

class TALibIndicatorProvider {
    companion object {
        private val core = Core()

        fun DoubleValues.normalize(outStart: MInteger, outLength: MInteger) {
            if (outLength.value > 0) {
                System.arraycopy(itemsArray, 0, itemsArray, outStart.value, outLength.value)
                Arrays.fill(itemsArray, 0, outStart.value, Double.NaN)
            }
        }

        fun DoubleValues.fillWithNaNs(size: Int) {
            Arrays.fill(itemsArray, 0, size, Double.NaN)
        }
    }

    abstract class SingleInputTaLibIndicator(@StringRes name: Int, inputId: DataSourceId) : IndicatorBase(name) {
        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val input = DataSourceEditableProperty(R.string.indicatorSingleInput, name, inputId) { id, value ->
            dependsOn(R.string.indicatorSingleInput, value)
            onPropertyChanged(id)
        }

        override fun onDataDrasticallyChanged(dataManager: IDataManager) {
            if (!isAttached) return

            val inputValues = dataManager.getYValues(input.value)
            if (inputValues != null) {

                val size = inputValues.size()
                if (size == 0) return

                val endIndex = size - 1

                // TODO end index should be inclusive
                compute(0, endIndex, inputValues)
            } else clear()
        }

        protected abstract fun compute(startIndex: Int, endIndex: Int, inputValues: DoubleValues)

        protected abstract fun clear()
    }

    abstract class SingleInputOutputTaLibIndicator(
        @StringRes name: Int,
        inputId: DataSourceId,
        val outputId: DataSourceId
    ) : SingleInputTaLibIndicator(name, inputId) {
        private val outputValues = DoubleValues()

        protected val outputChangedArgs = DataSourceChangedArgs(outputId)

        override fun clear() {
            outputValues.clear()
        }

        override fun onDataManagerAttached(dataManager: IDataManager) {
            super.onDataManagerAttached(dataManager)

            dataManager.registerYValuesSource(outputId, outputValues)
        }

        override fun onDataManagerDetached(dataManager: IDataManager) {
            super.onDataManagerDetached(dataManager)

            dataManager.unregisterYValuesSource(outputId)
        }

        override fun compute(startIndex: Int, endIndex: Int, inputValues: DoubleValues) {
            val outStart = MInteger()
            val outLength = MInteger()
            val size = inputValues.size()
            outputValues.setSize(size)

            if (tryCompute(
                    startIndex,
                    endIndex,
                    inputValues.itemsArray,
                    outStart,
                    outLength,
                    outputValues.itemsArray
                )) {
                outputValues.normalize(outStart, outLength)
            } else {
                outputValues.fillWithNaNs(size)
            }

            onDataProviderChanged(outputChangedArgs)
        }

        protected abstract fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean
    }

    class SMAIndicator(
        period: Int,
        inputId: DataSourceId,
        outputId: DataSourceId
    ) :
        SingleInputOutputTaLibIndicator(R.string.smaIndicatorName, inputId, outputId) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorSmaPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(
                    core.smaLookback(period.value),
                    startIndex,
                    endIndex
                )) {
                return false
            }

            core.sma(
                startIndex,
                endIndex,
                inputValues,
                period.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            period.reset()
        }
    }

    class MacdIndicator(
        inputId: DataSourceId,
        val macdId: DataSourceId,
        val macdSignalId: DataSourceId,
        val macdHistId: DataSourceId,
        slow: Int,
        fast: Int,
        signal: Int
    ) : SingleInputTaLibIndicator(R.string.macdIndicatorName, inputId) {

        private val outputChangedArgs = DataSourceChangedArgs(macdId, macdSignalId, macdHistId)

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val slow = PeriodEditableProperty(R.string.indicatorMacdSlow, name, slow) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val fast = PeriodEditableProperty(R.string.indicatorMacdFast, name, fast) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val signal = PeriodEditableProperty(R.string.indicatorMacdSignal, name, signal) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        private val macd = DoubleValues()
        private val macdSignal = DoubleValues()
        private val macdHist = DoubleValues()

        override fun clear() {
            macd.clear()
            macdSignal.clear()
            macdHist.clear()
        }

        override fun onDataManagerAttached(dataManager: IDataManager) {
            super.onDataManagerAttached(dataManager)

            dataManager.registerYValuesSource(macdId, macd)
            dataManager.registerYValuesSource(macdSignalId, macdSignal)
            dataManager.registerYValuesSource(macdHistId, macdHist)
        }

        override fun onDataManagerDetached(dataManager: IDataManager) {
            super.onDataManagerDetached(dataManager)

            dataManager.unregisterYValuesSource(macdId)
            dataManager.unregisterYValuesSource(macdSignalId)
            dataManager.unregisterYValuesSource(macdHistId)
        }

        override fun compute(startIndex: Int, endIndex: Int, inputValues: DoubleValues) {
            val outStart = MInteger()
            val outLength = MInteger()
            val size = inputValues.size()

            macd.setSize(size)
            macdSignal.setSize(size)
            macdHist.setSize(size)

            if (shouldSkipCalculation(
                    core.macdLookback(fast.value, slow.value, signal.value),
                    startIndex,
                    endIndex
                )) {
                macd.fillWithNaNs(size)
                macdSignal.fillWithNaNs(size)
                macdHist.fillWithNaNs(size)
            } else {
                core.macd(
                    startIndex,
                    endIndex,
                    inputValues.itemsArray,
                    fast.value,
                    slow.value,
                    signal.value,
                    outStart,
                    outLength,
                    macd.itemsArray,
                    macdSignal.itemsArray,
                    macdHist.itemsArray
                )

                macd.normalize(outStart, outLength)
                macdSignal.normalize(outStart, outLength)
                macdHist.normalize(outStart, outLength)
            }

            onDataProviderChanged(outputChangedArgs)
        }

        override fun reset() {
            slow.reset()
            fast.reset()
            signal.reset()
        }
    }

    class RSIIndicator(
        period: Int,
        inputId: DataSourceId,
        outputId: DataSourceId
    ) :
        SingleInputOutputTaLibIndicator(
            R.string.rsiIndicatorName, inputId, outputId
        ) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorRsiPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.rsiLookback(period.value), startIndex, endIndex)) {
                return false
            }

            core.rsi(
                startIndex,
                endIndex,
                inputValues,
                period.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            period.reset()
        }
    }

    class BBandsIndicator(
        period: Int,
        devUp: Double,
        devDown: Double,
        maType: MAType,
        inputId: DataSourceId,
        val lowerBandId: DataSourceId,
        val middleBandId: DataSourceId,
        val upperBandId: DataSourceId
    ) : SingleInputTaLibIndicator(R.string.bBandsIndicatorName, inputId) {
        private val lowerBand = DoubleValues()
        private val middleBand = DoubleValues()
        private val upperBand = DoubleValues()

        private val outputChangedArgs = DataSourceChangedArgs(lowerBandId, middleBandId, upperBandId)

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorBBandsPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val devUp = DoubleEditableProperty(R.string.indicatorBBandsDevUp, name, devUp) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val devDown = DoubleEditableProperty(R.string.indicatorBBandsDevDown, name, devDown) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val maType = EnumEditableProperty<MAType>(R.string.indicatorBBandsMAType, name, maType) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun clear() {
            lowerBand.clear()
            middleBand.clear()
            upperBand.clear()
        }

        override fun compute(startIndex: Int, endIndex: Int, inputValues: DoubleValues) {
            val outStart = MInteger()
            val outLength = MInteger()
            val size = inputValues.size()

            lowerBand.setSize(size)
            middleBand.setSize(size)
            upperBand.setSize(size)

            if (shouldSkipCalculation(
                    core.bbandsLookback(period.value, devUp.value, devDown.value, maType.value),
                    startIndex,
                    endIndex
                )) {
                lowerBand.fillWithNaNs(size)
                middleBand.fillWithNaNs(size)
                upperBand.fillWithNaNs(size)
            } else {
                core.bbands(
                    startIndex,
                    endIndex,
                    inputValues.itemsArray,
                    period.value,
                    devUp.value,
                    devDown.value,
                    maType.value,
                    outStart,
                    outLength,
                    upperBand.itemsArray,
                    middleBand.itemsArray,
                    lowerBand.itemsArray
                )

                lowerBand.normalize(outStart, outLength)
                middleBand.normalize(outStart, outLength)
                upperBand.normalize(outStart, outLength)
            }

            onDataProviderChanged(outputChangedArgs)
        }

        override fun onDataManagerAttached(dataManager: IDataManager) {
            super.onDataManagerAttached(dataManager)

            dataManager.registerYValuesSource(lowerBandId, lowerBand)
            dataManager.registerYValuesSource(middleBandId, middleBand)
            dataManager.registerYValuesSource(upperBandId, upperBand)
        }

        override fun onDataManagerDetached(dataManager: IDataManager) {
            super.onDataManagerDetached(dataManager)

            dataManager.unregisterYValuesSource(lowerBandId)
            dataManager.unregisterYValuesSource(middleBandId)
            dataManager.unregisterYValuesSource(upperBandId)
        }

        override fun reset() {
            period.reset()
            devUp.reset()
            devDown.reset()
            maType.reset()
        }
    }

    class HT_TrendlineIndicator(
        inputId: DataSourceId,
        outputId: DataSourceId
    ) : SingleInputOutputTaLibIndicator(
        R.string.htTrendlineIndicatorName, inputId, outputId
    ) {
        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.htTrendlineLookback(), startIndex, endIndex)) {
                return false
            }

            core.htTrendline(
                startIndex,
                endIndex,
                inputValues,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {}
    }

    class STDDevIndicator(
        period: Int,
        dev: Double,
        inputId: DataSourceId,
        outputId: DataSourceId
    ) :
        SingleInputOutputTaLibIndicator(
            R.string.stdDevIndicatorName, inputId, outputId
        ) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorStdDevPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        var dev = DoubleEditableProperty(R.string.indicatorStdDev_Dev, name, dev) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.stdDevLookback(period.value, dev.value), startIndex, endIndex)) {
                return false
            }

            core.stdDev(
                startIndex,
                endIndex,
                inputValues,
                period.value,
                dev.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            period.reset()
            dev.reset()
        }
    }

    class EMAIndicator(
        period: Int,
        inputId: DataSourceId,
        outputId: DataSourceId
    ) :
        SingleInputOutputTaLibIndicator(
            R.string.emaIndicatorName, inputId, outputId
        ) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorEmaPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.emaLookback(period.value), startIndex, endIndex)) {
                return false
            }

            core.ema(
                startIndex,
                endIndex,
                inputValues,
                period.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            period.reset()
        }
    }

    abstract class HighLowCloseInputIndicator(
        name: Int,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        inputCloseId: DataSourceId
    ) : IndicatorBase(name) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val inputHigh = DataSourceEditableProperty(R.string.indicatorHighInput, name, inputHighId) { id, value ->
            dependsOn(R.string.indicatorHighInput, value)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val inputLow = DataSourceEditableProperty(R.string.indicatorLowInput, name, inputLowId) { id, value ->
            dependsOn(R.string.indicatorLowInput, value)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val inputClose = DataSourceEditableProperty(R.string.indicatorCloseInput, name, inputCloseId) { id, value ->
            dependsOn(R.string.indicatorCloseInput, value)
            onPropertyChanged(id)
        }

        override fun onDataDrasticallyChanged(dataManager: IDataManager) {
            if (!isAttached) return

            val inputHighValues = dataManager.getYValues(inputHigh.value)
            val inputLowValues = dataManager.getYValues(inputLow.value)
            val inputCloseValues = dataManager.getYValues(inputClose.value)
            if (inputHighValues != null && inputLowValues != null && inputCloseValues != null) {

                val size = inputCloseValues.size()
                if (size == 0) return

                val endIndex = size - 1

                // TODO end index should be inclusive
                compute(
                    0,
                    endIndex,
                    inputHighValues,
                    inputLowValues,
                    inputCloseValues
                )
            } else clear()
        }

        protected abstract fun compute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleValues,
            inputLowValues: DoubleValues,
            inputCloseValues: DoubleValues
        )

        protected abstract fun clear()

        override fun reset() {
            inputHigh.reset()
            inputLow.reset()
            inputClose.reset()
        }
    }

    abstract class HighLowCloseInputSingleOutputIndicator(
        name: Int,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        inputCloseId: DataSourceId,
        val outputId: DataSourceId
    ) : HighLowCloseInputIndicator(name, inputHighId, inputLowId, inputCloseId) {

        private val outputValues = DoubleValues()

        protected val outputChangedArgs = DataSourceChangedArgs(outputId)

        override fun clear() {
            outputValues.clear()
        }

        override fun onDataManagerAttached(dataManager: IDataManager) {
            super.onDataManagerAttached(dataManager)

            dataManager.registerYValuesSource(outputId, outputValues)
        }

        override fun onDataManagerDetached(dataManager: IDataManager) {
            super.onDataManagerDetached(dataManager)

            dataManager.unregisterYValuesSource(outputId)
        }

        override fun compute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleValues,
            inputLowValues: DoubleValues,
            inputCloseValues: DoubleValues
        ) {
            val outStart = MInteger()
            val outLength = MInteger()
            val size = inputCloseValues.size()
            outputValues.setSize(size)

            if (tryCompute(
                    startIndex,
                    endIndex,
                    inputHighValues.itemsArray,
                    inputLowValues.itemsArray,
                    inputCloseValues.itemsArray,
                    outStart,
                    outLength,
                    outputValues.itemsArray
                )) {
                outputValues.normalize(outStart, outLength)
            } else {
                outputValues.fillWithNaNs(size)
            }

            onDataProviderChanged(outputChangedArgs)
        }

        protected abstract fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleArray,
            inputLowValues: DoubleArray,
            inputCloseValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean
    }

    class ADXIndicator(
        period: Int,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        inputCloseId: DataSourceId,
        outputId: DataSourceId
    ) : HighLowCloseInputSingleOutputIndicator(
        R.string.adxIndicatorName,
        inputHighId,
        inputLowId,
        inputCloseId,
        outputId
    ) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorAdxPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleArray,
            inputLowValues: DoubleArray,
            inputCloseValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.adxLookback(period.value), startIndex, endIndex)) {
                return false
            }

            core.adx(
                startIndex,
                endIndex,
                inputHighValues,
                inputLowValues,
                inputCloseValues,
                period.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            super.reset()

            period.reset()
        }
    }

    class ATRIndicator(
        period: Int,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        inputCloseId: DataSourceId,
        outputId: DataSourceId
    ) : HighLowCloseInputSingleOutputIndicator(
        R.string.atrIndicatorName,
        inputHighId,
        inputLowId,
        inputCloseId,
        outputId
    ) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorAtrPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleArray,
            inputLowValues: DoubleArray,
            inputCloseValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.atrLookback(period.value), startIndex, endIndex)) {
                return false
            }

            core.atr(
                startIndex,
                endIndex,
                inputHighValues,
                inputLowValues,
                inputCloseValues,
                period.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            super.reset()

            period.reset()
        }
    }

    class CCIIndicator(
        period: Int,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        inputCloseId: DataSourceId,
        outputId: DataSourceId
    ) : HighLowCloseInputSingleOutputIndicator(
        R.string.cciIndicatorName,
        inputHighId,
        inputLowId,
        inputCloseId,
        outputId
    ) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val period = PeriodEditableProperty(R.string.indicatorCciPeriod, name, period) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleArray,
            inputLowValues: DoubleArray,
            inputCloseValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(
                    core.cciLookback(period.value),
                    startIndex,
                    endIndex
                )) {
                return false
            }

            core.cci(
                startIndex,
                endIndex,
                inputHighValues,
                inputLowValues,
                inputCloseValues,
                period.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            super.reset()

            period.reset()
        }
    }

    abstract class CloseVolumeInputIndicator(
        name: Int,
        inputCloseId: DataSourceId,
        inputVolumeId: DataSourceId,
    ) : IndicatorBase(name) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val inputClose = DataSourceEditableProperty(R.string.indicatorCloseInput, name, inputCloseId) { id, value ->
            dependsOn(R.string.indicatorCloseInput, value)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val inputVolume = DataSourceEditableProperty(R.string.indicatorVolumeInput, name, inputVolumeId) { id, value ->
            dependsOn(R.string.indicatorVolumeInput, value)
            onPropertyChanged(id)
        }

        override fun onDataDrasticallyChanged(dataManager: IDataManager) {
            if (!isAttached) return

            val inputCloseValues = dataManager.getYValues(inputClose.value)
            val inputVolumeValues = dataManager.getYValues(inputVolume.value)
            if (inputCloseValues != null && inputVolumeValues != null) {

                val size = inputCloseValues.size()
                if (size == 0) return

                val endIndex = size - 1

                // TODO end index should be inclusive
                compute(0, endIndex, inputCloseValues, inputVolumeValues)
            } else clear()
        }

        protected abstract fun compute(
            startIndex: Int,
            endIndex: Int,
            inputCloseValues: DoubleValues,
            inputVolumeValues: DoubleValues
        )

        protected abstract fun clear()

        override fun reset() {
            inputClose.reset()
            inputVolume.reset()
        }
    }

    abstract class CloseVolumeInputSingleOutputIndicator(
        name: Int,
        inputCloseId: DataSourceId,
        inputVolumeId: DataSourceId,
        val outputId: DataSourceId
    ) : CloseVolumeInputIndicator(name, inputCloseId, inputVolumeId) {

        private val outputValues = DoubleValues()

        protected val outputChangedArgs = DataSourceChangedArgs(outputId)

        override fun clear() {
            outputValues.clear()
        }

        override fun onDataManagerAttached(dataManager: IDataManager) {
            super.onDataManagerAttached(dataManager)

            dataManager.registerYValuesSource(outputId, outputValues)
        }

        override fun onDataManagerDetached(dataManager: IDataManager) {
            super.onDataManagerDetached(dataManager)

            dataManager.unregisterYValuesSource(outputId)
        }

        override fun compute(
            startIndex: Int,
            endIndex: Int,
            inputCloseValues: DoubleValues,
            inputVolumeValues: DoubleValues,
        ) {
            val outStart = MInteger()
            val outLength = MInteger()
            val size = inputCloseValues.size()
            outputValues.setSize(size)

            if (tryCompute(
                startIndex,
                endIndex,
                inputCloseValues.itemsArray,
                inputVolumeValues.itemsArray,
                outStart,
                outLength,
                outputValues.itemsArray
            )) {
                outputValues.normalize(outStart, outLength)
            } else {
                outputValues.fillWithNaNs(size)
            }

            onDataProviderChanged(outputChangedArgs)
        }

        protected abstract fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputCloseValues: DoubleArray,
            inputVolumeValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean
    }

    class OBVIndicator(
        inputCloseId: DataSourceId,
        inputVolumeId: DataSourceId,
        outputId: DataSourceId
    ) : CloseVolumeInputSingleOutputIndicator(
        R.string.obvIndicatorName,
        inputCloseId,
        inputVolumeId,
        outputId
    ) {
        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputCloseValues: DoubleArray,
            inputVolumeValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.obvLookback(), startIndex, endIndex)) {
                return false
            }

            core.obv(
                startIndex,
                endIndex,
                inputCloseValues,
                inputVolumeValues,
                outStart,
                outLength,
                outputValues
            )

            return true
        }
    }

    abstract class HighLowInputIndicator(
        name: Int,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId
    ) : IndicatorBase(name) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val inputHigh = DataSourceEditableProperty(R.string.indicatorHighInput, name, inputHighId) { id, value ->
            dependsOn(R.string.indicatorHighInput, value)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val inputLow = DataSourceEditableProperty(R.string.indicatorLowInput, name, inputLowId) { id, value ->
            dependsOn(R.string.indicatorLowInput, value)
            onPropertyChanged(id)
        }

        override fun onDataDrasticallyChanged(dataManager: IDataManager) {
            if (!isAttached) return

            val inputHighValues = dataManager.getYValues(inputHigh.value)
            val inputLowValues = dataManager.getYValues(inputLow.value)
            if (inputHighValues != null && inputLowValues != null) {

                val size = inputHighValues.size()
                if (size == 0) return

                val endIndex = size - 1

                // TODO end index should be inclusive
                compute(0, endIndex, inputHighValues, inputLowValues)
            } else clear()
        }

        protected abstract fun compute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleValues,
            inputLowValues: DoubleValues
        )

        protected abstract fun clear()

        override fun reset() {
            inputHigh.reset()
            inputLow.reset()
        }
    }

    abstract class HighLowInputSingleOutputIndicator(
        name: Int,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        val outputId: DataSourceId
    ) : HighLowInputIndicator(name, inputHighId, inputLowId) {

        private val outputValues = DoubleValues()

        protected val outputChangedArgs = DataSourceChangedArgs(outputId)

        override fun clear() {
            outputValues.clear()
        }

        override fun onDataManagerAttached(dataManager: IDataManager) {
            super.onDataManagerAttached(dataManager)

            dataManager.registerYValuesSource(outputId, outputValues)
        }

        override fun onDataManagerDetached(dataManager: IDataManager) {
            super.onDataManagerDetached(dataManager)

            dataManager.unregisterYValuesSource(outputId)
        }

        override fun compute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleValues,
            inputLowValues: DoubleValues
        ) {
            val outStart = MInteger()
            val outLength = MInteger()
            val size = inputHighValues.size()
            outputValues.setSize(size)

            if (tryCompute(
                    startIndex,
                    endIndex,
                    inputHighValues.itemsArray,
                    inputLowValues.itemsArray,
                    outStart,
                    outLength,
                    outputValues.itemsArray
                )) {
                outputValues.normalize(outStart, outLength)
            } else {
                outputValues.fillWithNaNs(size)
            }

            onDataProviderChanged(outputChangedArgs)
        }

        protected abstract fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleArray,
            inputLowValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean
    }

    class SARIndicator(
        acceleration: Double,
        maximum: Double,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        outputId: DataSourceId
    ) : HighLowInputSingleOutputIndicator(
        R.string.sarIndicatorName,
        inputHighId,
        inputLowId,
        outputId
    ) {

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val acceleration = PositiveDoubleEditableProperty(R.string.indicatorSarAcceleration, name, acceleration) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val maximum = PositiveDoubleEditableProperty(R.string.indicatorSarMaximum, name, maximum) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        override fun tryCompute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleArray,
            inputLowValues: DoubleArray,
            outStart: MInteger,
            outLength: MInteger,
            outputValues: DoubleArray
        ) : Boolean {
            if (shouldSkipCalculation(core.sarLookback(acceleration.value, maximum.value), startIndex, endIndex)) {
                return false
            }

            core.sar(
                startIndex,
                endIndex,
                inputHighValues,
                inputLowValues,
                acceleration.value,
                maximum.value,
                outStart,
                outLength,
                outputValues
            )

            return true
        }

        override fun reset() {
            super.reset()

            acceleration.reset()
            maximum.reset()
        }
    }

    class StochIndicator(
        fastK: Int,
        slowK: Int,
        slowD: Int,
        slowK_maType: MAType,
        slowD_maType: MAType,
        inputHighId: DataSourceId,
        inputLowId: DataSourceId,
        inputCloseId: DataSourceId,
        val slowKId: DataSourceId,
        val slowDId: DataSourceId,
    ) : HighLowCloseInputIndicator(
        R.string.stochIndicatorName,
        inputHighId,
        inputLowId,
        inputCloseId
    ) {
        private val outputChangedArgs = DataSourceChangedArgs(slowKId, slowDId)

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val fastK = FastSlowPeriodEditableProperty(R.string.indicatorStochFastK, name, fastK) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val slowK = FastSlowPeriodEditableProperty(R.string.indicatorStochSlowK, name, slowK) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val slowD = FastSlowPeriodEditableProperty(R.string.indicatorStochSlowD, name, slowD) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val slowK_maType = EnumEditableProperty<MAType>(R.string.indicatorStochSlowK_MAType, name, slowK_maType) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        @get:com.scitrader.finance.edit.annotations.EditableProperty
        val slowD_maType = EnumEditableProperty<MAType>(R.string.indicatorStochSlowD_MAType, name, slowD_maType) { id, _ ->
            onDataSourceChanged(outputChangedArgs)
            onPropertyChanged(id)
        }

        private val slowKValues = DoubleValues()
        private val slowDValues = DoubleValues()

        override fun clear() {
            slowKValues.clear()
            slowDValues.clear()
        }

        override fun onDataManagerAttached(dataManager: IDataManager) {
            super.onDataManagerAttached(dataManager)

            dataManager.registerYValuesSource(slowKId, slowKValues)
            dataManager.registerYValuesSource(slowDId, slowDValues)
        }

        override fun onDataManagerDetached(dataManager: IDataManager) {
            super.onDataManagerDetached(dataManager)

            dataManager.unregisterYValuesSource(slowKId)
            dataManager.unregisterYValuesSource(slowDId)
        }

        override fun compute(
            startIndex: Int,
            endIndex: Int,
            inputHighValues: DoubleValues,
            inputLowValues: DoubleValues,
            inputCloseValues: DoubleValues
        ) {
            val outStart = MInteger()
            val outLength = MInteger()
            val size = inputCloseValues.size()

            slowKValues.setSize(size)
            slowDValues.setSize(size)

            if (shouldSkipCalculation(
                    core.stochLookback(fastK.value, slowK.value, slowK_maType.value, slowD.value, slowD_maType.value),
                    startIndex,
                    endIndex
                )) {
                slowKValues.fillWithNaNs(size)
                slowDValues.fillWithNaNs(size)
            } else {
                core.stoch(
                    startIndex,
                    endIndex,
                    inputHighValues.itemsArray,
                    inputLowValues.itemsArray,
                    inputCloseValues.itemsArray,
                    fastK.value,
                    slowK.value,
                    slowK_maType.value,
                    slowD.value,
                    slowD_maType.value,
                    outStart,
                    outLength,
                    slowKValues.itemsArray,
                    slowDValues.itemsArray
                )

                slowKValues.normalize(outStart, outLength)
                slowDValues.normalize(outStart, outLength)
            }

            onDataProviderChanged(outputChangedArgs)
        }

        override fun reset() {
            super.reset()

            fastK.reset()
            slowK.reset()
            slowD.reset()
            slowK_maType.reset()
            slowD_maType.reset()
        }
    }
}
