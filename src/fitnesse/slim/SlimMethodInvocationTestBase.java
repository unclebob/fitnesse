// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.*;
import static util.ListUtility.list;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.DateConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.test.TestSlimInterface;

// Extracted Test class to be implemented by all Java based Slim ports
// The tests for PhpSlim and JsSlim implement this class

abstract public class SlimMethodInvocationTestBase {
  protected StatementExecutorInterface caller;
  protected TestSlimInterface testSlim;
  protected String testClass = "TestSlim";

  @Before
  public abstract void setUp() throws Exception;

  protected abstract String getTestClassName();

  @Test
  public void callNiladicFunction() throws Exception {
    caller.call("testSlim", "nilad");
    assertTrue(testSlim.niladWasCalled());
  }

  @Test
  public void throwMethodNotCalledErrorIfNoSuchMethod() throws Exception {
    String response = (String) caller.call("testSlim", "noSuchMethod");
    assertTrue(response,
      response.indexOf(SlimServer.EXCEPTION_TAG) != -1 &&
        response.indexOf("message:<<NO_METHOD_IN_CLASS noSuchMethod[0] " + getTestClassName() + ".>>") != -1);
  }

  @Test
  public void methodReturnsString() throws Exception {
    Object retval = caller.call("testSlim", "returnString");
    assertEquals("string", retval);
  }

  @Test
  public void methodReturnsInt() throws Exception {
    Object retval = caller.call("testSlim", "returnInt");
    assertEquals("7", retval);
  }

  @Test
  public void methodReturnsVoid() throws Exception {
    Object retval = caller.call("testSlim", "nilad");
    assertEquals(VoidConverter.VOID_TAG, retval);
  }

  @Test
  public void methodTakesAndReturnsBooleanTrue() throws Exception {
    Object retval = caller.call("testSlim", "echoBoolean", "true");
    assertEquals(BooleanConverter.TRUE, retval);
  }

  @Test
  public void methodTakesAndReturnsBooleanFalse() throws Exception {
    Object retval = caller.call("testSlim", "echoBoolean", "false");
    assertEquals(BooleanConverter.FALSE, retval);
  }


  @Test
  public void passOneString() throws Exception {
    caller.call("testSlim", "oneString", "string");
    assertEquals("string", testSlim.getStringArg());
  }

  @Test
  public void passOneInt() throws Exception {
    caller.call("testSlim", "oneInt", "42");
    assertEquals(42, testSlim.getIntArg());
  }

  @Test
  public void passOneDouble() throws Exception {
    caller.call("testSlim", "oneDouble", "3.14159");
    assertEquals(3.14159, testSlim.getDoubleArg(), .000001);
  }

  @Test
  public void passOneDate() throws Exception {
    caller.call("testSlim", "oneDate", "5-May-2009");
    assertEquals((Date) new DateConverter().fromString("5-May-2009"), testSlim.getDateArg());
  }

  @Test
  public void passOneList() throws Exception {
    caller.call("testSlim", "oneList", list("one", "two"));
    assertEquals(list("one", "two"), testSlim.getListArg());
  }

  @Test
  public void passManyArgs() throws Exception {
    caller.call("testSlim", "manyArgs", "1", "2.1", "c");
    assertEquals(1, testSlim.getIntegerObjectArg().intValue());
    assertEquals(2.1, testSlim.getDoubleObjectArg(), .00001);
    assertEquals('c', testSlim.getCharArg());
  }

  @Test
  public void convertLists() throws Exception {
    caller.call("testSlim", "oneList", "[1 ,2, 3,4, hello Bob]");
    assertEquals(list("1", "2", "3", "4", "hello Bob"), caller.call("testSlim", "getListArg"));
  }

  @Test
  public void convertArraysOfStrings() throws Exception {
    caller.call("testSlim", "setStringArray", "[1 ,2, 3,4, hello Bob]");
    assertEquals("[1, 2, 3, 4, hello Bob]", caller.call("testSlim", "getStringArray"));
  }

  @Test
  public void convertArraysOfIntegers() throws Exception {
    caller.call("testSlim", "setIntegerArray", "[1 ,2, 3,4]");
    assertEquals("[1, 2, 3, 4]", caller.call("testSlim", "getIntegerArray"));
  }

  @Test
  public void convertArrayOfIntegersThrowsExceptionIfNotInteger() throws Exception {
    Object result = caller.call("testSlim", "setIntegerArray", "[1 ,2, 3,4, hello]");
    String resultString = (String) result;
    assertTrue(resultString, resultString.indexOf("message:<<CANT_CONVERT_TO_INTEGER_LIST>>") != -1);
  }

  @Test
  public void convertArraysOfBooleans() throws Exception {
    caller.call("testSlim", "setBooleanArray", "[true ,false, false,true]");
    assertEquals("[true, false, false, true]", caller.call("testSlim", "getBooleanArray"));
  }

  @Test
  public void convertArraysOfDoubles() throws Exception {
    caller.call("testSlim", "setDoubleArray", "[1 ,2.2, -3e2,0.04]");
    assertEquals("[1.0, 2.2, -300.0, 0.04]", caller.call("testSlim", "getDoubleArray"));
  }

  @Test
  public void convertArrayOfDoublesThrowsExceptionIfNotInteger() throws Exception {
    Object result = caller.call("testSlim", "setDoubleArray", "[1 ,2, 3,4, hello]");
    String resultString = (String) result;
    assertTrue(resultString, resultString.indexOf("message:<<CANT_CONVERT_TO_DOUBLE_LIST>>") != -1);
  }

  @Test
  public void handleReturnNull() throws Exception {
    Object result = caller.call("testSlim", "nullString");
    Assert.assertNull(result);
  }

  @Test
  public void handleEchoNull() throws Exception {
    Object result = caller.call("testSlim", "echoString", new Object[]{null});
    Assert.assertNull(result);
  }

  @Test
  public void handleNullSymbols() throws Exception {
    caller.setVariable("x", null);
    Object result = caller.call("testSlim", "echoString", new Object[]{"$x"});
    Assert.assertNull(result);
  }

  @Test
  public void handleNullSymbolsSurroundedByString() throws Exception {
    caller.setVariable("x", null);
    Object result = caller.call("testSlim", "echoString", new Object[]{"A $x B"});
    assertEquals("A null B", result);
  }

  @Test
  public void handleUnspecifiedSymbols() throws Exception {
    Object result = caller.call("testSlim", "echoString", new Object[]{"$x"});
    assertEquals("$x", result);
  }
}
