package de.invesdwin.context.client.swing;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({ TestViewTest.class })
@Immutable
public class RichclientTestSuite {

}
