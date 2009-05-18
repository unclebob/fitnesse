// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SlimServiceTest {
  private List<Object> statements;
  private SlimClient slimClient = new SlimClient("localhost", 8099);

  @Before
  public void setUp() throws Exception {
    createSlimService();
    slimClient = new SlimClient("localhost", 8099);
    statements = new ArrayList<Object>();
    slimClient.connect();
  }

  protected void createSlimService() throws Exception {
    while (!tryCreateSlimService())
      Thread.sleep(10);
  }

  private boolean tryCreateSlimService() throws Exception {
    try {
      SlimService.main(new String[]{"8099"});
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
    slimClient.sendBye();
    slimClient.close();
  }

  @Test
  public void emptySession() throws Exception {
    assertTrue("Connected", slimClient.isConnected());
  }

  @Test
  public void callOneMethod() throws Exception {
    addImportAndMake();
    addEchoInt("id", "1");
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("1", result.get("id"));
  }

  private void addEchoInt(String id, String number) {
    statements.add(list(id, "call", "testSlim", "echoInt", number));
  }

  private void addImportAndMake() {
    statements.add(list("i1", "import", getImport()));
    statements.add(list("m1", "make", "testSlim", "TestSlim"));
  }

  protected String getImport() {
    return "fitnesse.slim.test";
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
    statements.add(list("id", "call", "testSlim", "echoString", "hello\nworld\n"));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("hello\nworld\n", result.get("id"));
  }

  @Test
  public void callWithMultiByteChar() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "testSlim", "echoString", "K\u00f6ln"));
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
    statements.add(list("id", "call", "testSlim", "noSuchFunction"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<NO_METHOD_IN_CLASS", "id", results);
  }

  private void assertContainsException(String message, String id, Map<String, Object> results) {
    String result = (String) results.get(id);
    assertTrue(result, result.indexOf(SlimServer.EXCEPTION_TAG) != -1 && result.indexOf(message) != -1);
  }

  @Test
  public void makeClassThatDoesntExist() throws Exception {
    statements.add(list("m1", "make", "me", "NoSuchClass"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR", "m1", results);
  }

  @Test
  public void useInstanceThatDoesntExist() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "noInstance", "f"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<NO_INSTANCE", "id", results);
  }

  @Test
  public void verboseArgument() throws Exception {
    String args[] = {"-v", "99"};
    assertTrue(SlimService.parseCommandLine(args));
    assertTrue(SlimService.verbose);
  }

  @Test
  public void notStopTestExceptionThrown() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "testSlim", "throwNormal"));
    statements.add(list("id2", "call", "testSlim", "throwNormal"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("__EXCEPTION__:java.lang.Exception: This is my exception", "id", results);
    assertContainsException("__EXCEPTION__:java.lang.Exception: This is my exception", "id2", results);
  }

  @Test
  public void stopTestExceptionThrown() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "testSlim", "throwStopping"));
    statements.add(list("id2", "call", "testSlim", "throwNormal"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("__EXCEPTION__:ABORT_SLIM_TEST:fitnesse.slim.test.TestSlim$StopTestException: This is a stop test exception", "id", results);
    assertNull(results.get("id2"));
  }
}

