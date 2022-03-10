package com.scitrader.finance.pane.series;

import android.graphics.Color;
import android.graphics.Typeface;

import com.scichart.drawing.common.FontStyle;
import com.scichart.drawing.utility.ColorUtil;

public final class Constants {
    public final static int DefaultRed = 0xFFFD3720;
    public final static int DefaultGreen = 0xFF28996F;
    public final static int DefaultBlue = 0xFF4DB7F3;

    public final static int DefaultBand = ColorUtil.argb(DefaultBlue, 0.4f);

    public final static int DefaultStrokeUp = DefaultGreen;
    public final static int DefaultStrokeDown = DefaultRed;
    public final static int DefaultFillUp = ColorUtil.argb(DefaultStrokeUp, 0.7f);
    public final static int DefaultFillDown = ColorUtil.argb(DefaultStrokeDown, 0.7f);

    public final static float DefaultThickness = 2f;
    public final static float LightThickness = 1f;

    public final static double DefaultCandleStickDataPointWidth = 0.7;

    public final static int LegendTooltipTitleColor = Color.WHITE;
    public final static FontStyle DefaultTooltipTitleFontStyle = new FontStyle(Typeface.create("sans-serif-medium", Typeface.NORMAL), 12.0f, LegendTooltipTitleColor, true);
    public final static FontStyle DefaultTooltipInfoFontStyle = new FontStyle(Typeface.create("sans-serif", Typeface.NORMAL), 12.0f, LegendTooltipTitleColor, true);
}
