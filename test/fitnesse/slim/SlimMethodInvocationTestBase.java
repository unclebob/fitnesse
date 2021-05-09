// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.DateConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.test.TestSlimInterface;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// Extracted Test class to be implemented by all Java based Slim ports
// The tests for PhpSlim and JsSlim implement this class

public abstract class SlimMethodInvocationTestBase {
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
  public void throwMethodNotCalledErrorIfNoSuchMethodAndListAvailableMethodsSorted() throws Exception {
    try {
      caller.create("ShouldIBuyMilk", "fitnesse.slim.test.ShouldIBuyMilk", new Object[0]);
      caller.call("ShouldIBuyMilk", "noSuchMethod");
      fail("Called non-existing method.");
    } catch (SlimException e) {
      assertTrue(e.getMessage(),e.toString().contains(SlimServer.EXCEPTION_TAG) &&
          e.toString().contains("message:<<NO_METHOD_IN_CLASS No Method noSuchMethod[0] in class "+ "fitnesse.slim.test.ShouldIBuyMilk" + ".\n"+
                                " Available methods:\n"+
                                "equals(java.lang.Object) -> boolean\n"+
                                "execute() -> void\n"+
                                "getClass() -> class java.lang.Class\n"+
                                "goToStore() -> class java.lang.String\n"+
                                "hashCode() -> int\n"+
                                "notify() -> void\n"+
                                "notifyAll() -> void\n"+
                                "setCashInWallet(int) -> void\n"+
                                "setCreditCard(java.lang.String) -> void\n"+
                                "setPintsOfMilkRemaining(int) -> void\n"+
                                "table(java.util.List) -> void\n"+
                                "toString() -> class java.lang.String\n"+
                                "wait() -> void\n"+
                                "wait(long) -> void\n"+
                                "wait(long, int) -> void>>"));
    }
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
    Date expected = new DateConverter().fromString("5-May-2009");
    assertEquals(expected, testSlim.getDateArg());
  }

  @Test
  public void passOneList() throws Exception {
    caller.call("testSlim", "oneList", Arrays.asList("one", "two"));
    assertEquals(Arrays.asList("one", "two"), testSlim.getListArg());
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
    assertEquals(Arrays.asList("1", "2", "3", "4", "hello Bob"), caller.call("testSlim", "getListArg"));
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
    try {
      caller.call("testSlim", "setIntegerArray", "[1 ,2, 3,4, hello]");
      fail("Converted array with non-integers to an integer array.");
    } catch (SlimException e) {
      System.out.println(e.getMessage());
      assertEquals("fitnesse.slim.SlimError: message:<<Can't convert hello to integer.\n"
        + "Tried to invoke: fitnesse.slim.test.TestSlim.setIntegerArray(java.lang.Integer[]) -> void. On instance of: "+ getTestClassName() + ">>", e.getMessage());
      assertTrue(NumberFormatException.class.isInstance(e.getCause().getCause().getCause()));
    }
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
    try {
      caller.call("testSlim", "setDoubleArray", "[1 ,2, 3,4, hello]");
      fail("Converted array with non-doubles to a double array.");
    } catch (SlimException e) {
      System.out.println(e.getMessage());
      assertEquals("fitnesse.slim.SlimError: message:<<Can't convert hello to double.\n"
        + "Tried to invoke: fitnesse.slim.test.TestSlim.setDoubleArray(java.lang.Double[]) -> void. On instance of: "+ getTestClassName() + ">>", e.getMessage());
      assertTrue(NumberFormatException.class.isInstance(e.getCause().getCause().getCause()));
    }
  }

  @Test
  public void handleReturnNull() throws Exception {
    Object result = caller.call("testSlim", "nullString");
    assertNull(result);
  }

  @Test
  public void handleEchoNull() throws Exception {
    Object result = caller.call("testSlim", "echoString", new Object[]{null});
    assertNull(result);
  }

  @Test
  public void handleNullSymbols() throws Exception {
    caller.assign("x", null);
    Object result = caller.call("testSlim", "echoString", new Object[]{"$x"});
    assertNull(result);

    caller.assign("xyz", null);
    result = caller.call("testSlim", "echoString", new Object[]{"$xyz"});
    assertNull(result);
  }

  @Test
  public void handleNullSymbolsSurroundedByString() throws Exception {
    caller.assign("x", null);
    Object result = caller.call("testSlim", "echoString", new Object[]{"A $x B"});
    assertEquals("A null B", result);

    caller.assign("xyz", null);
    result = caller.call("testSlim", "echoString", new Object[]{"A $xyz B"});
    assertEquals("A null B", result);
  }

  @Test
  public void handlesSymbolInStrings() throws Exception {
    caller.assign("x", "a");
    Object result = caller.call("testSlim", "echoString", new Object[]{"$x 1"});
    assertEquals("a 1", result);

    result = caller.call("testSlim", "echoString", new Object[]{"$x1"});
    assertEquals("a1", result);

    caller.assign("x", "abc");
    result = caller.call("testSlim", "echoString", new Object[]{"$x1"});
    assertEquals("abc1", result);

    caller.assign("x", "a");
    caller.assign("y", "b");
    result = caller.call("testSlim", "echoString", new Object[]{"1$x1$y2"});
    assertEquals("1a1b2", result);

    caller.assign("xyz", "ccded");
    result = caller.call("testSlim", "echoString", new Object[]{"1$x1$y$xyzpostfix"});
    assertEquals("1a1bccdedpostfix", result);
  }

  @Test
  public void handleUnspecifiedSymbols() throws Exception {
    Object result = caller.call("testSlim", "echoString", new Object[]{"$x"});
    assertEquals("$x", result);

    result = caller.call("testSlim", "echoString", new Object[]{"$xyz"});
    assertEquals("$xyz", result);
  }

  @Test
  public void handleUnspecifiedSymbolsInString() throws Exception {
    Object result = caller.call("testSlim", "echoString", new Object[]{"A $x B"});
    assertEquals("A $x B", result);

    result = caller.call("testSlim", "echoString", new Object[]{"A $xyz B"});
    assertEquals("A $xyz B", result);

    result = caller.call("testSlim", "echoString", new Object[]{"A$xyzB"});
    assertEquals("A$xyzB", result);
  }
}
