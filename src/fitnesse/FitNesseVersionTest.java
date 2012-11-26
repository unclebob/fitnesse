package fitnesse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FitNesseVersionTest {

  @Test
  public void doesLoadVersionResourceFile() {
    FitNesseVersion version = new FitNesseVersion();
    assertTrue(version.toString().startsWith("v"));
    assertFalse(version.toString().endsWith("\n"));
  }

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

  @Test
  public void veryOldImaginaryVersionDoesntCauseRuntimeException() throws Exception {
    FitNesseVersion version = new FitNesseVersion("v20100613");
    assertTrue(version.isAtLeast("v100303"));
  }

  @Test
  public void dateVersionsHandlesVersionsWithExtraSuffixes() throws Exception {
    assertEquals((Long)20100101L, FitNesseVersion.dateVersion("v20100101-abc-1"));
  }

  @Test
  public void dateVersionsHandlesVersionsWithNoExtraSuffixes() throws Exception {
    assertEquals((Long)20100101L, FitNesseVersion.dateVersion("v20100101"));
  }

  @Test
  public void versionsWithSuffixesCompareToVersionsWithoutSuffix() throws Exception {
    FitNesseVersion version = new FitNesseVersion("v20100101-abc-1");
    assertFalse(version.isAtLeast("v20100303"));
  }

  @Test
  public void suffixesAreIgnoredWhenComparingVersions() throws Exception {
    FitNesseVersion version = new FitNesseVersion("v20100101-abc-1");
    assertFalse(version.isAtLeast("v20100303-abc-2"));
  }
}
