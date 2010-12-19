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
    Object response = caller.create("x", "fitnesse.slim.SlimHelperLibrary", new Object[0]);
    assertEquals("OK", response);
    SlimHelperLibrary x = (SlimHelperLibrary) caller.getInstance("x");
    assertSame(caller, x.getStatementExecutor());
  }
  
  @Test
  public void testSlimHelperLibraryCanPushAndPopTableActor() throws Exception {
    Object response = caller.create("x", "fitnesse.slim.SlimHelperLibrary", new Object[0]);
    response = caller.create(ACTOR_INSTANCE_NAME, getTestClassName(), new Object[0]);
    Object firstActor = caller.getInstance(ACTOR_INSTANCE_NAME);
    SlimHelperLibrary helperLibrary = (SlimHelperLibrary) caller.getInstance("x");

    helperLibrary.pushActor();
    
    response = caller.create(ACTOR_INSTANCE_NAME, getTestClassName(), new Object[] {"1"});
    assertEquals("OK", response);
    assertNotSame(firstActor, caller.getInstance(ACTOR_INSTANCE_NAME));
    
    helperLibrary.popActor();
    
    assertSame(firstActor, caller.getInstance(ACTOR_INSTANCE_NAME));
  }

}
