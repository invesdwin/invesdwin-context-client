package de.invesdwin.context.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import de.invesdwin.nowicket.generated.binding.GeneratedBinding;

@NotThreadSafe
public class SampleModelPanel extends Panel {

    public SampleModelPanel(final String id, final IModel<SampleModel> model) {
        super(id, model);
        new GeneratedBinding(this).bind();
    }

}
