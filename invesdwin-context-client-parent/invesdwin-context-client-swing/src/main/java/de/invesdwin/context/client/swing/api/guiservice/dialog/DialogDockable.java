package de.invesdwin.context.client.swing.api.guiservice.dialog;

import java.awt.Dimension;
import java.awt.Window;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;
import javax.swing.JDialog;

import bibliothek.gui.dock.common.layout.RequestDimension;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.context.client.swing.util.RequestDimensions;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;

@NotThreadSafe
public class DialogDockable extends JDialog implements IDockable {

    private final String uniqueId;
    private AView<?, ?> view;

    @GuardedBy("this")
    private Instant requestResizeStart;
    @GuardedBy("this")
    private RequestDimension requestResizeDimension;
    @GuardedBy("this")
    private Duration requestResizeTimeout;

    public DialogDockable(final String uniqueId, final Window owner) {
        super(owner);
        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public JComponent getComponent() {
        return (JComponent) getContentPane().getComponent(0);
    }

    @Override
    public void setComponent(final JComponent component) {
        getContentPane().removeAll();
        getContentPane().add(component);
    }

    @Override
    public void setView(final AView<?, ?> view) {
        this.view = view;
    }

    @Override
    public AView<?, ?> getView() {
        return view;
    }

    @Override
    public boolean isResizeLocked() {
        return false;
    }

    @Override
    public void setResizeLocked(final boolean resizeLocked) {
        //noop
    }

    @Override
    public boolean isResizeLockedHorizontally() {
        return false;
    }

    @Override
    public void setResizeLockedHorizontally(final boolean resizeLockedHorizontally) {
        //noop
    }

    @Override
    public boolean isResizeLockedVertically() {
        return false;
    }

    @Override
    public void setResizeLockedVertically(final boolean resizeLockedVertically) {
        //noop
    }

    @Override
    public void setResizeRequest(final RequestDimension dimension, final Duration timeout) {
        this.requestResizeStart = new Instant();
        this.requestResizeDimension = dimension;
        this.requestResizeTimeout = timeout;
        if (RequestDimensions.isResizeRequestPending(requestResizeDimension, getSize())) {
            EventDispatchThreadUtil.invokeLater(this::processResizeRequest);
        } else {
            resetResizeRequest();
        }
    }

    private void processResizeRequest() {
        final Instant requestResizeStartCopy;
        final RequestDimension requestResizeDimensionCopy;
        final Duration requestResizeTimeoutCopy;
        synchronized (this) {
            requestResizeStartCopy = requestResizeStart;
            requestResizeDimensionCopy = requestResizeDimension;
            requestResizeTimeoutCopy = requestResizeTimeout;
        }
        if (requestResizeStartCopy == null || requestResizeStartCopy.isGreaterThanOrEqualTo(requestResizeTimeoutCopy)) {
            resetResizeRequest();
            return;
        }
        final Dimension actual = getSize();
        if (RequestDimensions.isResizeRequestPending(requestResizeDimensionCopy, actual)) {
            setSize(RequestDimensions.toDimension(requestResizeDimensionCopy, actual));
            EventDispatchThreadUtil.invokeLater(this::processResizeRequest);
        } else {
            resetResizeRequest();
        }
    }

    private void resetResizeRequest() {
        synchronized (this) {
            requestResizeStart = null;
            requestResizeDimension = null;
            requestResizeTimeout = null;
        }
    }

}
