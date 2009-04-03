// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.slim.converters.VoidConverter;

public class ListExecutorTest {
  private List<Object> statements;
  private ListExecutor executor;
  private List<Object> expectedResults = new ArrayList<Object>();

  @Before
  public void setup() {
    executor = new ListExecutor();
    statements = new ArrayList<Object>();
    statements.add(list("i1", "import", "fitnesse.slim.test"));
    statements.add(list("m1", "make", "testSlim", "TestSlim"));
    expectedResults.add(list("i1", "OK"));
    expectedResults.add(list("m1", "OK"));
  }

  private void respondsWith(List<Object> expected) {
    expectedResults.addAll(expected);
    List<Object> result = executor.execute(statements);
    Map<String, Object> expectedMap = SlimClient.resultToMap(expectedResults);
    Map<String, Object> resultMap = SlimClient.resultToMap(result);
    assertEquals(expectedMap, resultMap);
  }

  @Test()
  public void invalidOperation() throws Exception {
    statements.add(list("inv1", "invalidOperation"));
    assertExceptionReturned("message:<<INVALID_STATEMENT: invalidOperation.>>", "inv1");
  }

  @Test(expected = SlimError.class)
  public void malformedStatement() throws Exception {
    statements.add(list("id", "call", "notEnoughArguments"));
    assertExceptionReturned("XX", "id");
  }

  private void assertExceptionReturned(String message, String returnTag) {
    Map<String, Object> results = SlimClient.resultToMap(executor.execute(statements));
    String result = (String) results.get(returnTag);
    assertTrue(result, result.indexOf(SlimServer.EXCEPTION_TAG) != -1 && result.indexOf(message) != -1);
  }

  @Test
  public void noSuchInstance() throws Exception {
    statements.add(list("id", "call", "noSuchInstance", "noSuchMethod"));
    assertExceptionReturned("message:<<NO_INSTANCE noSuchInstance.>>", "id");
  }

  @Test
  public void emptyListReturnsNicely() throws Exception {
    statements.clear();
    executor.execute(statements);
    expectedResults.clear();
    respondsWith(list());
  }

  @Test
  public void createWithFullyQualifiedNameWorks() throws Exception {
    statements.clear();
    statements.add(list("m1", "make", "testSlim", "fitnesse.slim.test.TestSlim"));
    expectedResults.clear();
    respondsWith(list(list("m1", "OK")));
  }

  @Test
  public void exceptionInConstructorIsPassedThrough() throws Exception {
    statements.clear();
    expectedResults.clear();
    statements.add(list("m1", "make", "x", "fitnesse.slim.test.ConstructorThrows", "thrown message"));
    assertExceptionReturned("thrown message", "m1");
  }

  @Test
  public void oneFunctionCall() throws Exception {
    statements.add(list("id", "call", "testSlim", "returnString"));
    respondsWith(list(list("id", "string")));
  }

  @Test
  public void oneFunctionCallWithBlankArgument() throws Exception {
    statements.add(list("id", "call", "testSlim", "echoString", ""));
    respondsWith(list(list("id", "")));
  }

  @Test
  public void oneFunctionCallToShowThatLaterImportsTakePrecedence() throws Exception {
    statements.add(0,list("i2", "import", "fitnesse.slim.test.testSlimInThisPackageShouldNotBeTheOneUsed"));
    statements.add(list("id", "call", "testSlim", "returnString"));
    expectedResults.add(0, list("i2", "OK"));
    respondsWith(list(list("id", "string")));
  }

  @Test
  public void canPassArgumentsToConstructor() throws Exception {
    statements.add(list("m2", "make", "testSlim2", "TestSlim", "3"));
    statements.add(list("c1", "call", "testSlim2", "returnConstructorArg"));
    statements.add(list("c2", "call", "testSlim", "returnConstructorArg"));
    respondsWith(
      list(
        list("m2", "OK"),
        list("c1", "3"),
        list("c2", "0")
      )
    );
  }


  @Test
  public void multiFunctionCall() throws Exception {
    statements.add(list("id1", "call", "testSlim", "addTo", "1", "2"));
    statements.add(list("id2", "call", "testSlim", "addTo", "3", "4"));
    respondsWith(list(list("id1", "3"), list("id2", "7")));
  }

  @Test
  public void callAndAssign() throws Exception {
    statements.add(list("id1", "callAndAssign", "v", "testSlim", "addTo", "5", "6"));
    statements.add(list("id2", "call", "testSlim", "echoInt", "$v"));
    respondsWith(list(list("id1", "11"), list("id2", "11")));
  }

  @Test
  public void canReplaceMultipleVariablesInAnArgument() throws Exception {
    statements.add(list("id1", "callAndAssign", "v1", "testSlim", "echoString", "Bob"));
    statements.add(list("id2", "callAndAssign", "v2", "testSlim", "echoString", "Martin"));
    statements.add(list("id3", "call", "testSlim", "echoString", "name: $v1 $v2"));
    respondsWith(list(list("id1", "Bob"), list("id2", "Martin"), list("id3", "name: Bob Martin")));
  }

  @Test
  public void canReplaceSymbolWhenValueIsNull() throws Exception {
    statements.add(list("id1", "make", "nf", "NullFixture"));
    statements.add(list("id2", "callAndAssign", "v", "nf", "getNull"));
    statements.add(list("id3", "call", "testSlim", "echoString", "$v"));
    respondsWith(list(list("id1", "OK"), list("id2", null), list("id3", "null")));
  }

  @Test
  public void passAndReturnList() throws Exception {
    List<String> l = list("one", "two");
    statements.add(list("id", "call", "testSlim", "echoList", l));
    respondsWith(list(list("id", l)));
  }

  @Test
  public void passAndReturnListWithVariable() throws Exception {
    statements.add(list("id1", "callAndAssign", "v", "testSlim", "addTo", "3", "4"));
    statements.add(list("id2", "call", "testSlim", "echoList", list("$v")));
    respondsWith(list(list("id1", "7"), list("id2", list("7"))));
  }

  @Test
  public void callToVoidFunctionReturnsVoidValue() throws Exception {
    statements.add(list("id", "call", "testSlim", "voidFunction"));
    respondsWith(list(list("id", VoidConverter.VOID_TAG)));
  }

  @Test
  public void callToFunctionReturningNull() throws Exception {
    statements.add(list("id", "call", "testSlim", "nullString"));
    respondsWith(list(list("id", null)));
  }
}
