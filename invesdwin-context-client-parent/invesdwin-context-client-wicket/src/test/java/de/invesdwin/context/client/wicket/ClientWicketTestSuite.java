package de.invesdwin.context.client.wicket;

import javax.annotation.concurrent.Immutable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.invesdwin.context.client.wicket.generated.markup.AnnotatedGeneratedMarkupTest;
import de.invesdwin.context.client.wicket.generated.markup.SpecifiedGeneratedMarkupMainTest;
import de.invesdwin.context.client.wicket.generated.markup.SpecifiedGeneratedMarkupTest;
import de.invesdwin.context.client.wicket.generated.markup.internal.run.AutomatedAnotherSampleModelTest;
import de.invesdwin.context.client.wicket.internal.SimpleHomePageTest;
import de.invesdwin.context.client.wicket.internal.SimpleHomePageWebServerTest;

@RunWith(Suite.class)
@SuiteClasses({ SimpleHomePageWebServerTest.class, SimpleHomePageTest.class, SpecifiedGeneratedMarkupTest.class,
        SpecifiedGeneratedMarkupMainTest.class, AnnotatedGeneratedMarkupTest.class,
        AutomatedAnotherSampleModelTest.class })
@Immutable
public class ClientWicketTestSuite {

}
