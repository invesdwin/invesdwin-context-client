package de.invesdwin.context.client.swing.api.binding.component;

public interface IComponentBinding {

    /**
     * Moves the UI component value to the model, though keeping a record of the previous model value.
     */
    void submit();

    /**
     * Validates the current model value.
     */
    boolean validate();

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
