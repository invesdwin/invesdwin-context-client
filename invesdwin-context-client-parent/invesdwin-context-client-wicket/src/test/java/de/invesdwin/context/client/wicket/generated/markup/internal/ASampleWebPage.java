package de.invesdwin.context.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar.ComponentPosition;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarComponents;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome6IconType;
import de.invesdwin.nowicket.application.AWebPage;
import de.invesdwin.nowicket.application.auth.ABaseWebApplication;

@NotThreadSafe
public abstract class ASampleWebPage extends AWebPage {

    public ASampleWebPage(final IModel<?> model) {
        super(model);
    }

    @Override
    protected Navbar newNavbar(final String id) {
        final Navbar navbar = super.newNavbar(id);

        navbar.setBrandName(Model.of("Sample"));

        navbar.addComponents(NavbarComponents.transform(ComponentPosition.LEFT,
                new NavbarButton<Void>(ABaseWebApplication.get().getHomePage(), Model.of("Home"))
                        .setIconType(FontAwesome6IconType.house_s),
                new NavbarButton<Void>(SampleModelPage.class, Model.of("Sample Menu Item"))
                        .setIconType(FontAwesome6IconType.bell_s)));

        return navbar;
    }

    @Override
    protected String getContainerClass() {
        return "container-fluid";
    }

}
