package fitnesse.slim;

import fitnesse.slim.test.TestSlim;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SlimInstanceCreationTest {
  private StatementExecutor caller;

  @Before
  public void setUp() throws Exception {
    caller = new StatementExecutor();
  }

  @Test
  public void canCreateInstance() throws Exception {
    Object response = caller.create("x", "fitnesse.slim.test.TestSlim", new Object[0]);
    assertEquals("OK", response);
    Object x = caller.getInstance("x");
    assertTrue(x instanceof TestSlim);
  }

  @Test
  public void canCreateInstanceWithArguments() throws Exception {
    Object response = caller.create("x", "fitnesse.slim.test.TestSlim", new Object[]{"3"});
    assertEquals("OK", response);
    Object x = caller.getInstance("x");
    assertTrue(x instanceof TestSlim);
  }

  @Test
  public void cantCreateInstanceIfConstructorArgumentBad() throws Exception {
    String result = (String) caller.create("x", "fitnesse.slim.test.TestSlim", new Object[]{"notInt"});
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR fitnesse.slim.test.TestSlim[1]>>", result);
  }

  @Test
  public void cantCreateInstanceIfConstructorArgumentCountIncorrect() throws Exception {
    String result = (String) caller.create("x", "fitnesse.slim.test.TestSlim", new Object[]{"3", "4"});
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR fitnesse.slim.test.TestSlim[2]>>", result);
  }


  @Test
  public void throwsInstanceNotCreatedErrorIfNoSuchClass() throws Exception {
    String result = (String) caller.create("x", "fitnesse.slim.test.NoSuchClass", new Object[0]);
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR fitnesse.slim.test.NoSuchClass[0]>>", result);
  }

  @Test
  public void throwsInstanceNotCreatedErrorIfNoPublicDefaultConstructor() throws Exception {
    String result = (String) caller.create("x", "fitnesse.slim.test.ClassWithNoPublicDefaultConstructor", new Object[0]);
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR fitnesse.slim.test.ClassWithNoPublicDefaultConstructor[0]>>", result);
  }

  private void assertException(String message, String result) {
    assertTrue(result, result.indexOf(SlimServer.EXCEPTION_TAG) != -1 && result.indexOf(message) != -1);
  }
}
