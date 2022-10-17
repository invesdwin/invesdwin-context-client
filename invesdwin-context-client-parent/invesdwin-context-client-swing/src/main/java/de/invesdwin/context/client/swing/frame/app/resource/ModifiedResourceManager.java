package de.invesdwin.context.client.swing.frame.app.resource;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.jdesktop.application.ResourceMap;

@NotThreadSafe
public class ModifiedResourceManager extends ResourceManager {

    public ModifiedResourceManager(final ApplicationContext context) {
        super(context);
    }

    @Override
    public ResourceMap createResourceMap(final ClassLoader classLoader, final ResourceMap parent,
            final List<String> bundleNames) {
        return new ModifiedResourceMap(parent, classLoader, bundleNames);
    }

}
