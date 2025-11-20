package de.invesdwin.context.client.swing.frame.content;

import java.awt.Dimension;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;

import bibliothek.gui.dock.common.layout.RequestDimension;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.context.client.swing.util.RequestDimensions;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;

@NotThreadSafe
public class StandaloneDockable implements IDockable {

    private final String uniqueId;
    private JComponent component;
    private AView<?, ?> view;
    private String title;

    @GuardedBy("this")
    private Instant requestResizeStart;
    @GuardedBy("this")
    private RequestDimension requestResizeDimension;
    @GuardedBy("this")
    private Duration requestResizeTimeout;

    public StandaloneDockable(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void setComponent(final JComponent component) {
        this.component = component;
    }

    @Override
    public void requestFocus() {
        component.requestFocus();
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
    public Dimension getSize() {
        return component.getSize();
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
            EventDispatchThreadUtil.invokeLaterIfNotInEDT(() -> processResizeRequest(0));
        } else {
            resetResizeRequest();
        }
    }

    private void processResizeRequest(final int successCount) {
        final Instant requestResizeStartCopy;
        final RequestDimension requestResizeDimensionCopy;
        final Duration requestResizeTimeoutCopy;
        synchronized (this) {
            requestResizeStartCopy = requestResizeStart;
            requestResizeDimensionCopy = requestResizeDimension;
            requestResizeTimeoutCopy = requestResizeTimeout;
        }
        if (requestResizeStartCopy == null || requestResizeStartCopy.isGreaterThanOrEqualTo(requestResizeTimeoutCopy)) {
            if (successCount >= ContentPaneDockable.PROCESS_RESIZE_REQUEST_VERIFICATION_COUNT) {
                resetResizeRequest();
            } else {
                EventDispatchThreadUtil.invokeLater(() -> processResizeRequest(successCount + 1));
            }
            return;
        }
        final Dimension actual = getSize();
        if (RequestDimensions.isResizeRequestPending(requestResizeDimensionCopy, actual)) {
            component.setSize(RequestDimensions.toDimension(requestResizeDimensionCopy, actual));
            EventDispatchThreadUtil.invokeLater(() -> processResizeRequest(0));
        } else {
            if (successCount >= ContentPaneDockable.PROCESS_RESIZE_REQUEST_VERIFICATION_COUNT) {
                resetResizeRequest();
            } else {
                EventDispatchThreadUtil.invokeLater(() -> processResizeRequest(successCount + 1));
            }
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
