package de.invesdwin.context.client.swing.internal.app;

import javax.annotation.concurrent.ThreadSafe;

import org.jdesktop.application.TaskService;

import de.invesdwin.util.concurrent.Executors;

@ThreadSafe
public class DefaultTaskService extends TaskService {

    public DefaultTaskService() {
        super(TaskService.DEFAULT_NAME, Executors.newCachedThreadPool(DefaultTaskService.class.getSimpleName()));
    }

}
