package fitnesse.responders.versions;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static util.RegexTestCase.assertSubString;

public class VersionComparerTest {
  
  private VersionComparer comparer;
  private String originalContent;
  private String revisedContent;

  @Before
  public void setUp() {
    comparer = new VersionComparer();
    originalContent = "First line in content\nLine in first content\nLast line in content.";
    revisedContent = "First line in content\nLine in second content\nLast line in content.";
  }
  
  @Test
  public void testSimpleCompare() {
    comparer.compare("1", originalContent, "2", revisedContent);
    assertSubString("-Line in first content", StringUtils.join(comparer.getDifferences(), "\n"));
    assertSubString("+Line in second content", StringUtils.join(comparer.getDifferences(), "\n"));
  }

  @Test
  public void testPadsNonDifferentLinesWithSpaces() {
    comparer.compare("1", originalContent, "2", revisedContent);
    assertSubString(" First line in content", StringUtils.join(comparer.getDifferences(), "\n"));
    assertSubString(" Last line in content.", StringUtils.join(comparer.getDifferences(), "\n"));
  }
}
