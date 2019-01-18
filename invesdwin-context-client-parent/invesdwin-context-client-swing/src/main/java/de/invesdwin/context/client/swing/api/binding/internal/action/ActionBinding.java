package de.invesdwin.context.client.swing.api.binding.internal.action;

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

import de.invesdwin.context.client.swing.api.AModel;
import de.invesdwin.context.client.swing.api.binding.BindingGroup;
import de.invesdwin.context.log.Log;
import de.invesdwin.norva.beanpath.spi.element.IPropertyBeanPathElement;
import de.invesdwin.util.lang.Reflections;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public class ActionBinding {

    private final Log log = new Log(this);

    private final AModel model;
    private final Component component;
    private final BindingGroup bindingGroup;

    private String targetActionName;
    private String targetMethodName;
    private Method targetMethod;
    private String targetPath;
    private Class<?> targetType;
    private ActionMap actionMap;
    private ResourceMap resourceMap;

    public ActionBinding(final AModel model, final Component component, final BindingGroup bindingGroup) {
        this.model = model;
        this.component = component;
        this.bindingGroup = bindingGroup;
    }

    public void initBinding() {
        initialize();
        if (targetType == null) {
            //it was already warned about this
            return;
        }
        intAction();
    }

    private void initialize() {
        if (component.getName().contains(".")) {
            targetActionName = component.getName().substring(component.getName().lastIndexOf('.') + 1);
            targetPath = Strings.removeEnd(component.getName(), "." + targetActionName);
            final IPropertyBeanPathElement targetElement = bindingGroup.getContext()
                    .getElementRegistry()
                    .getElement(targetPath);
            targetType = targetElement.getModifier().getBeanClassAccessor().getType().getType();
            actionMap = Application.getInstance().getContext().getActionMap(targetType);
            resourceMap = Application.getInstance().getContext().getResourceMap(targetType.getClass());
        } else {
            targetActionName = component.getName();
            targetPath = null;
            targetType = model.getClass();
            actionMap = model.getActionMap();
            resourceMap = model.getResourceMap();
        }
        targetMethodName = Strings.stripNonAlphanumeric(targetActionName);
        targetMethod = Reflections.findMethod(targetType.getClass(), targetMethodName);
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
                        targetActionName, targetType.getClass(), component.getName(), component, targetMethodName);
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
