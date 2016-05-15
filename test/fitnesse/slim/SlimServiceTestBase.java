// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.slim.instructions.CallInstruction;
import fitnesse.slim.instructions.ImportInstruction;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.instructions.MakeInstruction;
import fitnesse.socketservice.PlainClientSocketFactory;
import fitnesse.socketservice.PlainServerSocketFactory;
import fitnesse.socketservice.ServerSocketFactory;
import fitnesse.socketservice.SslServerSocketFactory;
import fitnesse.testsystems.CompositeExecutionLogListener;
import fitnesse.testsystems.MockCommandRunner;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

// Extracted Test class to be implemented by all Java based Slim ports
// The tests for PhpSlim and JsSlim implement this class

public abstract class SlimServiceTestBase {
  static Thread service;
  protected List<Instruction> statements;
  protected SlimCommandRunningClient slimClient;

  public static synchronized void startWithFactoryAsync(final SlimFactory slimFactory, final SlimService.Options options) throws IOException {
    if (service != null && service.isAlive()) {
      service.interrupt();
      throw new SlimError("Already an in-process server running: " + service.getName() + " (alive=" + service.isAlive() + ")");
    }
    ServerSocketFactory serverSocketFactory = options.useSSL ? new SslServerSocketFactory(true, options.sslParameterClassName) : new PlainServerSocketFactory();
    final SlimService slimservice = new SlimService(slimFactory.getSlimServer(), serverSocketFactory.createServerSocket(options.port), options.daemon);
    service = new Thread() {
      @Override
      public void run() {
        try {
          slimservice.accept();
        } catch (IOException e) {
          throw new SlimError(e);
        }
      }
    };
    service.start();
  }

  public static void waitForServiceToStopAsync() throws InterruptedException {
    // wait for service to close.
    for (int i = 0; i < 1000; i++) {
      if (!service.isAlive())
        break;
      Thread.sleep(50);
    }
  }

  protected abstract void startSlimService() throws Exception;

  protected abstract void closeSlimService() throws Exception;

  protected abstract String getImport();

  protected abstract String expectedExceptionMessage();

  protected abstract String expectedStopTestExceptionMessage();

  @Before
  public void setUp() throws InterruptedException, IOException {
    createSlimService();
    slimClient = new SlimCommandRunningClient(new MockCommandRunner(new CompositeExecutionLogListener()), "localhost", 8099, 10, SlimCommandRunningClient.MINIMUM_REQUIRED_SLIM_VERSION, new PlainClientSocketFactory());
    statements = new ArrayList<>();
    slimClient.connect();
  }

  protected void createSlimService() throws InterruptedException {
    while (!tryCreateSlimService())
      Thread.sleep(10);
  }

  private boolean tryCreateSlimService() {
    try {
      startSlimService();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @After
  public void after() throws Exception {
    teardown();
  }

  protected void teardown() throws Exception {
    slimClient.bye();
    slimClient.kill();
    closeSlimService();
  }

  @Test
  public void emptySession() throws Exception {
    assertTrue("Connected", slimClient.isConnected());
  }

  @Test
  public void versionNumberShouldBeDetected() throws Exception {
    double slimVersion = Double.parseDouble(SlimVersion.VERSION);
    assertEquals(slimVersion, slimClient.getServerVersion(), .0001);
  }

  @Test
  public void callOneMethod() throws Exception {
    addImportAndMake();
    addEchoInt("id", "1");
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("1", result.get("id"));
  }

  private void addEchoInt(String id, String number) {
    statements.add(new CallInstruction(id, "testSlim", "echoInt", new Object[] { number }));
  }

  private void addImportAndMake() {
    statements.add(new ImportInstruction("i1", getImport()));
    statements.add(new MakeInstruction("m1", "testSlim", "TestSlim"));
  }

  @Test
  public void makeManyCallsInOrderToTestLongSequencesOfInstructions() throws Exception {
    addImportAndMake();
    for (int i = 0; i < 1000; i++)
      addEchoInt(String.format("id_%d", i), Integer.toString(i));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    for (int i = 0; i < 1000; i++)
      assertEquals(i, Integer.parseInt((String) result.get(String.format("id_%d", i))));
  }

  @Test
  public void callWithLineBreakInStringArgument() throws Exception {
    addImportAndMake();
    statements.add(new CallInstruction("id", "testSlim", "echoString", new Object[] { "hello\nworld\n" }));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("hello\nworld\n", result.get("id"));
  }

  @Test
  public void callWithMultiByteChar() throws Exception {
    addImportAndMake();
    statements.add(new CallInstruction("id", "testSlim", "echoString", new Object[] { "K\u00f6ln" }));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("K\u00f6ln", result.get("id"));
  }

  @Test
  public void makeManyIndividualCalls() throws Exception {
    addImportAndMake();
    slimClient.invokeAndGetResponse(statements);
    for (int i = 0; i < 100; i++) {
      statements.clear();
      addEchoInt("id", "42");
      Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
      assertEquals(1, result.size());
      assertEquals("42", result.get("id"));
    }
  }

  @Test
  public void callFunctionThatDoesntExist() throws Exception {
    addImportAndMake();
    statements.add(new CallInstruction("id", "testSlim", "noSuchFunction"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<NO_METHOD_IN_CLASS", "id", results);
  }

  @Test
  public void callFunctionThatReturnsHugeResult() throws Exception {
    addImportAndMake();
    statements.add(new CallInstruction("id", "testSlim", "returnHugeString"));
    slimClient.invokeAndGetResponse(statements);
    // should not crash
  }

  @Test
  public void callFunctionWithHugeParameter() throws Exception {
    addImportAndMake();
    String hugeString = StringUtils.repeat("x", 999999 + 10);
    statements.add(new CallInstruction("id", "testSlim", "echoString", new Object[] {hugeString}));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals(hugeString, result.get("id"));
  }

  private void assertContainsException(String message, String id, Map<String, Object> results) {
    String result = (String) results.get(id);
    assertTrue(result, result.contains(SlimServer.EXCEPTION_TAG)
        && result.contains(message));
  }

  @Test
  public void makeClassThatDoesntExist() throws Exception {
    statements.add(new MakeInstruction("m1", "me", "NoSuchClass"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR", "m1", results);
  }

  @Test
  public void useInstanceThatDoesntExist() throws Exception {
    addImportAndMake();
    statements.add(new CallInstruction("id", "noInstance", "f"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<NO_INSTANCE", "id", results);
  }

  @Test
  public void verboseArgument() throws Exception {
    String args[] = {"-v", "99"};
    SlimService.Options options = SlimService.parseCommandLine(args);
    assertNotNull(options);
    assertTrue(options.verbose);
  }

  @Test
  public void notStopTestExceptionThrown() throws Exception {
    addImportAndMake();
    statements.add(new CallInstruction("id", "testSlim", "throwNormal"));
    statements.add(new CallInstruction("id2", "testSlim", "throwNormal"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("__EXCEPTION__:" + expectedExceptionMessage(), "id", results);
    assertContainsException("__EXCEPTION__:" + expectedExceptionMessage(), "id2", results);
  }

  @Test
  public void stopTestExceptionThrown() throws Exception {
    addImportAndMake();
    statements.add(new CallInstruction("id", "testSlim", "throwStopping"));
    statements.add(new CallInstruction("id2", "testSlim", "throwNormal"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("__EXCEPTION__:" + expectedStopTestExceptionMessage(), "id", results);
    assertNull(results.get("id2"));
  }

  @Test
  public void canSpecifyAnInteractionClass() {
    final SlimService.Options options = SlimService.parseCommandLine(new String[]{"-i", "fitnesse.slim.fixtureInteraction.DefaultInteraction"});
    assertNotNull("should parse correctly", options);
    assertEquals("fitnesse.slim.fixtureInteraction.DefaultInteraction", options.interaction.getClass().getName());
  }

  @Test
  public void canSpecifyAComplexCommandLine() {
    String commandLine = "-v -i fitnesse.slim.fixtureInteraction.DefaultInteraction 7890";
    String[] args = commandLine.split(" ");

    SlimService.Options options = SlimService.parseCommandLine(args);
    assertNotNull("should parse correctly", options);
    assertEquals("should have interaction class set", "fitnesse.slim.fixtureInteraction.DefaultInteraction", options.interaction.getClass().getName());
    assertTrue("should be verbose", options.verbose);
    assertEquals("should have set port", 7890, options.port);
  }

  @Test
  public void canSpecifyComplexArgs() {
    String commandLine = "-v -i fitnesse.slim.fixtureInteraction.DefaultInteraction 7890";
    String[] args = commandLine.split(" ");

    SlimService.Options options = SlimService.parseCommandLine(args);
    assertNotNull("should parse correctly", options);
    assertEquals("should have interaction class set", "fitnesse.slim.fixtureInteraction.DefaultInteraction", options.interaction.getClass().getName());
    assertTrue("should be verbose", options.verbose);
    assertEquals("should have set port", 7890, options.port);
  }

}
