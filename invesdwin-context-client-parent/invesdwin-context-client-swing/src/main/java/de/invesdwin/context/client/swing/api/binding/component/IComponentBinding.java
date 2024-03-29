package de.invesdwin.context.client.swing.api.binding.component;

public interface IComponentBinding {

    IComponentBinding[] EMPTY_ARRAY = new IComponentBinding[0];

    String getBeanPath();

    /**
     * Moves the UI component value to the model, though keeping a record of the previous model value.
     */
    void submit();

    void setInvalidMessage(String invalidMessage);

    String getInvalidMessage();

    /**
     * Validates the current model value.
     */
    String validate();

    /**
     * Synchronized from model to component and discards previous validations. Can be used to apply side effects of
     * forced buttons.
     */
    void reset();

    /**
     * Removes the record of the previous model value, thus making it impossible to roll back anymore. Making the
     * submitted value now permanent.
     */
    void commit();

    /**
     * Reverts the previous model value from before the submit.
     */
    void rollback();

    /**
     * Moves the model value to the UI component.
     */
    void update();

}
