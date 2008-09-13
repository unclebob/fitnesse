package fitnesse.slim;

import fitnesse.slim.test.TestSlim;
import static org.junit.Assert.*;
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
    Object x = caller.create("x", "fitnesse.slim.test.TestSlim");
    assertTrue(x instanceof TestSlim);
  }

  @Test
  public void createdInstanceIsAssignedToName() throws Exception {
    Object x = caller.create("x", "fitnesse.slim.test.TestSlim");
    Object x2 = caller.getInstance("x");
    assertSame(x, x2);
  }

  @Test
  public void throwsInstanceNotCreatedErrorIfNoSuchClass() throws Exception {
    try {
      caller.create("x", "fitness.slim.test.NoSuchClass");
      fail("should throw because no such class exists.");
    } catch (SlimError e) {
    }
  }

  @Test
  public void throwsInstanceNotCreatedErrorIfNoPublicDefaultConstructor() throws Exception {
    try {
      caller.create("x", "fitnesse.slim.test.ClassWithNoPublicDefaultConstructor");
      fail("Should throw because no public constructor");
    } catch (SlimError e) {
    }
  }


}
