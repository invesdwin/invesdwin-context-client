package de.invesdwin.context.client.swing.api.guiservice;

import java.awt.Component;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.beans.init.MergedContext;
import de.invesdwin.context.client.swing.util.Components;

@Named
@Immutable
public class GuiService implements IGuiService {

    @Inject
    private StatusBar statusBar;
    @Inject
    private ContentPane contentPane;
    @Inject
    private SplashScreen splashScreen;

    public static IGuiService get() {
        return MergedContext.getInstance().getBean(IGuiService.class);
    }

    @Override
    public StatusBar getStatusBar() {
        return statusBar;
    }

    @Override
    public SplashScreen getSplashScreen() {
        return splashScreen;
    }

    @Override
    public ContentPane getContentPane() {
        return contentPane;
    }

    @Override
    public void processRequestFinally(final Component component) {
        Components.updateAllViews(component);
    }

}
