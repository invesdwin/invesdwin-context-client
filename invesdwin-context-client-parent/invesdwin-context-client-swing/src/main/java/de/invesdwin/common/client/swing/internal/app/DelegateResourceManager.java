package de.invesdwin.common.client.swing.internal.app;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.jdesktop.application.ResourceMap;

import de.invesdwin.util.lang.Reflections;

/**
 * So that application properties of the interface implementation can be loaded, an evil hack is needed that is
 * implemented here. :)
 * 
 */
@ThreadSafe
public class DelegateResourceManager extends ResourceManager {

    @GuardedBy("this")
    private boolean initialized;

    public DelegateResourceManager(final ApplicationContext context) {
        super(context);
        setResourceFolder(null);
    }

    @Override
    public ResourceMap getResourceMap() {
        synchronized (this) {
            if (!initialized) {
                //Bundles enhancement
                final List<String> applicationBundleNames = new ArrayList<String>(
                        getContext().getResourceManager().getApplicationBundleNames());

                //DelegateRichApplication properties must be loaded in any case
                if (!applicationBundleNames.contains(DelegateRichApplication.class.getName())) {
                    applicationBundleNames.add(0, DelegateRichApplication.class.getName());
                }

                //Use the interface implementation properties first in chain
                applicationBundleNames.add(0, DelegateRichApplication.getDelegateClass().getName());
                getContext().getResourceManager().setApplicationBundleNames(applicationBundleNames);

                initialized = true;
            }
        }
        return super.getResourceMap();
    }

    public static void inject(final ApplicationContext context) {
        Reflections.field("resourceManager")
                .ofType(ResourceManager.class)
                .in(context)
                .set(new DelegateResourceManager(context));
    }

}
