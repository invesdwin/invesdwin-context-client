package de.invesdwin.context.client.wicket.generated.markup.internal.bugfix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.invesdwin.context.client.wicket.generated.markup.internal.SampleModel;
import de.invesdwin.context.client.wicket.generated.markup.internal.SampleModelPanel;
import de.invesdwin.nowicket.application.AWebPage;
import de.invesdwin.nowicket.generated.binding.GeneratedBinding;
import de.invesdwin.nowicket.generated.binding.processor.element.IHtmlElement;
import de.invesdwin.nowicket.generated.binding.processor.element.SelectHtmlElement;
import de.invesdwin.nowicket.generated.binding.processor.element.TabbedHtmlElement;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.BindingInterceptor;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.component.collapsible.ModelCollapsibleList;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.component.collapsible.accordion.ModelAccordion;
import de.invesdwin.nowicket.generated.binding.processor.visitor.builder.component.palette.ModelPalette;

@NotThreadSafe
public class BugfixPage extends AWebPage {

    public BugfixPage(final IModel<Bugfix> model) {
        super(model);
        new GeneratedBinding(this).addBindingInterceptor(new BindingInterceptor() {

            @Override
            protected Component create(final IHtmlElement<?, ?> e) {
                if ("tabs".equals(e.getWicketId())) {
                    return new ModelAccordion((TabbedHtmlElement) e);
                }
                if ("tabsAllOpenAllowed".equals(e.getWicketId())) {
                    return new ModelCollapsibleList((TabbedHtmlElement) e);
                }
                if ("accordionFromList".equals(e.getWicketId())) {
                    /*
                     * could also use ModelCollapsibleList here to allow multiple to be open at the same time
                     */
                    final IModel<Collection<? extends ITab>> tabs = new IModel<Collection<? extends ITab>>() {
                        @Override
                        public Collection<? extends ITab> getObject() {
                            final List<ITab> tabs = new ArrayList<ITab>();
                            for (final SampleModel l : model.getObject().getAccordionFromList()) {
                                tabs.add(new ITab() {
                                    @Override
                                    public IModel<String> getTitle() {
                                        return Model.of("accordion from list - " + l.getOne());
                                    }

                                    @Override
                                    public WebMarkupContainer getPanel(final String containerId) {
                                        return new SampleModelPanel(containerId, Model.of(l));
                                    }

                                    @Override
                                    public boolean isVisible() {
                                        return true;
                                    }

                                });
                            }
                            return tabs;
                        }

                    };
                    return new ModelAccordion(e.getWicketId(), tabs) {
                        @Override
                        protected Collapsible newAccordionCollapsible(final String componentId, final ITab tab,
                                final int index) {
                            return new Collapsible(componentId, tab, index) {
                                @Override
                                protected Component newTitle(final String markupId, final ITab tab) {
                                    final IModel<String> badgeModel = new IModel<String>() {
                                        @Override
                                        public String getObject() {
                                            final SampleModel modelObject = (SampleModel) getPanelModel().getObject();
                                            return modelObject.getTwo();
                                        }
                                    };
                                    return new ACustomTitleBadgePanel(markupId, badgeModel) {

                                        @Override
                                        protected Component newLink(final String id) {
                                            return superNewTitle(id, tab);
                                        }
                                    };
                                }

                                protected Component superNewTitle(final String markupId, final ITab tab) {
                                    return super.newTitle(markupId, tab);
                                }
                            };
                        }
                    };
                }
                if ("multiSelectPalette".equals(e.getWicketId())) {
                    return new ModelPalette((SelectHtmlElement) e);
                }
                return null;
            }
        }).bind();
    }
}
