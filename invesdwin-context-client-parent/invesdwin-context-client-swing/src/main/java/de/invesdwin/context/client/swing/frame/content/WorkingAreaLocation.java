package de.invesdwin.context.client.swing.frame.content;

import javax.annotation.concurrent.Immutable;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.location.CWorkingAreaLocation;

@Immutable
public enum WorkingAreaLocation {
    Center {
        @Override
        public CLocation create(final CWorkingAreaLocation base) {
            return base;
        }
    },
    North {
        @Override
        public CLocation create(final CWorkingAreaLocation base) {
            return base.north(0.333, ordinal());
        }
    },
    South {
        @Override
        public CLocation create(final CWorkingAreaLocation base) {
            return base.south(0.333, ordinal());
        }
    },
    West {
        @Override
        public CLocation create(final CWorkingAreaLocation base) {
            return base.west(0.333, ordinal());
        }
    },
    East {
        @Override
        public CLocation create(final CWorkingAreaLocation base) {
            return base.east(0.333, ordinal());
        }
    };

    public abstract CLocation create(CWorkingAreaLocation base);
}
