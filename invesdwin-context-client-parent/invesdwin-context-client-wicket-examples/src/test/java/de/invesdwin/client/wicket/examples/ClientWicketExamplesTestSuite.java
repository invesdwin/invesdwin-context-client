package de.invesdwin.client.wicket.examples;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ KerberosLoginTest.class })
@Immutable
public class ClientWicketExamplesTestSuite {

}
