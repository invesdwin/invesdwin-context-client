package de.invesdwin.client.wicket.examples;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({ KerberosLoginTest.class })
@Immutable
public class ClientWicketExamplesTestSuite {

}
