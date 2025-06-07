package fitnesse.slim.instructions;

import fitnesse.slim.instructions.SystemExitSecurityManager.SystemExitException;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.fail;
import static util.RegexTestCase.assertMatches;

public class SystemExitSecurityManagerTest {
  @After
  public void teardown() {
    SystemExitSecurityManager.restoreOriginalSecurityManager();
  }

  @Test(expected = SystemExitException.class)
  public void shouldThrowExceptionWhenSystemExitIsCalled() {
    activateSystemExitSecurityManager();
    System.exit(0);
    fail("should have thrown exception");
  }

  @Test
  public void shouldIncludeExitCode() {
    try {
      activateSystemExitSecurityManager();
      System.exit(42);
      fail("should have thrown exception");
    } catch (SystemExitException e) {
      assertMatches("system exit with exit code 42", e.getMessage());
    }
  }

  private void activateSystemExitSecurityManager() {
    System.setSecurityManager(null);
    System.setProperty(SystemExitSecurityManager.PREVENT_SYSTEM_EXIT, "true");
    SystemExitSecurityManager.activateIfWanted();
  }
}
