package de.invesdwin.context.client.swing;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ TestModelViewTest.class })
@Immutable
public class ClientSwingTestSuite {

}
