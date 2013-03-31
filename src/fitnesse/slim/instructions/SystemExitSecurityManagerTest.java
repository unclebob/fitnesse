package fitnesse.slim.instructions;

import static org.junit.Assert.fail;
import static util.RegexTestCase.assertMatches;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.slim.instructions.SystemExitSecurityManager.SystemExitException;

public class SystemExitSecurityManagerTest {

  SecurityManager oldSecurityManager;

  SecurityManager securityManager;

  @Before
  public void setup() {
    oldSecurityManager = System.getSecurityManager();
    securityManager = new SystemExitSecurityManager(oldSecurityManager);
    System.setSecurityManager(securityManager);
  }

  @After
  public void teardown() {
    System.setSecurityManager(oldSecurityManager);
  }

  @Test
  public void shouldThrowExceptionWhenSystemExitIsCalled() {
    try {
      System.exit(0);
      fail("should have thrown exception");
    } catch (SystemExitException e) {
    }
  }

  @Test
  public void shouldIncludeExitCode() {
    try {
      System.exit(42);
      fail("should have thrown exception");
    } catch (SystemExitException e) {
      assertMatches("system exit with exit code 42", e.getMessage());
    }
  }
}
