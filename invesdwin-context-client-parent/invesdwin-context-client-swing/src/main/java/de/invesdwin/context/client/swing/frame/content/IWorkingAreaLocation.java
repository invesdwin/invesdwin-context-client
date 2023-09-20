package de.invesdwin.context.client.swing.frame.content;

import bibliothek.gui.dock.common.CWorkingArea;

public interface IWorkingAreaLocation {

    int getId();

    void setLocation(ContentPaneDockable dockable, CWorkingArea workingArea);

}
