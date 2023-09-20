package de.invesdwin.context.client.swing.frame.content;

import javax.annotation.concurrent.Immutable;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.location.CWorkingAreaLocation;
import bibliothek.util.Filter;

@Immutable
public interface IReusingWorkingAreaLocation extends IWorkingAreaLocation {

    CLocation newLocation(CWorkingAreaLocation base);

    @Override
    default void setLocation(final ContentPaneDockable dockable, final CWorkingArea workingArea) {
        setLocation(dockable, workingArea, this);
    }

    static void setLocation(final ContentPaneDockable dockable, final CWorkingArea workingArea,
            final IReusingWorkingAreaLocation location) {
        final boolean found = dockable.setLocationsAside(new Filter<CDockable>() {
            @Override
            public boolean includes(final CDockable d) {
                if (d != dockable && d instanceof ContentPaneDockable) {
                    final ContentPaneDockable c = (ContentPaneDockable) d;
                    if (c.getLocation().getId() == location.getId()) {
                        return true;
                    }
                }
                return false;
            }
        });
        if (!found) {
            dockable.setLocation(location.newLocation(CLocation.working(workingArea)));
        }
    }

}
