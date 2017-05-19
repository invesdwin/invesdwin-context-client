package de.invesdwin.common.client.wicket.examples.guestbook;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.invesdwin.common.client.wicket.examples.AExampleWebPage;
import de.invesdwin.nowicket.generated.binding.GeneratedBinding;
import de.invesdwin.nowicket.generated.binding.processor.element.IHtmlElement;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.BindingInterceptor;

@NotThreadSafe
public class GuestbookExamplePage extends AExampleWebPage {

    public GuestbookExamplePage() {
        this(Model.of(new GuestbookExample()));
    }

    public GuestbookExamplePage(final IModel<GuestbookExample> model) {
        super(model);
        new GeneratedBinding(this).withBindingInterceptor(new BindingInterceptor() {

            @Override
            protected Component create(final IHtmlElement<?, ?> e) {
                if ("entriesList".equals(e.getWicketId())) {
                    return new MarkupContainer(e.getWicketId()) {
                        @Override
                        public boolean isVisible() {
                            return !model.getObject().getEntries().isEmpty();
                        };
                    };
                }
                return super.create(e);
            }
        }).bind();
    }
}
