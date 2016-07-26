// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fitnesse.slim.converters.VoidConverter;
import fitnesse.testsystems.slim.SlimCommandRunningClient;
import org.junit.Before;
import org.junit.Test;

// Extracted Test class to be implemented by all Java based Slim ports
// The tests for PhpSlim and JsSlim implement this class

public abstract class ListExecutorTestBase {
  protected List<Object> statements;
  protected ListExecutor executor;
  protected List<Object> expectedResults = new ArrayList<>();
  protected String testClass = "TestSlim";

  protected abstract ListExecutor getListExecutor() throws Exception;
  protected abstract String getTestClassPath();

  @Before
  public void setup() throws Exception {
    executor = getListExecutor();
    statements = new ArrayList<>();
    statements.add(Arrays.asList("i1", "import", getTestClassPath()));
    statements.add(Arrays.asList("m1", "make", "testSlim", testClass));
    expectedResults.add(Arrays.asList("i1", "OK"));
    expectedResults.add(Arrays.asList("m1", "OK"));
  }

  protected void respondsWith(List<?> expected) {
    expectedResults.addAll(expected);
    List<Object> result = executor.execute(statements);
    Map<String, Object> expectedMap = SlimCommandRunningClient.resultToMap(expectedResults);
    Map<String, Object> resultMap = SlimCommandRunningClient.resultToMap(result);
    assertEquals(expectedMap, resultMap);
  }

  @Test
  public void checkSetup()
  {
    respondsWith(new ArrayList<>());
  }

  @Test()
  public void invalidOperation() throws Exception {
    statements.add(Arrays.asList("inv1", "invalidOperation"));
    assertExceptionReturned(String.format("message:<<%s invalidOperation>>", SlimServer.MALFORMED_INSTRUCTION),"inv1");
  }

  @Test(expected = SlimError.class)
  public void malformedStatement() throws Exception {
    statements.add(Arrays.asList("id", "call", "notEnoughArguments"));
    assertExceptionReturned("XX", "id");
  }

  private void assertExceptionReturned(String message, String returnTag) {
    Map<String, Object> results = SlimCommandRunningClient.resultToMap(executor.execute(statements));
    SlimException result = (SlimException) results.get(returnTag);
    assertTrue(result.getMessage(), result.toString().contains(SlimServer.EXCEPTION_TAG) && result.toString().contains(message));
  }

  @Test
  public void noSuchInstance() throws Exception {
    statements.add(Arrays.asList("id", "call", "noSuchInstance", "noSuchMethod"));
    assertExceptionReturned("message:<<NO_INSTANCE noSuchInstance.noSuchMethod.>>", "id");
  }

  @Test
  public void emptyListReturnsNicely() throws Exception {
    statements.clear();
    executor.execute(statements);
    expectedResults.clear();
    respondsWith(new ArrayList<>());
  }

  @Test
  public void createWithFullyQualifiedNameWorks() throws Exception {
    statements.clear();
    statements.add(Arrays.asList("m1", "make", "testSlim", getTestClassPath() + "." + testClass));
    expectedResults.clear();
    respondsWith(Arrays.asList(Arrays.asList("m1", (Object) "OK")));
  }

  @Test
  public void exceptionInConstructorIsPassedThrough() throws Exception {
    statements.clear();
    expectedResults.clear();
    statements.add(Arrays.asList("m1", "make", "x", getTestClassPath() + ".ConstructorThrows", "thrown message"));
    assertExceptionReturned("thrown message", "m1");
  }

  @Test
  public void oneFunctionCall() throws Exception {
    statements.add(Arrays.asList("id", "call", "testSlim", "returnString"));
    respondsWith(Arrays.asList(Arrays.asList("id", (Object) "string")));
  }

  @Test
  public void oneFunctionCallVerbose() throws Exception {
    final String endl = System.getProperty("line.separator");
    executor.setVerbose();
    PrintStream oldOut = System.out;
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    System.setOut(new PrintStream(os));

    statements.add(Arrays.asList("id", "call", "testSlim", "returnString"));
    executor.execute(statements);

    System.setOut(oldOut);
    assertEquals("!1 Instructions" + endl +
      "[i1, import, fitnesse.slim.test]\n" +
      endl +
      "[i1, OK]" + endl +
      "------" + endl +
      "[m1, make, testSlim, TestSlim]\n" +
      endl +
      "[m1, OK]" + endl +
      "------" + endl +
      "[id, call, testSlim, returnString]\n" +
      endl +
      "[id, string]" + endl +
      "------" + endl, os.toString());
  }


  @Test
  public void oneFunctionCallWithBlankArgument() throws Exception {
    statements.add(Arrays.asList("id", "call", "testSlim", "echoString", ""));
    respondsWith(Arrays.asList(Arrays.asList("id", "")));
  }

  @Test
  public void oneFunctionCallToShowThatLaterImportsTakePrecedence() throws Exception {
    statements.add(0, Arrays.asList("i2", "import", getTestClassPath() + ".testSlimInThisPackageShouldNotBeTheOneUsed"));
    statements.add(Arrays.asList("id", "call", "testSlim", "returnString"));
    expectedResults.add(0, Arrays.asList("i2", "OK"));
    respondsWith(Arrays.asList(Arrays.asList("id", "string")));
  }

  @Test
  public void canPassArgumentsToConstructor() throws Exception {
    statements.add(Arrays.asList("m2", "make", "testSlim2", testClass, "3"));
    statements.add(Arrays.asList("c1", "call", "testSlim2", "returnConstructorArg"));
    statements.add(Arrays.asList("c2", "call", "testSlim", "returnConstructorArg"));
    respondsWith(
            Arrays.asList(Arrays.asList("m2", (Object) "OK"), Arrays.asList("c1", (Object) "3"), Arrays.asList("c2", (Object) "0"))
    );
  }


  @Test
  public void multiFunctionCall() throws Exception {
    statements.add(Arrays.asList("id1", "call", "testSlim", "addTo", "1", "2"));
    statements.add(Arrays.asList("id2", "call", "testSlim", "addTo", "3", "4"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "3"), Arrays.asList("id2", (Object) "7")));
  }

  @Test
  public void callAndAssign() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v", "testSlim", "addTo", "5", "6"));
    statements.add(Arrays.asList("id2", "call", "testSlim", "echoInt", "$v"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "11"), Arrays.asList("id2", (Object) "11")));
  }

  @Test
  public void canReplaceMultipleSymbolsInAnArgument() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v1", "testSlim", "echoString", "Bob"));
    statements.add(Arrays.asList("id2", "callAndAssign", "v2", "testSlim", "echoString", "Martin"));
    statements.add(Arrays.asList("id3", "call", "testSlim", "echoString", "name: $v1 $v2"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "Bob"), Arrays.asList("id2", (Object) "Martin"), Arrays.asList("id3", (Object) "name: Bob Martin")));
  }

  @Test
  public void canReplaceMultipleSymbolsInAnArgumentWhenOneVarIsPrefixOfAnother() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v", "testSlim", "echoString", "Bob"));
    statements.add(Arrays.asList("id2", "callAndAssign", "v1", "testSlim", "echoString", "Martin"));
    statements.add(Arrays.asList("id3", "call", "testSlim", "echoString", "name: $v $v1"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "Bob"), Arrays.asList("id2", (Object) "Martin"), Arrays.asList("id3", (Object) "name: Bob Martin")));
  }

  @Test
  public void canReplaceSymbolWhenValueIsNull() throws Exception {
    statements.add(Arrays.asList("id1", "make", "nf", "NullFixture"));
    statements.add(Arrays.asList("id2", "callAndAssign", "v", "nf", "getNull"));
    statements.add(Arrays.asList("id3", "call", "testSlim", "echoString", "$v"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "OK"), Arrays.asList("id2", (Object) null), Arrays.asList("id3", (Object) null)));
  }

  @Test
  public void passAndReturnList() throws Exception {
    List<String> l = Arrays.asList("one", "two");
    statements.add(Arrays.asList("id", "call", "testSlim", "echoList", l));
    respondsWith(Arrays.asList(Arrays.asList("id", l)));
  }

  @Test
  public void passAndReturnListWithVariable() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v", "testSlim", "addTo", "3", "4"));
    statements.add(Arrays.asList("id2", "call", "testSlim", "echoList", Arrays.asList("$v")));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "7"), Arrays.asList("id2", (Object) Arrays.asList(7))));
  }

  @Test
  public void callToVoidFunctionReturnsVoidValue() throws Exception {
    statements.add(Arrays.asList("id", "call", "testSlim", "voidFunction"));
    respondsWith(Arrays.asList(Arrays.asList("id", (Object) VoidConverter.VOID_TAG)));
  }

  @Test
  public void callToFunctionReturningNull() throws Exception {
    statements.add(Arrays.asList("id", "call", "testSlim", "nullString"));
    respondsWith(Arrays.asList(Arrays.asList("id", (Object) null)));
  }

  @Test
  public void fixtureChainingWithAssignmentFromFactory() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v", "testSlim", "createTestSlimWithString", "test string"));
    statements.add(Arrays.asList("m2", "make", "chainedTestSlim", "$v"));
    statements.add(Arrays.asList("id2", "call", "chainedTestSlim", "getStringArg"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "TestSlim: 0, test string"), Arrays.asList("m2", (Object) "OK"), Arrays.asList("id2", (Object) "test string")));
  }

  @Test
  public void methodAcceptsTestSlimFromSymbol() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v", "testSlim", "createTestSlimWithString", "test string"));
    statements.add(Arrays.asList("id2", "call", "testSlim", "getStringFromOther", "$v"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "TestSlim: 0, test string"), Arrays.asList("id2", (Object) "test string")));
  }

  @Test
  public void methodAcceptsObjectFromSymbol() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v", "testSlim", "createTestSlimWithString", "test string"));
    statements.add(Arrays.asList("id2", "call", "testSlim", "isSame", "$v"));
    statements.add(Arrays.asList("m2", "make", "chainedTestSlim", "$v"));
    statements.add(Arrays.asList("id3", "call", "chainedTestSlim", "isSame", "$v"));

    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "TestSlim: 0, test string"), Arrays.asList("id2", (Object) "false"), Arrays.asList("m2", (Object) "OK"), Arrays.asList("id3", (Object) "true")));
  }

  @Test
  public void constructorAcceptsTestSlimFromSymbol() throws Exception {
    statements.add(Arrays.asList("id1", "callAndAssign", "v", "testSlim", "createTestSlimWithString", "test string"));
    statements.add(Arrays.asList("m2", "make", "newTestSlim", testClass, "4", "$v"));
    statements.add(Arrays.asList("id2", "call", "newTestSlim", "getStringArg"));
    respondsWith(Arrays.asList(Arrays.asList("id1", (Object) "TestSlim: 0, test string"), Arrays.asList("m2", (Object) "OK"), Arrays.asList("id2", (Object) "test string")));
  }

}
