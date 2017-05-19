package de.invesdwin.common.client.wicket.generated.markup.internal.suffix;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import de.invesdwin.common.client.wicket.generated.markup.internal.ASampleWebPage;
import de.invesdwin.nowicket.generated.binding.GeneratedBinding;
import de.invesdwin.nowicket.generated.binding.processor.element.IHtmlElement;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.BindingInterceptor;

@NotThreadSafe
public class SuffixModelPage extends ASampleWebPage {

    public SuffixModelPage() {
        this(new SuffixModelSuffix());
    }

    public SuffixModelPage(final SuffixModelSuffix model) {
        super(Model.of(model));
        new GeneratedBinding(this).withBindingInterceptor(new BindingInterceptor() {
            @Override
            protected Component create(final IHtmlElement<?, ?> e) {
                if ("suffixPanel".equals(e.getWicketId())) {
                    return new SuffixModelPanel(e.getWicketId(), Model.of(new SuffixModelSuffix()));
                }
                return super.create(e);
            }
        }).bind();

    }
}
