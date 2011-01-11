package fitnesse.slim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SlimHelperLibraryTest {
  private static final String SLIM_HELPER_LIBRARY_INSTANCE_NAME = "SlimHelperLibrary";
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
  public void testSlimHelperLibraryIsStoredInSlimExecutor() throws Exception {
    Object helperLibrary = caller.getInstance(SLIM_HELPER_LIBRARY_INSTANCE_NAME);
    assertTrue(helperLibrary instanceof SlimHelperLibrary);
  }
  
  @Test
  public void testSlimHelperLibraryHasStatementExecutor() throws Exception {
    SlimHelperLibrary helperLibrary = (SlimHelperLibrary) caller.getInstance(SLIM_HELPER_LIBRARY_INSTANCE_NAME);
    assertSame(caller, helperLibrary.getStatementExecutor());
  }
  
  @Test
  public void testSlimHelperLibraryCanPushAndPopFixture() throws Exception {
    SlimHelperLibrary helperLibrary = (SlimHelperLibrary) caller.getInstance(SLIM_HELPER_LIBRARY_INSTANCE_NAME);
    Object response = caller.create(ACTOR_INSTANCE_NAME, getTestClassName(), new Object[0]);
    Object firstActor = caller.getInstance(ACTOR_INSTANCE_NAME);

    helperLibrary.pushFixture();
    
    response = caller.create(ACTOR_INSTANCE_NAME, getTestClassName(), new Object[] {"1"});
    assertEquals("OK", response);
    assertNotSame(firstActor, caller.getInstance(ACTOR_INSTANCE_NAME));
    
    helperLibrary.popFixture();
    
    assertSame(firstActor, caller.getInstance(ACTOR_INSTANCE_NAME));
  }

}
