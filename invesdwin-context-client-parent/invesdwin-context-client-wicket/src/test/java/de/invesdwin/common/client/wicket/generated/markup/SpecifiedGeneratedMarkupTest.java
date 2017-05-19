package de.invesdwin.common.client.wicket.generated.markup;

import java.io.File;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Test;

import de.invesdwin.common.client.wicket.generated.markup.internal.RedirectedModel;
import de.invesdwin.context.test.ATest;
import de.invesdwin.nowicket.generated.markup.SpecifiedGeneratedMarkup;

@NotThreadSafe
public class SpecifiedGeneratedMarkupTest extends ATest {

    public static final File DESTINATION = new File("src/test/java");
    public static final Class<?>[] MODELS = { /*
                                               * SampleModel.class, AnotherSampleModel.class,
                                               */RedirectedModel.class /*
                                                                       * , Bugfix.class, BugfixRemoveFrom.class
                                                                       */ };

    @Test
    public void test() {
        new SpecifiedGeneratedMarkup(DESTINATION, MODELS).generate();
    }
}
