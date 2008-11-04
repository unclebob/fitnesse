package fitnesse.slim;

import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.test.TestSlim;
import static fitnesse.util.ListUtility.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;


public class SlimMethodInvocationTest {
  private StatementExecutor caller;
  private TestSlim testSlim;

  @Before
  public void setUp() {
    caller = new StatementExecutor();
    caller.create("testSlim", "fitnesse.slim.test.TestSlim", new Object[0]);
    testSlim = (TestSlim) caller.getInstance("testSlim");
  }

  @Test
  public void callNiladicFunction() throws Exception {
    caller.call("testSlim", "nilad");
    assertTrue(testSlim.niladWasCalled());
  }

  @Test
  public void throwMethodNotCalledErrorIfNoSuchMethod() throws Exception {
    String response = (String)caller.call("testSlim", "noSuchMethod");
    assertTrue(response,
      response.indexOf(SlimServer.EXCEPTION_TAG) != -1 && 
      response.indexOf("message:<<NO_METHOD_IN_CLASS noSuchMethod[0] fitnesse.slim.test.TestSlim.>>") != -1);
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
  public void passOneList() throws Exception {
    caller.call("testSlim", "oneList", list("one", "two"));
    assertEquals(list("one", "two"), testSlim.getListArg());
  }

  @Test
  public void passManyArgs() throws Exception {
    caller.call("testSlim", "manyArgs", "1", "2.1", "c");
    assertEquals(1, testSlim.getIntegerObjectArg());
    assertEquals(2.1, testSlim.getDoubleObjectArg(), .00001);
    assertEquals('c', testSlim.getCharArg());
  }

  @Test
  public void convertLists() throws Exception {
    caller.call("testSlim", "oneList", "[1 ,2, 3,4, hello Bob]");
    assertEquals(list("1","2","3","4","hello Bob"), caller.call("testSlim", "getListArg"));
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

}
