package de.invesdwin.context.client.swing.api.binding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JButton;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import de.invesdwin.context.beans.validator.BeanValidator;
import de.invesdwin.context.client.swing.api.annotation.DefaultCloseOperation;
import de.invesdwin.context.client.swing.api.binding.component.AComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.IComponentBinding;
import de.invesdwin.context.client.swing.api.binding.component.RootTitleBinding;
import de.invesdwin.context.client.swing.api.binding.component.button.ISubmitButtonExceptionHandler;
import de.invesdwin.context.client.swing.api.binding.component.button.SubmitButtonBinding;
import de.invesdwin.context.client.swing.api.guiservice.GuiService;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.context.client.swing.api.view.AView;
import de.invesdwin.norva.beanpath.impl.object.BeanObjectContext;
import de.invesdwin.norva.beanpath.spi.element.AActionBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.IBeanPathElement;
import de.invesdwin.norva.beanpath.spi.element.RootBeanPathElement;
import de.invesdwin.util.collections.fast.AFastIterableDelegateList;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Strings;

@NotThreadSafe
public class BindingGroup implements IComponentBinding {

    private final ALoadingCache<String, List<IComponentBinding>> beanPath_binding = new ALoadingCache<String, List<IComponentBinding>>() {
        @Override
        protected List<IComponentBinding> loadValue(final String key) {
            return new ArrayList<>();
        }
    };
    private final AFastIterableDelegateList<IComponentBinding> bindings = new AFastIterableDelegateList<IComponentBinding>() {
        @Override
        protected List<IComponentBinding> newDelegate() {
            return new ArrayList<>();
        }
    };

    private final AView<?, ?> view;
    private final BeanObjectContext modelContext;
    private final ISubmitButtonExceptionHandler submitButtonExceptionHandler;
    private String invalidMessage = null;
    private SubmitButtonBinding defaultCloseOperation;
    private RootTitleBinding rootTitleBinding;
    private volatile boolean submitButtonRunning;

    public BindingGroup(final AView<?, ?> view, final BeanObjectContext modelContext,
            final ISubmitButtonExceptionHandler submitButtonExceptionHandler) {
        this.view = view;
        this.modelContext = modelContext;
        this.submitButtonExceptionHandler = submitButtonExceptionHandler;
    }

    public AView<?, ?> getView() {
        return view;
    }

    public AModel getModel() {
        return view.getModel();
    }

    public BeanObjectContext getModelContext() {
        return modelContext;
    }

    public List<IComponentBinding> getBindings(final String beanPath) {
        return beanPath_binding.get(beanPath);
    }

    public void addBinding(final IComponentBinding binding) {
        beanPath_binding.get(binding.getBeanPath()).add(binding);
        bindings.add(binding);
        if (binding instanceof RootTitleBinding) {
            rootTitleBinding = (RootTitleBinding) binding;
        }
        if (binding instanceof SubmitButtonBinding) {
            final SubmitButtonBinding cBinding = (SubmitButtonBinding) binding;
            if (cBinding.isDefaultCloseOperation()) {
                if (defaultCloseOperation != null) {
                    throw new IllegalStateException("Only one @" + DefaultCloseOperation.class.getSimpleName()
                            + " supported binding group. Existing=" + defaultCloseOperation.getBeanPath() + " New="
                            + cBinding.getBeanPath());
                } else {
                    defaultCloseOperation = cBinding;
                }
            }
        }
    }

    public void finishBinding() {
        if (defaultCloseOperation == null) {
            final Collection<IBeanPathElement> elements = modelContext.getElementRegistry().getElements();
            for (final IBeanPathElement element : elements) {
                if (element instanceof AActionBeanPathElement) {
                    final AActionBeanPathElement action = (AActionBeanPathElement) element;
                    if (SubmitButtonBinding.isDefaultCloseOperation(action)) {
                        defaultCloseOperation = new SubmitButtonBinding(new JButton(), action, this);
                    }
                }
            }
        }
    }

    @Override
    public void submit() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].submit();
        }
    }

    @Override
    public String validate() {
        final Set<String> invalidMessages = new LinkedHashSet<>();
        final ConstraintViolationException exception = BeanValidator.getInstance().validate(getModel());
        if (exception != null) {
            for (final ConstraintViolation<?> violation : exception.getConstraintViolations()) {
                final String beanPath = violation.getPropertyPath().toString();
                final String invalidMessage = violation.getMessage();
                if (Strings.isNotBlank(invalidMessage)) {
                    if (beanPath_binding.containsKey(beanPath)) {
                        final List<IComponentBinding> bindings = beanPath_binding.get(beanPath);
                        for (int i = 0; i < bindings.size(); i++) {
                            bindings.get(i).setInvalidMessage(invalidMessage);
                        }
                    } else {
                        invalidMessages.add(AComponentBinding.surroundTitle(beanPath) + " " + invalidMessage);
                    }
                }
            }
        }
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            final String invalidMessage = array[i].validate();
            if (Strings.isNotBlank(invalidMessage)) {
                invalidMessages.add(invalidMessage);
            }
        }
        String combinedInvalidMessage = null;
        for (final String invalidMessage : invalidMessages) {
            if (combinedInvalidMessage == null) {
                combinedInvalidMessage = invalidMessage;
            } else {
                combinedInvalidMessage += "\n" + invalidMessage;
            }
        }
        this.invalidMessage = combinedInvalidMessage;
        return combinedInvalidMessage;
    }

    @Override
    public String getInvalidMessage() {
        return invalidMessage;
    }

    @Override
    public void setInvalidMessage(final String invalidMessage) {
        this.invalidMessage = invalidMessage;
    }

    @Override
    public void commit() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].commit();
        }
    }

    @Override
    public void rollback() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].rollback();
        }
    }

    @Override
    public void update() {
        final IComponentBinding[] array = bindings.asArray(IComponentBinding.class);
        for (int i = 0; i < array.length; i++) {
            array[i].update();
        }
    }

    public ISubmitButtonExceptionHandler getSubmitButtonExceptionHandler() {
        return submitButtonExceptionHandler;
    }

    @Override
    public String getBeanPath() {
        return RootBeanPathElement.ROOT_BEAN_PATH;
    }

    public String getTitle(final IBeanPathElement element, final Object target) {
        final String title = element.getTitle(target);
        if (title == null) {
            return null;
        }
        if (hasTitleUtilityElement(element) && !isVisibleName(element, title)) {
            //title() or getXYZTitle() method has priority
            final String str = i18n(title);
            return str;
        } else {
            //properties have priority over static title or title annotation
            final String str = i18n(element.getBeanPath(), title);
            return str;
        }
    }

    protected boolean hasTitleUtilityElement(final IBeanPathElement element) {
        return element.getTitleElement() != null || element.getContainerTitleElement() != null;
    }

    protected boolean isVisibleName(final IBeanPathElement element, final String title) {
        return Objects.equals(element.getVisibleName(), title);
    }

    public String i18n(final String value) {
        return i18n(value, value);
    }

    public String i18n(final String value, final String defaultValue) {
        return GuiService.i18n(view, value, defaultValue);
    }

    public SubmitButtonBinding getDefaultCloseOperation() {
        return defaultCloseOperation;
    }

    public RootTitleBinding getRootTitleBinding() {
        return rootTitleBinding;
    }

    public boolean registerSubmitButtonRunning() {
        final boolean submitButtonRunningBefore = submitButtonRunning;
        submitButtonRunning = true;
        return submitButtonRunningBefore;
    }

    public void unregisterSubmitButtonRunning(final boolean registerSubmitButtonRunning) {
        submitButtonRunning = registerSubmitButtonRunning;
    }

    public boolean isSubmitButtonRunning() {
        return submitButtonRunning;
    }

}
