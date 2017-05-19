package de.invesdwin.common.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.model.Model;

import de.invesdwin.nowicket.generated.binding.GeneratedBinding;

@NotThreadSafe
public class SampleModelPage extends ASampleWebPage {

    public SampleModelPage() {
        this(new SampleModel());
    }

    public SampleModelPage(final SampleModel model) {
        super(Model.of(model));
        new GeneratedBinding(this).bind();
    }

}
