package de.invesdwin.context.client.swing.jfreechart.plot.dataset;

import java.util.List;

import org.jfree.data.general.Dataset;

public interface IDrawIncompleteBar {

    boolean isDrawIncompleteBar();

    static boolean isDrawIncompleteBar(final List<?> obj) {
        if (obj instanceof IDrawIncompleteBar) {
            final IDrawIncompleteBar cObj = (IDrawIncompleteBar) obj;
            return cObj.isDrawIncompleteBar();
        } else {
            return false;
        }
    }

    static boolean isDrawIncompleteBar(final Dataset obj) {
        if (obj instanceof IDrawIncompleteBar) {
            final IDrawIncompleteBar cObj = (IDrawIncompleteBar) obj;
            return cObj.isDrawIncompleteBar();
        } else {
            return false;
        }
    }

}
