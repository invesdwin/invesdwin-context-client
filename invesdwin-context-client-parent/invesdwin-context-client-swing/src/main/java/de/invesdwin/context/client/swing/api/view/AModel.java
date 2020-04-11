package de.invesdwin.context.client.swing.api.view;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.ActionMap;

import org.jdesktop.application.ResourceMap;

import de.invesdwin.context.client.swing.frame.app.DelegateRichApplication;
import de.invesdwin.context.log.Log;
import de.invesdwin.norva.beanpath.annotation.Hidden;
import de.invesdwin.util.bean.AValueObject;

@ThreadSafe
public abstract class AModel extends AValueObject {

    public static final String IGNORE = "IGNORE";
    public static final String TEXT_RESOURCE_SUFFIX = ".text";

    protected final Log log = new Log(this);

    private final ResourceMap resourceMap;
    private final Object actionMapLock = new Object();
    @GuardedBy("actionMapLock")
    private ActionMap actionMap;

    public AModel() {
        resourceMap = DelegateRichApplication.getInstance().getContext().getResourceMap(this.getClass());
        resourceMap.injectFields(this);
    }

    @Hidden(skip = true)
    public ResourceMap getResourceMap() {
        return resourceMap;
    }

    /**
     * ActionMaps must be initialized lazy because else the ProxyActions don't get configured properly on startup!
     */
    @Hidden(skip = true)
    public ActionMap getActionMap() {
        synchronized (actionMapLock) {
            if (actionMap == null) {
                actionMap = DelegateRichApplication.getInstance().getContext().getActionMap(this);
            }
            return actionMap;
        }
    }

}
