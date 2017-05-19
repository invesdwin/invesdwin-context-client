package de.invesdwin.common.client.wicket.generated.markup.internal.bugfix;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
public class BugfixComplexValueObject extends AValueObject {

    private String toString;

    public BugfixComplexValueObject(final String toString) {
        this.toString = toString;
    }

    public String getToString() {
        return toString;
    }

    public void setToString(final String toString) {
        this.toString = toString;
    }

    @Override
    public String toString() {
        return toString;
    }

}
