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
    Object response = caller.create("x", "fitnesse.slim.test.TestSlim");
    assertEquals("OK", response);
    Object x = caller.getInstance("x");
    assertTrue(x instanceof TestSlim);
  }

  @Test
  public void throwsInstanceNotCreatedErrorIfNoSuchClass() throws Exception {
    String result = (String) caller.create("x", "fitness.slim.test.NoSuchClass");
    assertTrue(result.indexOf(SlimServer.EXCEPTION_TAG) != -1);
  }

  @Test
  public void throwsInstanceNotCreatedErrorIfNoPublicDefaultConstructor() throws Exception {
    String result = (String) caller.create("x", "fitnesse.slim.test.ClassWithNoPublicDefaultConstructor");
    assertTrue(result.indexOf(SlimServer.EXCEPTION_TAG) != -1);
  }


}
