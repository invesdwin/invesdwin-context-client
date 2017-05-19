package de.invesdwin.common.client.swing.api.internal.action;

import java.awt.Component;
import java.lang.reflect.Method;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JToggleButton;

import org.fest.reflect.beanproperty.Invoker;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.beansbinding.BeanProperty;

import de.invesdwin.common.client.swing.api.AModel;
import de.invesdwin.context.log.Log;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public class ActionBinding {

    private final Log log = new Log(this);

    private final AModel model;
    private final Component component;

    private String targetActionName;
    private String targetMethodName;
    private Method targetMethod;
    private String targetPath;
    private Object target;
    private ActionMap actionMap;
    private ResourceMap resourceMap;

    public ActionBinding(final AModel model, final Component component) {
        this.model = model;
        this.component = component;
    }

    public void initBinding() {
        initialize();
        if (target == null) {
            //it was already warned about this
            return;
        }
        intAction();
    }

    private void initialize() {
        if (component.getName().contains(".")) {
            targetActionName = component.getName().substring(component.getName().lastIndexOf('.') + 1);
            targetPath = Strings.removeEnd(component.getName(), "." + targetActionName);
            target = extractTargetFromModelPath();
            if (target != null) {
                actionMap = Application.getInstance().getContext().getActionMap(target);
                resourceMap = Application.getInstance().getContext().getResourceMap(target.getClass());
            }
        } else {
            targetActionName = component.getName();
            targetPath = null;
            target = model;
            actionMap = model.getActionMap();
            resourceMap = model.getResourceMap();
        }
        if (target != null) {
            targetMethodName = Strings.stripNonAlphanumeric(targetActionName);
            targetMethod = Reflections.findMethod(target.getClass(), targetMethodName);
        }
    }

    private Object extractTargetFromModelPath() {
        final BeanProperty<AModel, Object> targetProperty = BeanProperty.create(targetPath);
        if (targetProperty.isReadable(model)) {
            final Object target = targetProperty.getValue(model);
            if (target != null) {
                return target;
            } else {
                log.warn(
                        "Getter [%s] returned null at [%s], thus no Action binding can happen for the path [%s] on the component [%s]. Please initialize the property properly.",
                        targetProperty, model, component.getName(), component);
            }
        }
        return null;
    }

    private void intAction() {
        final Method setActionMethod = Reflections.findMethod(component.getClass(), "setAction", Action.class);
        if (setActionMethod != null) {
            Action action = getActionFromActionMap();
            if (action == null && targetMethod != null) {
                //Create a new Action and register it, because the annotation is missing
                action = new GeneratedApplicationAction(actionMap, resourceMap, targetActionName, targetMethod);
                actionMap.put(targetActionName, action);
            }

            if (action == null && component instanceof AbstractButton && !(component instanceof JMenu)
                    && !(component instanceof JToggleButton)) {
                //We should expect that actions should be available for buttons
                log.warn(
                        "Action [%s] does not exist in [%s], thus Action binding cannot occur for the path [%s] on the component [%s]. Please create the method [%s] or correct the path.",
                        targetActionName, target.getClass(), component.getName(), component, targetMethodName);
            } else if (action != null) {
                final Runnable textSetter = initActionText(action);
                initActionIcon(action);
                Reflections.invokeMethod(setActionMethod, component, action);
                if (textSetter != null) {
                    textSetter.run();
                }
            }
        }
    }

    private Action getActionFromActionMap() {
        ActionMap parentActionMap = actionMap;
        while (parentActionMap != null) {
            final Action action = actionMap.get(targetActionName);
            if (action != null) {
                return action;
            } else {
                parentActionMap = parentActionMap.getParent();
            }
        }
        return null;
    }

    /**
     * Ignores the Action name in the component if it matches the same method name.
     */
    private Runnable initActionText(final Action action) {
        if (action.getValue(Action.NAME).equals(targetActionName)) {
            final Method setHideActionTextMethod = Reflections.findMethod(component.getClass(), "setHideActionText",
                    boolean.class);
            if (setHideActionTextMethod != null) {
                final Invoker<String> textProperty = Reflections.property("text").ofType(String.class).in(component);
                final String textCopy = textProperty.get();
                Reflections.invokeMethod(setHideActionTextMethod, component, true);
                return new Runnable() {
                    @Override
                    public void run() {
                        textProperty.set(textCopy);
                    }
                };
            }
        }
        return null;
    }

    /**
     * Sets the icon in the Action if it does not already exist on it.
     */
    private void initActionIcon(final Action action) {
        final Method getIconMethod = Reflections.findMethod(component.getClass(), "getIcon");
        if (getIconMethod != null) {
            final Icon icon = (Icon) Reflections.invokeMethod(getIconMethod, component);
            if (action.getValue(Action.SMALL_ICON) == null) {
                action.putValue(Action.SMALL_ICON, icon);
            }
            if (action.getValue(Action.LARGE_ICON_KEY) == null) {
                action.putValue(Action.LARGE_ICON_KEY, icon);
            }
        }
    }

}
