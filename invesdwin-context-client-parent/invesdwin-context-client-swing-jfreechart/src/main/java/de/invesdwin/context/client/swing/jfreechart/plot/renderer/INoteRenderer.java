package de.invesdwin.context.client.swing.jfreechart.plot.renderer;

import java.util.Collection;

import de.invesdwin.context.client.swing.jfreechart.plot.annotation.XYNoteIconAnnotation;

public interface INoteRenderer {

    Collection<XYNoteIconAnnotation> getVisibleNoteIcons();

}
