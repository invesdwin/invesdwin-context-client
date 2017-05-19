package de.invesdwin.common.client.wicket.generated.markup.internal.bugfix;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@NotThreadSafe
public abstract class ACustomTitleBadgePanel extends Panel {

    public ACustomTitleBadgePanel(final String id, final IModel<String> model) {
        super(id);
        final Label badge = new Label("badge", model);
        add(badge);
        final Component link = newLink("link");
        add(link);
    }

    protected abstract Component newLink(String id);

}
