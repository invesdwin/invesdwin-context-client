package de.invesdwin.context.client.swing.frame.content;

import javax.annotation.concurrent.Immutable;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.location.CWorkingAreaLocation;

@Immutable
public enum WorkingAreaLocation implements IReusingWorkingAreaLocation {
    Center {
        @Override
        public CLocation newLocation(final CWorkingAreaLocation base) {
            return base;
        }
    },
    North {
        @Override
        public CLocation newLocation(final CWorkingAreaLocation base) {
            return base.north(0.333, getId());
        }
    },
    South {
        @Override
        public CLocation newLocation(final CWorkingAreaLocation base) {
            return base.south(0.333, getId());
        }
    },
    West {
        @Override
        public CLocation newLocation(final CWorkingAreaLocation base) {
            return base.west(0.333, getId());
        }
    },
    East {
        @Override
        public CLocation newLocation(final CWorkingAreaLocation base) {
            return base.east(0.333, getId());
        }
    };

    @Override
    public int getId() {
        return ordinal();
    }

}
