package de.invesdwin.context.client.swing.frame.app;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import org.jdesktop.application.TaskService;

import de.invesdwin.util.concurrent.Executors;

@ThreadSafe
@Named
public final class DefaultTaskService extends TaskService {

    public DefaultTaskService() {
        super(TaskService.DEFAULT_NAME,
                Executors.newCachedThreadPool(DefaultTaskService.class.getSimpleName()).withDynamicThreadName(false));
    }

}
