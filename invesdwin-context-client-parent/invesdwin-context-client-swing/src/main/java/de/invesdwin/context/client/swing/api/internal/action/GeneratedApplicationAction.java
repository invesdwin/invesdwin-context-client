package de.invesdwin.context.client.swing.api.internal.action;

import java.lang.reflect.Method;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.ActionMap;

import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskService;

@SuppressWarnings("serial")
@NotThreadSafe
public class GeneratedApplicationAction extends ApplicationAction {

    public GeneratedApplicationAction(final ActionMap actionMap, final ResourceMap resourceMap,
            final String targetActionName, final Method targetMethod) {
        super((ApplicationActionMap) actionMap, resourceMap, targetActionName, targetMethod, null, false, null,
                TaskService.DEFAULT_NAME, BlockingScope.NONE);
    }

}
