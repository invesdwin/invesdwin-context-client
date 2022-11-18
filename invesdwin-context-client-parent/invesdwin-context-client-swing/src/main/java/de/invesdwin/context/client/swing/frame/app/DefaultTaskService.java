package de.invesdwin.context.client.swing.frame.app;

import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.TaskService;

import de.invesdwin.util.concurrent.Executors;
import jakarta.inject.Named;

@ThreadSafe
@Named
public final class DefaultTaskService extends TaskService {

    public DefaultTaskService() {
        super(TaskService.DEFAULT_NAME,
                Executors.newCachedThreadPool(DefaultTaskService.class.getSimpleName()).setDynamicThreadName(false));
    }

}
