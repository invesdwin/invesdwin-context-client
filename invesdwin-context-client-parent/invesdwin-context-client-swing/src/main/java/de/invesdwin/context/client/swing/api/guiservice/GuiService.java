package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.util.Stack;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.JDialog;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskService;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.api.binding.component.button.SubmitButtonBinding;
import de.invesdwin.context.client.swing.api.guiservice.dialog.DialogDockable;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.context.client.swing.frame.content.DockableIdGenerator;
import de.invesdwin.context.client.swing.frame.content.IWorkingAreaLocation;
import de.invesdwin.context.client.swing.util.SubmitAllViewsHelper;
import de.invesdwin.context.client.swing.util.UpdateAllViewsHelper;
import de.invesdwin.context.client.swing.util.Views;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.swing.Dialogs;
import de.invesdwin.util.swing.EventDispatchThreadUtil;
import de.invesdwin.util.swing.listener.ComponentListenerSupport;
import de.invesdwin.util.swing.listener.WindowListenerSupport;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FDate;
import jakarta.inject.Inject;

@ThreadSafe
public class GuiService implements IGuiService {

    private final Stack<DialogDockable> dialogs = new Stack<DialogDockable>();
    private volatile boolean forced = false;

    @Inject
    private StatusBar statusBar;
    @Inject
    private ContentPane contentPane;
    @Inject
    private PersistentLayoutManager persistentLayoutManager;
    @GuardedBy("none for performance")
    private TaskService taskService;
    @GuardedBy("none for performance")
    private ApplicationContext applicationContext;

    public static IGuiService get() {
        return MergedContext.getInstance().getBean(IGuiService.class);
    }

    @Override
    public StatusBar getStatusBar() {
        return statusBar;
    }

    @Override
    public ContentPane getContentPane() {
        return contentPane;
    }

    @Override
    public PersistentLayoutManager getPersistentLayoutManager() {
        return persistentLayoutManager;
    }

    @Override
    public TaskService getTaskService() {
        if (taskService == null) {
            taskService = DelegateRichApplication.getInstance().getContext().getTaskService();
        }
        return taskService;
    }

    @Override
    public boolean hideModalView(final AView<?, ?> view) {
        if (getModalViewShowing() == view) {
            return hideModalView();
        }
        return false;
    }

    @Override
    public boolean hideModalView() {
        if (isModalViewShowing()) {
            final DialogDockable dialog = dialogs.pop();
            dialog.getView().setDockable(null);
            dialog.dispose();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void showModalView(final AView<?, ?> view) {
        showModalView(view, null);
    }

    @Override
    public void showModalView(final AView<?, ?> view, final Dimension dimension) {
        final Window window = getWindow();
        final DialogDockable dialog = new DialogDockable(DockableIdGenerator.newId(view), window);
        dialog.setModal(true);
        dialogs.push(dialog);
        final Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(view.getComponent());
        dialog.setTitle(view.getTitle());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        final SubmitButtonBinding defaultCloseOperation = view.getBindingGroup().getDefaultCloseOperation();
        if (defaultCloseOperation != null) {
            dialog.addWindowListener(new WindowListenerSupport() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    defaultCloseOperation.doClick();
                }
            });
        }
        Dialogs.installEscapeCloseOperation(dialog);
        view.setDockable(dialog);
        dialog.pack();
        if (dimension != null) {
            if (dimension.getHeight() > 0D && dimension.getWidth() > 0D) {
                dialog.setSize(dimension);
            }
        } else {
            dialog.setSize(new Dimension(400, 200));
        }
        dialog.setMinimumSize(new Dimension(100, 100));
        dialog.setLocationRelativeTo(window);
        dialog.addComponentListener(new ComponentListenerSupport() {
            @Override
            public void componentShown(final ComponentEvent e) {
                Views.triggerOnShowing(view);
            }
        });
        //this call blocks for modal dialogs, which is expected
        dialog.setVisible(true);
        EventDispatchThreadUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                //make sure task bar does not get in front on windows
                dialog.setLocationRelativeTo(window);
            }
        });
    }

    @Override
    public boolean isModalViewShowing() {
        return !dialogs.isEmpty();
    }

    @Override
    public boolean isModalViewShowing(final AView<?, ?> view) {
        return getModalViewShowing() == view;
    }

    @Override
    public AView<?, ?> getModalViewShowing() {
        if (dialogs.isEmpty()) {
            return null;
        }
        return dialogs.peek().getView();
    }

    @Override
    public Window getWindow() {
        if (dialogs.isEmpty()) {
            return Dialogs.getRootFrame();
        } else {
            return dialogs.peek();
        }
    }

    @Override
    public void showView(final AView<?, ?> view, final IWorkingAreaLocation location, final boolean requestFocus) {
        getContentPane().showView(view, location, requestFocus);
    }

    @Override
    public ResourceMap getResourceMap(final Class<?> clazz) {
        if (applicationContext == null) {
            applicationContext = DelegateRichApplication.getInstance().getContext();
        }
        return applicationContext.getResourceMap(clazz);
    }

    @Override
    public void updateAllViews(final AView<?, ?> view) {
        updateAllViews(view.getComponent());
    }

    //invokeAndWait to not overload the UI thread
    @Override
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void updateAllViews(final Component component) {
        UpdateAllViewsHelper.updateAllViews(component);
    }

    @Override
    public void submitAllViews(final AView<?, ?> view) {
        submitAllViews(view.getComponent());
    }

    //invokeAndWait to not overload the UI thread
    @Override
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void submitAllViews(final Component component) {
        SubmitAllViewsHelper.submitAllViews(component);
    }

    @Override
    public boolean isControlDown() {
        return getContentPane().isControlDown();
    }

    @Override
    public boolean isShiftDown() {
        return getContentPane().isShiftDown();
    }

    @Override
    public boolean isAltDown() {
        return getContentPane().isAltDown();
    }

    @Override
    public boolean isAltGraphDown() {
        return getContentPane().isAltGraphDown();
    }

    @Override
    public boolean isMetaDown() {
        return getContentPane().isMetaDown();
    }

    @Override
    public boolean isModifierDown() {
        return getContentPane().isModifierDown();
    }

    @Override
    public FDate getLastMouseClickTime() {
        return getContentPane().getLastMouseClickTime();
    }

    @Override
    public Instant getLastMouseClickInstant() {
        return getContentPane().getLastMouseClickInstant();
    }

    public static String i18n(final Class<?> clazz, final String value) {
        return i18n(clazz, value, value);
    }

    public static String i18n(final Class<?> clazz, final String value, final String defaultValue) {
        if (Strings.isBlank(value)) {
            return value;
        }
        final ResourceMap resourceMap = DelegateRichApplication.getInstance().getContext().getResourceMap(clazz);
        String i18n = resourceMap.getString(value);
        if (i18n == null && defaultValue != value) {
            i18n = resourceMap.getString(defaultValue);
        }
        if (i18n == null) {
            i18n = defaultValue;
        }
        return i18n;
    }

    public static String i18n(final AView<?, ?> view, final String value) {
        return i18n(view, value, value);
    }

    public static String i18n(final AView<?, ?> view, final String value, final String defaultValue) {
        if (Strings.isBlank(value)) {
            return value;
        }
        String i18n = view.getModel().getResourceMap().getString(value);
        if (i18n == null) {
            i18n = view.getResourceMap().getString(value);
            if (i18n == null && defaultValue != value) {
                i18n = view.getModel().getResourceMap().getString(defaultValue);
                if (i18n == null) {
                    i18n = view.getResourceMap().getString(defaultValue);
                }
            }
        }
        if (i18n == null) {
            i18n = defaultValue;
        }
        return i18n;
    }

    public static String i18n(final AModel model, final String value) {
        return i18n(model, value, value);
    }

    public static String i18n(final AModel model, final String value, final String defaultValue) {
        if (Strings.isBlank(value)) {
            return value;
        }
        String i18n = model.getResourceMap().getString(value);
        if (i18n == null && defaultValue != value) {
            i18n = model.getResourceMap().getString(defaultValue);
        }
        if (i18n == null) {
            i18n = defaultValue;
        }
        return i18n;
    }

    public static String i18n(final ResourceMap resourceMap, final String value) {
        return i18n(resourceMap, value, value);
    }

    public static String i18n(final ResourceMap resourceMap, final String value, final String defaultValue) {
        if (Strings.isBlank(value)) {
            return value;
        }
        if (resourceMap == null) {
            return defaultValue;
        }
        String i18n = resourceMap.getString(value);
        if (i18n == null && defaultValue != value) {
            i18n = resourceMap.getString(defaultValue);
        }
        if (i18n == null) {
            i18n = defaultValue;
        }
        return i18n;
    }

    @Override
    public boolean isForced() {
        return forced;
    }

    @Override
    public boolean setForced(final boolean forced) {
        final boolean forcedBefore = this.forced;
        this.forced = forced;
        return forcedBefore;
    }

}
