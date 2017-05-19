package de.invesdwin.context.client.swing.internal.content;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.jdesktop.application.Resource;
import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.ContentManager;
import org.noos.xing.mydoggy.ContentManagerListener;
import org.noos.xing.mydoggy.TabbedContentManagerUI.TabPlacement;
import org.noos.xing.mydoggy.event.ContentManagerEvent;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyTabbedContentManagerUI;

import de.invesdwin.context.client.swing.ContentPane;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.util.assertions.Assertions;

@SuppressWarnings("serial")
@NotThreadSafe
public class ContentPaneView extends AView<ContentPaneView, MyDoggyToolWindowManager> {

    private final ContentManagerListener contentRemovedListener = new ContentManagerListener() {
        @Override
        public void contentSelected(final ContentManagerEvent event) {}

        @Override
        public void contentRemoved(final ContentManagerEvent event) {
            Assertions.assertThat(EventQueue.isDispatchThread()).isTrue();
            //Also being triggered by removeView, in that case ignore it and don't call removeView again.
            final Content content = event.getContent();
            if (contentPane.containsContent(content)) {
                contentPane.removeContent(content);
            }
        }

        @Override
        public void contentAdded(final ContentManagerEvent event) {}
    };

    @Resource(key = "closeContent.Action.accelerator")
    private KeyStroke closeContentKeyStroke;
    @Inject
    private ContentPane contentPane;

    @Override
    protected ContentPaneView initModel() {
        return null;
    }

    @Override
    protected MyDoggyToolWindowManager initComponent() {
        final MyDoggyToolWindowManager toolWindowManager = new MyDoggyToolWindowManager();
        final MyDoggyTabbedContentManagerUI contentManagerUI = (MyDoggyTabbedContentManagerUI) toolWindowManager
                .getContentManager().getContentManagerUI();
        contentManagerUI.setMaximizable(false); //Without this an ArrayIndexOutOfBoundsException occurs!
        contentManagerUI.setTabPlacement(TabPlacement.BOTTOM);
        toolWindowManager.getContentManager().addContentManagerListener(new ContentManagerListener() {
            @Override
            public void contentSelected(final ContentManagerEvent event) {}

            @Override
            public void contentRemoved(final ContentManagerEvent event) {
                final JComponent component = (JComponent) event.getContent().getComponent();
                component.unregisterKeyboardAction(closeContentKeyStroke);
            }

            @Override
            public void contentAdded(final ContentManagerEvent event) {}
        });

        toolWindowManager.getContentManager().addContentManagerListener(contentRemovedListener);

        return toolWindowManager;
    }

    public Content addView(final ContentPane contentPane, final AView<?, ?> view) {
        final ContentManager contentManager = getComponent().getContentManager();
        final Content content = contentManager.addContent(view.getId(), view.getTitle(), view.getIcon(),
                view.getComponent(), view.getDescription());
        view.getComponent().registerKeyboardAction(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                contentPane.removeView(view);
            }
        }, closeContentKeyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        content.setSelected(true);
        return content;
    }

    public boolean removeView(final AView<?, ?> view) {
        return getComponent().getContentManager().removeContent(view.getContent());
    }

    public boolean containsView(final AView<?, ?> view) {
        return getComponent().getContentManager().getContent(view.getId()) != null;
    }
}
