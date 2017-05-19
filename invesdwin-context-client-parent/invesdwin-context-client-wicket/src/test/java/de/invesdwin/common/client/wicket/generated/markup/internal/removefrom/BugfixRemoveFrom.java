package de.invesdwin.common.client.wicket.generated.markup.internal.removefrom;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.common.client.wicket.generated.markup.internal.bugfix.BugfixComplexValueObject;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@GeneratedMarkup
@NotThreadSafe
public class BugfixRemoveFrom extends AValueObject {

    private final List<BugfixComplexValueObject> table = new ArrayList<BugfixComplexValueObject>();

    public BugfixRemoveFrom() {
        for (int i = 0; i < 3; i++) {
            table.add(new BugfixComplexValueObject("" + i));
        }
    }

    public List<BugfixComplexValueObject> getTable() {
        return table;
    }

    public void removeFromTable(final BugfixComplexValueObject value) {
        table.remove(value);
    }

}
