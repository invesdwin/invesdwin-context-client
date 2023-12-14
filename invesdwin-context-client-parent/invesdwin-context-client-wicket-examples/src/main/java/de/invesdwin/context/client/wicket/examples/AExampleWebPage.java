package de.invesdwin.context.client.wicket.examples;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.dropdown.MenuBookmarkablePageLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar.ComponentPosition;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarComponents;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarDropDownButton;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome6IconType;
import de.invesdwin.context.client.wicket.cas.page.CasLoginPage;
import de.invesdwin.context.client.wicket.cas.page.CasLogoutPage;
import de.invesdwin.context.client.wicket.examples.guestbook.GuestbookExamplePage;
import de.invesdwin.context.client.wicket.examples.secure.cas.SpringCasSecurePage;
import de.invesdwin.context.client.wicket.examples.secure.kerberos.SpringKerberosSecurePage;
import de.invesdwin.context.client.wicket.examples.secure.local.SpringSecurePage;
import de.invesdwin.context.client.wicket.examples.secure.local.WicketSecurePage;
import de.invesdwin.context.client.wicket.examples.secure.saml.SpringSamlSecurePage;
import de.invesdwin.context.client.wicket.saml.page.SamlLoginPage;
import de.invesdwin.context.client.wicket.saml.page.SamlLogoutPage;
import de.invesdwin.nowicket.application.AWebPage;
import de.invesdwin.nowicket.application.auth.ABaseWebApplication;
import de.invesdwin.nowicket.application.auth.Roles;
import de.invesdwin.nowicket.page.auth.defaultpage.DefaultSignInPage;
import de.invesdwin.nowicket.page.auth.defaultpage.DefaultSignOutPage;

@NotThreadSafe
public abstract class AExampleWebPage extends AWebPage {

    public AExampleWebPage(final IModel<?> model) {
        super(model);
    }

    @Override
    protected Navbar newNavbar(final String id) {
        final Navbar navbar = super.newNavbar(id);

        navbar.addComponents(NavbarComponents.transform(ComponentPosition.LEFT,
                new NavbarButton<Void>(GuestbookExamplePage.class, new ResourceModel("menu.guestbook"))
                        .setIconType(FontAwesome6IconType.book_s)));
        navbar.addComponents(NavbarComponents.transform(ComponentPosition.LEFT,
                new NavbarDropDownButton(new ResourceModel("menu.secure.local")) {

                    @Override
                    public boolean isActive(final Component item) {
                        return false;
                    }

                    @Override
                    protected List<AbstractLink> newSubMenuButtons(final String buttonMarkupId) {
                        final List<AbstractLink> subMenu = new ArrayList<AbstractLink>();

                        subMenu.add(new MenuBookmarkablePageLink<Void>(WicketSecurePage.class,
                                new ResourceModel("menu.wicketsecure")));
                        subMenu.add(new MenuBookmarkablePageLink<Void>(SpringSecurePage.class,
                                new ResourceModel("menu.springsecure")));

                        if (Roles.isAuthenticated()) {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(ABaseWebApplication.get().getSignOutPage(),
                                    new ResourceModel("menu.sign.out")));
                        } else {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(ABaseWebApplication.get().getSignInPage(),
                                    new ResourceModel("menu.sign.in")));
                        }

                        return subMenu;
                    }

                }));

        navbar.addComponents(NavbarComponents.transform(ComponentPosition.LEFT,
                new NavbarDropDownButton(new ResourceModel("menu.secure.saml")) {

                    @Override
                    public boolean isActive(final Component item) {
                        return false;
                    }

                    @Override
                    protected List<AbstractLink> newSubMenuButtons(final String buttonMarkupId) {
                        final List<AbstractLink> subMenu = new ArrayList<AbstractLink>();

                        subMenu.add(new MenuBookmarkablePageLink<Void>(SpringSamlSecurePage.class,
                                new ResourceModel("menu.springsamlsecure")));

                        if (Roles.isAuthenticated()) {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(SamlLogoutPage.class,
                                    new ResourceModel("menu.sign.out")));
                        } else {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(SamlLoginPage.class,
                                    new ResourceModel("menu.sign.in")));
                        }

                        return subMenu;
                    }

                }));

        navbar.addComponents(NavbarComponents.transform(ComponentPosition.LEFT,
                new NavbarDropDownButton(new ResourceModel("menu.secure.kerberos")) {

                    @Override
                    public boolean isActive(final Component item) {
                        return false;
                    }

                    @Override
                    protected List<AbstractLink> newSubMenuButtons(final String buttonMarkupId) {
                        final List<AbstractLink> subMenu = new ArrayList<AbstractLink>();

                        subMenu.add(new MenuBookmarkablePageLink<Void>(SpringKerberosSecurePage.class,
                                new ResourceModel("menu.springkerberossecure")));

                        if (Roles.isAuthenticated()) {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(DefaultSignOutPage.class,
                                    new ResourceModel("menu.sign.out")));
                        } else {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(DefaultSignInPage.class,
                                    new ResourceModel("menu.sign.in")));
                        }

                        return subMenu;
                    }

                }));

        navbar.addComponents(NavbarComponents.transform(ComponentPosition.LEFT,
                new NavbarDropDownButton(new ResourceModel("menu.secure.cas")) {

                    @Override
                    public boolean isActive(final Component item) {
                        return false;
                    }

                    @Override
                    protected List<AbstractLink> newSubMenuButtons(final String buttonMarkupId) {
                        final List<AbstractLink> subMenu = new ArrayList<AbstractLink>();

                        subMenu.add(new MenuBookmarkablePageLink<Void>(SpringCasSecurePage.class,
                                new ResourceModel("menu.springcassecure")));

                        if (Roles.isAuthenticated()) {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(CasLogoutPage.class,
                                    new ResourceModel("menu.sign.out")));
                        } else {
                            subMenu.add(new MenuBookmarkablePageLink<Void>(CasLoginPage.class,
                                    new ResourceModel("menu.sign.in")));
                        }

                        return subMenu;
                    }

                }));

        return navbar;
    }

}
