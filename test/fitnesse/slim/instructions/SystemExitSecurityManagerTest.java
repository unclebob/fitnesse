package fitnesse.slim.instructions;

import static org.junit.Assert.fail;
import static util.RegexTestCase.assertMatches;

import org.junit.After;
import org.junit.Test;

import fitnesse.slim.instructions.SystemExitSecurityManager.SystemExitException;

public class SystemExitSecurityManagerTest {

  SecurityManager oldSecurityManager;

  SecurityManager securityManager;

  @After
  public void teardown() {
    SystemExitSecurityManager.restoreOriginalSecurityManager();
  }

  @Test(expected = SystemExitException.class)
  public void shouldThrowExceptionWhenSystemExitIsCalled() {
    acticateSystemExitSecurityManager();
    System.exit(0);
    fail("should have thrown exception");

  }

  @Test
  public void shouldIncludeExitCode() {
    try {
      acticateSystemExitSecurityManager();
      System.exit(42);
      fail("should have thrown exception");
    } catch (SystemExitException e) {
      assertMatches("system exit with exit code 42", e.getMessage());
    }
  }
  
  private void acticateSystemExitSecurityManager() {
    System.setSecurityManager(null);
    System.setProperty(SystemExitSecurityManager.PREVENT_SYSTEM_EXIT, "true");
    SystemExitSecurityManager.activateIfWanted();
  }
}
