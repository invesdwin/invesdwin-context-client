package de.invesdwin.context.client.swing.api.guiservice.dialog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.invesdwin.norva.beanpath.annotation.ModalCloser;

/**
 * This marks actions that are to be called when the window close button was clicked. Only one such annotated action
 * should exist. Normally the default action should something equivalent to a "cancel".
 * 
 * This works both for Modals and for Dockables.
 * 
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ModalCloser
public @interface DefaultCloseOperation {

}
