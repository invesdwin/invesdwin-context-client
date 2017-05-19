package de.invesdwin.common.client.wicket.generated.markup.internal;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.math3.random.JDKRandomGenerator;

import de.invesdwin.norva.beanpath.annotation.ColumnOrder;
import de.invesdwin.util.bean.AValueObject;

@ColumnOrder({ "tab1", "tab2", "tab3", "tab4" })
@NotThreadSafe
public class TabbedContainerSampleModel extends AValueObject {

    private final SampleModel tab1;
    private final SampleModel tab2;
    private final SampleModel tab3;
    private final SampleModel tab4;

    public TabbedContainerSampleModel() {
        tab1 = new SampleModel(1);
        tab2 = new SampleModel(2);
        tab3 = new SampleModel(3);
        tab4 = new SampleModel(4);
    }

    public SampleModel getTab1() {
        return tab1;
    }

    public SampleModel getTab2() {
        return tab2;
    }

    public SampleModel getTab3() {
        return tab3;
    }

    public boolean hideTab3() {
        return new JDKRandomGenerator().nextBoolean();
    }

    public SampleModel getTab4() {
        return tab4;
    }

}
