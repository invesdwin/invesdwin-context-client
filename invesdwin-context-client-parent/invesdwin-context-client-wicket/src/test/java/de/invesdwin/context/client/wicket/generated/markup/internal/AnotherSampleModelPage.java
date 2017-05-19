package de.invesdwin.context.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.model.Model;

import de.invesdwin.nowicket.generated.binding.GeneratedBinding;

@NotThreadSafe
public class AnotherSampleModelPage extends ASampleWebPage {

    public AnotherSampleModelPage() {
        this(new AnotherSampleModel());
    }

    public AnotherSampleModelPage(final AnotherSampleModel model) {
        super(Model.of(model));
        new GeneratedBinding(this).bind();
    }

}
