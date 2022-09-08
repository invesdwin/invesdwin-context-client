package de.invesdwin.context.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.invesdwin.nowicket.application.IWebApplicationConfig;
import de.invesdwin.nowicket.component.navbar.Navbar;
import de.invesdwin.nowicket.generated.binding.GeneratedBinding;

@NotThreadSafe
public class TabbedRootSampleModelPage extends ASampleWebPage {

    public TabbedRootSampleModelPage(final IModel<TabbedRootSampleModel> model) {
        super(model);
        new GeneratedBinding(this).bind();
    }

    @Override
    protected String getContainerClass() {
        return "container";
    }

    @Override
    protected Navbar newNavbar(final String id) {
        final Navbar navbar = super.newNavbar(id);
        navbar.setBrandName(Model.of("(container instead of container-fluid)"));
        navbar.setBrandImage(IWebApplicationConfig.DEFAULT_FAVICON, Model.of("logo"));
        return navbar;
    }

}
