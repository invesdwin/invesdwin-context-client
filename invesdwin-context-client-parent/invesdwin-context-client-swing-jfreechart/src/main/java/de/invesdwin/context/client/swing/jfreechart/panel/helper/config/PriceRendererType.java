package de.invesdwin.context.client.swing.jfreechart.panel.helper.config;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.client.swing.jfreechart.panel.helper.legend.HighlightedLegendInfo;

@NotThreadSafe
public enum PriceRendererType implements IRendererType {
    None {
        @Override
        public boolean isLineStyleConfigurable() {
            return false;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return false;
        }

        @Override
        public boolean isUpColorConfigurable() {
            return false;
        }

        @Override
        public boolean isDownColorConfigurable() {
            return false;
        }

        @Override
        public boolean isSeriesColorConfigurable() {
            return false;
        }

        @Override
        public PriceRendererType orDefault() {
            return DEFAULT;
        }
    },
    Line {
        @Override
        public boolean isLineStyleConfigurable() {
            return true;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }

        @Override
        public boolean isUpColorConfigurable() {
            return false;
        }

        @Override
        public boolean isDownColorConfigurable() {
            return false;
        }

        @Override
        public boolean isSeriesColorConfigurable() {
            return true;
        }

        @Override
        public PriceRendererType orDefault() {
            return this;
        }
    },
    Step {
        @Override
        public boolean isLineStyleConfigurable() {
            return true;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }

        @Override
        public boolean isUpColorConfigurable() {
            return false;
        }

        @Override
        public boolean isDownColorConfigurable() {
            return false;
        }

        @Override
        public boolean isSeriesColorConfigurable() {
            return true;
        }

        @Override
        public PriceRendererType orDefault() {
            return this;
        }
    },
    Area {
        @Override
        public boolean isLineStyleConfigurable() {
            return true;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }

        @Override
        public boolean isUpColorConfigurable() {
            return false;
        }

        @Override
        public boolean isDownColorConfigurable() {
            return false;
        }

        @Override
        public boolean isSeriesColorConfigurable() {
            return true;
        }

        @Override
        public PriceRendererType orDefault() {
            return this;
        }
    },
    OHLC {
        @Override
        public boolean isLineStyleConfigurable() {
            return false;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }

        @Override
        public boolean isUpColorConfigurable() {
            return true;
        }

        @Override
        public boolean isDownColorConfigurable() {
            return true;
        }

        @Override
        public boolean isSeriesColorConfigurable() {
            return false;
        }

        @Override
        public PriceRendererType orDefault() {
            return this;
        }
    },
    Candlestick {
        @Override
        public boolean isLineStyleConfigurable() {
            return false;
        }

        @Override
        public boolean isLineWidthConfigurable() {
            return true;
        }

        @Override
        public boolean isUpColorConfigurable() {
            return true;
        }

        @Override
        public boolean isDownColorConfigurable() {
            return true;
        }

        @Override
        public boolean isSeriesColorConfigurable() {
            return false;
        }

        @Override
        public PriceRendererType orDefault() {
            return this;
        }
    };

    public static final PriceRendererType DEFAULT = Candlestick;

    @Override
    public void reset(final HighlightedLegendInfo highlighted, final SeriesInitialSettings initialSettings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeriesRendererType getSeriesRendererType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSeriesRendererTypeConfigurable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPriceLineConfigurable() {
        return true;
    }

    public abstract PriceRendererType orDefault();

}
