package fitnesse;

import static org.junit.Assert.*;

import org.junit.Test;

public class FitNesseVersionTest {

  @Test
  public void earlierVersionDateShouldNotBeAtLeastVersion() throws Exception {
    FitNesseVersion version = new FitNesseVersion("v20100101");
    assertFalse(version.isAtLeast("v20100303"));
  }

  @Test
  public void exactVersionDateShouldBeAtLeastVersion() throws Exception {
    FitNesseVersion version = new FitNesseVersion("v20100303");
    assertTrue(version.isAtLeast("v20100303"));
  }

  @Test
  public void laterVersionDateShouldBeAtLeastVersion() throws Exception {
    FitNesseVersion version = new FitNesseVersion("v20100613");
    assertTrue(version.isAtLeast("v20100303"));
  }
}
