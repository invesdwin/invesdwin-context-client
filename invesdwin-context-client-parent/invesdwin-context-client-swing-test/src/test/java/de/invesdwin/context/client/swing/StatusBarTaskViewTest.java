package de.invesdwin.context.client.swing;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.client.swing.frame.status.task.StatusBarTaskView;
import de.invesdwin.util.assertions.Assertions;

/**
 * Pins the display-only abbreviation of the status bar task text: the text must never
 * change when it fits, and when it does not, the abbreviation must fit the given pixel
 * width while keeping both the leading task identity and the trailing progress/ETA.
 */
@ThreadSafe
public class StatusBarTaskViewTest {

    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final FontMetrics FONT_METRICS = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            .createGraphics()
            .getFontMetrics(FONT);

    //the shape of a real task name: a derived store key string carrying a settings dump
    private static final String LONG_TASK_TEXT = "Loading precalculated values for MODEL_InnerEvaluator"
            + "/SESSION[08:00:00.000.000.000.000-22:00:00.000.000.000.000 | Europe/Berlin]:FEED:SYMBOL"
            + "/Time[FIVE_MINS]_OHLC_null/TIME_BARS_generic/EffectBarCache["
            + "settings:EffectSettings[config:EffectDPConfig{filterRedundantPositions:true,"
            + "redundantPositionChecksAllSignals:true,smallerBarConfig:Time[ONE_MIN],barConfig:Time[FIVE_MINS],"
            + "useSessionFilter:true,spaceLeftBars:0,spaceRightBars:15,noise1Pips:10.0,cutOffPeriod:2000,"
            + "slopeEnabled:true,slopeBasePriceAndStepAbsolute:500.0,slopeBasePipsLong:2.1,slopeBasePipsShort:2.6,"
            + "slopeIncrementPips:0.1,slopeRounded:true,slopeModelIdOverride:null},"
            + "noise1Function:TableRootBarNoiseFunction[noiseTable:default@-1276938141],"
            + "scaling:ScalingSettings[config:ScalingDPConfig{noise:2.5,table:default,cutOffPeriod:2000,"
            + "atr:barsCount:14,barConfig:Time[ONE_HOUR],useSessionFilter:true,smoothingType:Smoothed or Modified"
            + " Moving Average},ignoreSignalBelowOrEqualToScaling:0.0,ignoreSignalAboveOrEqualToScaling:500.0],"
            + "ignoreOrderCommentEntryFilter:VolatilityEntryFilter[VolatilityFilterDPConfig{barConfig:"
            + "Time[DAILY],useSessionFilter:true,mode:MiddleEvaluator,bars:10,minimumAvgHighLowRange:80.0,"
            + "maximumAvgHighLowRange:NaN}]] | calculationEnabled:true"
            + " | defaultCalculationEnabled:true]_snapshotCache (23%) | ETA: PT14M47S";

    private static final String TRAILING_PROGRESS = "(23%) | ETA: PT14M47S";

    @Test
    public void testTextThatFitsIsNotChanged() {
        Assertions.assertThat(StatusBarTaskView.abbreviateToFit(FONT_METRICS, "Initializing Strategy (64%)", 10000))
                .isEqualTo("Initializing Strategy (64%)");
        Assertions.assertThat(StatusBarTaskView.abbreviateToFit(FONT_METRICS, LONG_TASK_TEXT, Integer.MAX_VALUE))
                .isEqualTo(LONG_TASK_TEXT);
    }

    @Test
    public void testUnknownLayoutKeepsTextUnchanged() {
        //maxWidth 0 means the status bar has no geometry yet - the caller must not
        //abbreviate against a guessed width then
        Assertions.assertThat(StatusBarTaskView.abbreviateToFit(FONT_METRICS, LONG_TASK_TEXT, 0))
                .isEqualTo(LONG_TASK_TEXT);
        Assertions.assertThat(StatusBarTaskView.abbreviateToFit(FONT_METRICS, LONG_TASK_TEXT, -1))
                .isEqualTo(LONG_TASK_TEXT);
    }

    @Test
    public void testAbbreviationKeepsPrefixAndTrailingProgress() {
        final String abbreviated = StatusBarTaskView.abbreviateToFit(FONT_METRICS, LONG_TASK_TEXT, 600);
        Assertions.assertThat(abbreviated.length()).isLessThan(LONG_TASK_TEXT.length());
        Assertions.assertThat(abbreviated).contains("...");
        Assertions.assertThat(abbreviated).startsWith("Loading precalculated");
        Assertions.assertThat(abbreviated).endsWith(TRAILING_PROGRESS);
    }

    @Test
    public void testAbbreviationNeverExceedsTheGivenWidth() {
        for (int width = 80; width <= 1200; width += 20) {
            final String abbreviated = StatusBarTaskView.abbreviateToFit(FONT_METRICS, LONG_TASK_TEXT, width);
            Assertions.assertThat(FONT_METRICS.stringWidth(abbreviated))
                    .describedAs("abbreviated to width " + width + ": " + abbreviated)
                    .isLessThanOrEqualTo(width);
        }
    }

    @Test
    public void testTextShorterThanTheMarkerIsReturnedUnchanged() {
        //there is nothing to cut - the abbreviation must never make text longer
        Assertions.assertThat(StatusBarTaskView.abbreviateToFit(FONT_METRICS, "ab", 1)).isEqualTo("ab");
        Assertions.assertThat(StatusBarTaskView.abbreviateToFit(FONT_METRICS, "", 1)).isEmpty();
    }

    @Test
    public void testDegenerateWidthsNeitherThrowNorReturnEmpty() {
        for (int width = -10; width <= 79; width++) {
            final String abbreviated = StatusBarTaskView.abbreviateToFit(FONT_METRICS, LONG_TASK_TEXT, width);
            Assertions.assertThat(abbreviated).describedAs("width " + width).isNotEmpty();
        }
    }

}
