package de.invesdwin.common.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.norva.beanpath.annotation.Tabbed;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
@GeneratedMarkup
public class TabbedRootSampleModel extends AValueObject {

    private final AnotherSampleModel parent;
    private final TabbedContainerSampleModel tabs;

    public TabbedRootSampleModel(final AnotherSampleModel parent) {
        this.parent = parent;
        tabs = new TabbedContainerSampleModel();
    }

    @Tabbed
    public TabbedContainerSampleModel getTabs() {
        return tabs;
    }

    public AnotherSampleModel back() {
        return parent;
    }

}
