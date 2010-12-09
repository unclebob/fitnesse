package fitnesse.slim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SlimHelperLibraryTest {
  private static final String ACTOR_INSTANCE_NAME = "scriptTableActor";
  private StatementExecutorInterface caller;

  private String getTestClassName() {
    return "fitnesse.slim.test.TestSlim";
  }

  @Before
  public void setUp() throws Exception {
    caller = new StatementExecutor();
  }

  @Test
  public void testSlimHelperLibraryHasStatementExecutor() throws Exception {
    SlimHelperLibrary helperLibrary = makeSlimHelperLibraryWithStatementExecutor();
    assertSame(caller, helperLibrary.getStatementExecutor());
  }
  
  @Test
  public void testSlimHelperLibraryCanPushAndPopFixture() throws Exception {
    SlimHelperLibrary helperLibrary = makeSlimHelperLibraryWithStatementExecutor();
    Object response = caller.create(ACTOR_INSTANCE_NAME, getTestClassName(), new Object[0]);
    Object firstActor = caller.getInstance(ACTOR_INSTANCE_NAME);

    helperLibrary.pushFixture();
    
    response = caller.create(ACTOR_INSTANCE_NAME, getTestClassName(), new Object[] {"1"});
    assertEquals("OK", response);
    assertNotSame(firstActor, caller.getInstance(ACTOR_INSTANCE_NAME));
    
    helperLibrary.popFixture();
    
    assertSame(firstActor, caller.getInstance(ACTOR_INSTANCE_NAME));
  }

  public SlimHelperLibrary makeSlimHelperLibraryWithStatementExecutor() {
    Object response = caller.create("x", "fitnesse.slim.SlimHelperLibrary", new Object[0]);
    assertEquals("OK", response);
    SlimHelperLibrary helperLibrary = (SlimHelperLibrary) caller.getInstance("x");
    return helperLibrary;
  }

}
