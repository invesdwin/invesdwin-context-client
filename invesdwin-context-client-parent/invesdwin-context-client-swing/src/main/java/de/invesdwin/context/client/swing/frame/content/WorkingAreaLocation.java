package de.invesdwin.context.client.swing.frame.content;

import javax.annotation.concurrent.Immutable;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.location.CWorkingAreaLocation;
import bibliothek.util.Filter;

@Immutable
public enum WorkingAreaLocation implements IWorkingAreaLocation {
    Center {
        @Override
        protected CLocation newLocation(final CWorkingAreaLocation base) {
            return base;
        }
    },
    North {
        @Override
        protected CLocation newLocation(final CWorkingAreaLocation base) {
            return base.north(0.333, ordinal());
        }
    },
    South {
        @Override
        protected CLocation newLocation(final CWorkingAreaLocation base) {
            return base.south(0.333, ordinal());
        }
    },
    West {
        @Override
        protected CLocation newLocation(final CWorkingAreaLocation base) {
            return base.west(0.333, ordinal());
        }
    },
    East {
        @Override
        protected CLocation newLocation(final CWorkingAreaLocation base) {
            return base.east(0.333, ordinal());
        }
    },
    East_Fifteen_Percent {
        @Override
        protected CLocation newLocation(final CWorkingAreaLocation base) {
            return base.east(0.15, ordinal());
        }
    };

    protected abstract CLocation newLocation(CWorkingAreaLocation base);

    @Override
    public void setLocation(final ContentPaneDockable dockable, final CWorkingArea workingArea) {
        final boolean found = dockable.setLocationsAside(new Filter<CDockable>() {
            @Override
            public boolean includes(final CDockable d) {
                if (d != dockable && d instanceof ContentPaneDockable) {
                    final ContentPaneDockable c = (ContentPaneDockable) d;
                    if (c.getLocation() == WorkingAreaLocation.this) {
                        return true;
                    }
                }
                return false;
            }
        });
        if (!found) {
            dockable.setLocation(this.newLocation(CLocation.working(workingArea)));
        }
    }

}
