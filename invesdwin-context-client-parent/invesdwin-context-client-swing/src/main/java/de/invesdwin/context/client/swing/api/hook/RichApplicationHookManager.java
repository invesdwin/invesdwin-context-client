package de.invesdwin.context.client.swing.api.hook;

import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.invesdwin.context.beans.hook.ReinitializationHookManager;
import de.invesdwin.context.beans.hook.ReinitializationHookSupport;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import jakarta.inject.Named;

/**
 * Registers hooks for the application start. The hooks are called only once after the merged ApplicationContext has
 * been initialized.
 * 
 * These hooks are started in their own seperate threads to speedup the startup process and allow multiple background
 * processes while the application runs.
 * 
 * @author subes
 * 
 */
@ThreadSafe
@Named
public final class RichApplicationHookManager
        implements ApplicationContextAware, FactoryBean<RichApplicationHookManager> {

    public static final RichApplicationHookManager INSTANCE = new RichApplicationHookManager();
    @GuardedBy("INSTANCE")
    private static final Set<IRichApplicationHook> REGISTERED_HOOKS = ILockCollectionFactory.getInstance(false)
            .newLinkedSet();

    static {
        ReinitializationHookManager.register(new ReinitializationHookSupport() {
            @Override
            public void reinitializationStarted() {
                REGISTERED_HOOKS.clear();
            }
        });
    }

    private RichApplicationHookManager() {}

    public static void register(final IRichApplicationHook hook) {
        synchronized (INSTANCE) {
            REGISTERED_HOOKS.add(hook);
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        synchronized (INSTANCE) {
            for (final IRichApplicationHook hook : applicationContext.getBeansOfType(IRichApplicationHook.class)
                    .values()) {
                register(hook);
            }
        }
    }

    @Override
    public RichApplicationHookManager getObject() throws Exception {
        return INSTANCE;
    }

    @Override
    public Class<?> getObjectType() {
        return INSTANCE.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void triggerInitializeDone() {
        synchronized (INSTANCE) {
            for (final IRichApplicationHook hook : REGISTERED_HOOKS) {
                hook.initializeDone();
            }
        }
    }

    public void triggerStartupDone() {
        synchronized (INSTANCE) {
            for (final IRichApplicationHook hook : REGISTERED_HOOKS) {
                hook.startupDone();
            }
        }
    }

    public void triggerShowMainFrameDone() {
        synchronized (INSTANCE) {
            for (final IRichApplicationHook hook : REGISTERED_HOOKS) {
                hook.showMainFrameDone();
            }
        }
    }

    public void triggerHideMainFrameDone() {
        synchronized (INSTANCE) {
            for (final IRichApplicationHook hook : REGISTERED_HOOKS) {
                hook.hideMainFrameDone();
            }
        }
    }

    public void triggerShutdownDone() {
        synchronized (INSTANCE) {
            for (final IRichApplicationHook hook : REGISTERED_HOOKS) {
                hook.shutdownDone();
            }
        }
    }

}