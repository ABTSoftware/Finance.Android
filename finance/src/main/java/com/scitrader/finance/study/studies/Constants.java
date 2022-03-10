package com.scitrader.finance.study.studies;

import com.tictactec.ta.lib.MAType;

public final class Constants {
    public static final class Indicator {
        public final static int defaultPeriod = 14;
        public final static double defaultDev = 2.0;
        public final static int defaultSlow = 12;
        public final static int defaultFast = 26;
        public final static int defaultSignal = 9;
        public final static double defaultAcceleration = 1.0;
        public final static double defaultMaximum = 1.0;
        public final static MAType defaultMaType = MAType.Sma;
    }
}
