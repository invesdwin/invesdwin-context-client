package de.invesdwin.context.client.swing.api.guiservice.dialog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.invesdwin.norva.beanpath.annotation.ModalCloser;

/**
 * This can be used to make button methods close a modal dialog (if they are part of the modal).
 * 
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ModalCloser
public @interface DefaultCloseOperation {

}
