package de.invesdwin.context.client.swing.component.logviewer;

import de.invesdwin.norva.marker.ISerializableValueObject;
import de.invesdwin.util.collections.iterable.ICloseableIterable;
import de.invesdwin.util.time.date.FDate;

public interface ILogViewerSource extends ISerializableValueObject {

    ICloseableIterable<LogViewerEntry> getLogViewerEntries(FDate from, Integer tailCount);

}
