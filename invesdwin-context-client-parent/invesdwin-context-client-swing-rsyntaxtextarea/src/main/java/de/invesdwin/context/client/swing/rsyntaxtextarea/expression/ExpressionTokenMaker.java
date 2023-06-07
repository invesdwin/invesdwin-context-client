package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

import de.invesdwin.context.client.swing.rsyntaxtextarea.expression.internal.AExpressionTokenMaker;

/**
 * https://github.com/bobbylight/RSyntaxTextArea/wiki/Adding-Syntax-Highlighting-for-a-new-Language#JFlexBased
 * 
 * Once you run this file through JFlex, you'll have to make manual changes to it. Once I stop being lazy, there will be
 * a JFlex skeleton file that handles these changes for you, so the generated .java file will work out-of-the-box, but
 * for now you have to do the following things manually: There are two zzRefill() and yyreset() methods with the same
 * signatures in the generated file. You need to delete the second of each definition (the ones generated by the lexer).
 * Change the declaration/definition of zzBuffer to NOT be initialized. This is a needless memory allocation for us
 * since we will be pointing the array somewhere else anyway.
 * 
 * @author subes
 *
 */
@NotThreadSafe
public class ExpressionTokenMaker extends AExpressionTokenMaker {

    static {
        final AbstractTokenMakerFactory tokenMakerFactory = (AbstractTokenMakerFactory) TokenMakerFactory
                .getDefaultInstance();
        tokenMakerFactory.putMapping(getSyntaxStyle(), ExpressionTokenMaker.class.getName());
    }

    public static String getSyntaxStyle() {
        return "text/expression";
    }

}
