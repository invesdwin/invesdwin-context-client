package de.invesdwin.context.client.swing.frame.app.resource;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ImageIcon;

import org.jdesktop.application.ResourceMap;

import de.invesdwin.util.collections.Arrays;
import de.invesdwin.util.swing.HiDPI;

@NotThreadSafe
public class ModifiedResourceMap extends ResourceMap {

    public ModifiedResourceMap(final ResourceMap parent, final ClassLoader classLoader,
            final List<String> bundleNames) {
        super(parent, classLoader, bundleNames);
    }

    public ModifiedResourceMap(final ResourceMap parent, final ClassLoader classLoader, final String... bundleNames) {
        this(parent, classLoader, Arrays.asList(bundleNames));
    }

    @Override
    public Object getObject(final String key, final Class type) {
        final Object value = super.getObject(key, type);
        if (value instanceof ImageIcon) {
            final ImageIcon icon = (ImageIcon) value;
            return HiDPI.scale(icon);
        } else {
            return value;
        }
    }

}
