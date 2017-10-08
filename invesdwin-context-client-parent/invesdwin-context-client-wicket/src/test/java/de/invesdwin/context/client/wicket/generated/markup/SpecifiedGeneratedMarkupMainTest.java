package de.invesdwin.context.client.wicket.generated.markup;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.nowicket.generated.markup.SpecifiedGeneratedMarkupMain;

@NotThreadSafe
public class SpecifiedGeneratedMarkupMainTest extends ATest {

    @Test
    public void testCommandline() {
        final List<String> params = new ArrayList<String>();
        for (final Class<?> model : SpecifiedGeneratedMarkupTest.MODELS) {
            params.add(model.getName());
        }
        params.add("-d");
        params.add(SpecifiedGeneratedMarkupTest.DESTINATION.getPath());
        SpecifiedGeneratedMarkupMain.main(params.toArray(new String[0]));
    }
}
