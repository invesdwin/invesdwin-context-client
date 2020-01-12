package de.invesdwin.context.client.javafx.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes the annotated method to run inside the FxApplicationThread.
 * 
 * <p>
 * <b>ATTENTION:</b> If the method signature specifies a different return type than void, Void or Future, then the calls
 * to that method with InvokeLater are not asynchronous, because the aspect waits for the futures! If you need
 * asynchronicity with a return value, you should use Future instead or directly use FxApplicationThreadUtil!
 * </p>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FxApplicationThread {
    InvocationType value();

    enum InvocationType {
        RUN_AND_WAIT,
        RUN_LATER,
        RUN_LATER_IF_NOT_IN_FAT;
    }
}
