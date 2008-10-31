package fitnesse.slim;

import fitnesse.slim.converters.VoidConverter;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.test.TestSlim;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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

  private List<Object> list(Object... objects) {
    return Arrays.asList(objects);
  }


  @Test
  public void passManyArgs() throws Exception {
    caller.call("testSlim", "manyArgs", "1", "2.1", "c");
    assertEquals(1, testSlim.getIntegerObjectArg());
    assertEquals(2.1, testSlim.getDoubleObjectArg(), .00001);
    assertEquals('c', testSlim.getCharArg());
  }


}
