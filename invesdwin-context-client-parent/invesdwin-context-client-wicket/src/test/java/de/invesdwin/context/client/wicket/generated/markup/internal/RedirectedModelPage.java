package de.invesdwin.context.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import de.invesdwin.nowicket.generated.binding.GeneratedBinding;
import de.invesdwin.nowicket.generated.binding.processor.element.AnchorHtmlElement;
import de.invesdwin.nowicket.generated.binding.processor.element.IHtmlElement;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.BindingInterceptor;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.component.link.ModelDownloadLink;

@NotThreadSafe
public class RedirectedModelPage extends ASampleWebPage {

    public RedirectedModelPage(final RedirectedModel model) {
        super(Model.of(model));
        new GeneratedBinding(this).addBindingInterceptor(new BindingInterceptor() {
            @Override
            protected Component create(final IHtmlElement<?, ?> e) {
                //see http://stackoverflow.com/questions/7646270/how-to-use-wickets-downloadlink-with-a-file-generated-on-the-fly
                if ("multiFileDownload".equals(e.getWicketId())) {
                    final ModelDownloadLink downloadLink = new ModelDownloadLink((AnchorHtmlElement) e);
                    downloadLink.setDeleteAfterDownload(true);
                    downloadLink.setCacheDuration(java.time.Duration.ZERO);
                    return downloadLink;
                }
                return super.create(e);
            }
        }).bind();
    }
}
