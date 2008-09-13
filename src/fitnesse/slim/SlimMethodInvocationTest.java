package fitnesse.slim;

import fitnesse.slim.test.TestSlim;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SlimMethodInvocationTest {
  private StatementExecutor caller;
  private TestSlim testSlim;

  @Before
  public void setUp() {
    caller = new StatementExecutor();
    testSlim = (TestSlim) caller.create("testSlim", "fitnesse.slim.test.TestSlim");
  }

  @Test
  public void callNiladicFunction() throws Exception {
    caller.call("testSlim","nilad");
    assertTrue(testSlim.niladWasCalled());
  }

  @Test(expected = SlimError.class)
  public void throwMethodNotCalledErrorIfNoSuchMethod() throws Exception {
    caller.call("testSlim", "noSuchMethod");
  }

  @Test
  public void methodReturnsString() throws Exception {
    String retval = caller.call("testSlim", "returnString");
    assertEquals("string", retval);
  }

  @Test
  public void methodReturnsInt() throws Exception {
    String retval = caller.call("testSlim", "returnInt");
    assertEquals("7", retval);
  }

  @Test
  public void methodReturnsVoid() throws Exception {
    String retval = caller.call("testSlim", "nilad");
    assertEquals("VOID", retval);
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
  public void passManyArgs() throws Exception {
    caller.call("testSlim", "manyArgs", "1", "2.1", "c");
    assertEquals(1, testSlim.getIntegerObjectArg());
    assertEquals(2.1, testSlim.getDoubleObjectArg(), .00001);
    assertEquals('c', testSlim.getCharArg());
  }


}
