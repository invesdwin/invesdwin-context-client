package de.invesdwin.context.client.swing.frame.content;

import java.awt.Component;
import java.awt.Dimension;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.Icon;
import javax.swing.JComponent;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.layout.RequestDimension;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.client.swing.api.binding.component.button.SubmitButtonBinding;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.api.view.IDockable;
import de.invesdwin.context.client.swing.util.RequestDimensions;
import de.invesdwin.util.swing.AComponentFinder;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;

@NotThreadSafe
public class ContentPaneDockable extends DefaultSingleCDockable implements IDockable {

    private AView<?, ?> view;
    private final IWorkingAreaLocation location;

    @GuardedBy("this")
    private Instant requestResizeStart;
    @GuardedBy("this")
    private RequestDimension requestResizeDimension;
    @GuardedBy("this")
    private Duration requestResizeTimeout;

    public ContentPaneDockable(final String id, final Icon icon, final String title, final JComponent component,
            final IWorkingAreaLocation location) {
        super(id, icon, title, component);
        this.location = location;
        setCloseable(true);
    }

    public IWorkingAreaLocation getLocation() {
        return location;
    }

    @Override
    public void setTitle(final String title) {
        setTitleText(title);
    }

    @Override
    public String getTitle() {
        return getTitleText();
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
    public Component getFocusComponent() {
        final Component focusComponent = super.getFocusComponent();
        if (focusComponent != null) {
            return focusComponent;
        } else {
            //fallback
            return AComponentFinder.DEFAULT_FOCUS.find(getComponent());
        }
    }

    @Override
    public void setVisible(final boolean visible) {
        if (view != null && isVisible() && !visible) {
            final BindingGroup bindingGroup = view.getBindingGroup();
            if (bindingGroup != null) {
                final SubmitButtonBinding defaultCloseOperation = bindingGroup.getDefaultCloseOperation();
                if (defaultCloseOperation != null) {
                    //binding handles removal
                    defaultCloseOperation.doClick();
                    return;
                }
            }
            //content pane handles removal
            GuiService.get().getContentPane().removeView(view);
        } else {
            super.setVisible(visible);
        }
    }

    @Override
    public JComponent getContentPane() {
        return (JComponent) super.getContentPane();
    }

    @Override
    public void requestFocus() {
        toFront(getFocusComponent());
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
        if (RequestDimensions.isResizeRequestPending(requestResizeDimensionCopy, getSize())) {
            setResizeRequest(requestResizeDimensionCopy, true);
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

    @Override
    public Dimension getSize() {
        return super.getContentPane().getSize();
    }

}
