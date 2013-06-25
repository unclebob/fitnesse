// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.components.ClassPathBuilder;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.net.ServerSocket;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

public class SlimTestSystemTest {
  private WikiPage root;
  private TestSystemListener dummyListener = new DummyListener();

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    // Enforce the test runner here, to make sure we're talking to the right system
    SlimClientBuilder.clearSlimPortOffset();
  }

  private String getClassPath(WikiPage page) {
    return new ClassPathBuilder().getClasspath(page);
  }

  @Test
  public void portRotates() throws Exception {
    for (int i = 1; i < 15; i++) {
      SlimClientBuilder descriptor = new SlimClientBuilder(root.getData(), getClassPath(root));
      assertEquals(8085 + (i % 10), descriptor.getSlimPort());
    }
  }

  @Test
  public void portStartsAtSlimPortVariable() throws Exception {
    WikiPage pageWithSlimPortDefined = WikiPageUtil.addPage(root, PathParser.parse("PageWithSlimPortDefined"), "!define SLIM_PORT {9000}\n");
    for (int i = 1; i < 15; i++) {
      SlimClientBuilder descriptor = new SlimClientBuilder(pageWithSlimPortDefined.getData(), getClassPath(pageWithSlimPortDefined));
      assertEquals(9000 + (i % 10), descriptor.getSlimPort());
    }
  }

  @Test
  public void badSlimPortVariableDefaults() throws Exception {
    WikiPage pageWithBadSlimPortDefined = WikiPageUtil.addPage(root, PathParser.parse("PageWithBadSlimPortDefined"), "!define SLIM_PORT {BOB}\n");
    for (int i = 1; i < 15; i++)
      assertEquals(8085 + (i % 10), new SlimClientBuilder(pageWithBadSlimPortDefined.getData(), getClassPath(pageWithBadSlimPortDefined)).getSlimPort());
  }

  @Test
  public void slimHostDefaultsTolocalhost() throws Exception {
    WikiPage pageWithoutSlimHostVariable = WikiPageUtil.addPage(root, PathParser.parse("PageWithoutSlimHostVariable"), "some gunk\n");
    assertEquals("localhost", new SlimClientBuilder(pageWithoutSlimHostVariable.getData(), getClassPath(pageWithoutSlimHostVariable)).determineSlimHost());
  }

  @Test
  public void slimHostVariableSetsTheHost() throws Exception {
    WikiPage pageWithSlimHostVariable = WikiPageUtil.addPage(root, PathParser.parse("PageWithSlimHostVariable"), "!define SLIM_HOST {somehost}\n");
    assertEquals("somehost", new SlimClientBuilder(pageWithSlimHostVariable.getData(), getClassPath(pageWithSlimHostVariable)).determineSlimHost());
  }

  @Test
  public void translateExceptionMessage() throws Exception {
    assertTranslatedException("Could not find constructor for SomeClass", "NO_CONSTRUCTOR SomeClass");
    assertTranslatedException("Could not invoke constructor for SomeClass", "COULD_NOT_INVOKE_CONSTRUCTOR SomeClass");
    assertTranslatedException("No converter for SomeClass", "NO_CONVERTER_FOR_ARGUMENT_NUMBER SomeClass");
    assertTranslatedException("Method someMethod not found in SomeClass", "NO_METHOD_IN_CLASS someMethod SomeClass");
    assertTranslatedException("The instance someInstance does not exist", "NO_INSTANCE someInstance");
    assertTranslatedException("Could not find class SomeClass", "NO_CLASS SomeClass");
    assertTranslatedException("The instruction [a, b, c] is malformed", "MALFORMED_INSTRUCTION [a, b, c]");
  }

  private void assertTranslatedException(String expected, String message) {
    assertEquals(expected, SlimTestSystem.translateExceptionMessage(message));
  }


  @Test(expected = SocketException.class)
  public void createSlimServiceFailsFastWhenSlimPortIsNotAvailable() throws Exception {
    final int slimServerPort = 10258;
    ServerSocket slimSocket = new ServerSocket(slimServerPort);
    try {
      SlimClientBuilder sys = new SlimClientBuilder(root.getData(), getClassPath(root));
      String slimArguments = String.format("%s %d", "", slimServerPort);
      sys.createSlimService(slimArguments);
    } finally {
      slimSocket.close();
    }
  }

  static class DummyListener implements TestSystemListener {
    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
    }

    public void testOutputChunk(String output) {
    }

    public void testComplete(TestSummary testSummary) {
    }

    public void exceptionOccurred(Throwable e) {
    }

    @Override
    public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    }

    @Override
    public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    }
  }
}
