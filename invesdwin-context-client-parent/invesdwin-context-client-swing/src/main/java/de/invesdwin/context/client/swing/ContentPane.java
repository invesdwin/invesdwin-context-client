package de.invesdwin.context.client.swing;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.noos.xing.mydoggy.Content;

import de.invesdwin.aspects.annotation.EventDispatchThread;
import de.invesdwin.aspects.annotation.EventDispatchThread.InvocationType;
import de.invesdwin.context.client.swing.api.AView;
import de.invesdwin.context.client.swing.internal.content.ContentPaneView;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public class ContentPane {

    @GuardedBy("@EventDispatchThread")
    private final Map<String, AView<?, ?>> id_sichtbareView = new HashMap<String, AView<?, ?>>();

    @Inject
    private ContentPaneView contentPaneView;

    /**
     * Throws an exception if the View has already been added.
     */
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void addView(final AView<?, ?> view) {
        Assertions.assertThat(containsView(view)).as("View [%s] is already being displayed.", view.getId()).isFalse();
        final Content content = contentPaneView.addView(ContentPane.this, view);
        view.setContent(ContentPane.this, content);
        Assertions.assertThat(id_sichtbareView.put(view.getId(), view)).isNull();
    }

    /**
     * Throws an exception if the View has not been added yet.
     */
    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void removeView(final AView<?, ?> view) {
        Assertions.assertThat(id_sichtbareView.remove(view.getId())).isNotNull();
        //May also be called by contentRemoved, in that case we should not trigger that again.
        if (containsView(view)) {
            Assertions.assertThat(contentPaneView.removeView(view)).isTrue();
        }
        view.setContent(ContentPane.this, null);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void removeContent(final Content content) {
        final AView<?, ?> view = id_sichtbareView.get(content.getId());
        Assertions.assertThat(view).as("No View for Content [%s] found.", content.getId()).isNotNull();
        removeView(view);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public boolean containsContent(final Content content) {
        return id_sichtbareView.containsKey(content.getId());
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public boolean containsView(final AView<?, ?> view) {
        return contentPaneView.containsView(view);
    }

    @EventDispatchThread(InvocationType.INVOKE_AND_WAIT)
    public void reset() {
        for (final Content content : contentPaneView.getComponent().getContentManager().getContents()) {
            removeContent(content);
        }
    }

}
