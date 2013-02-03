package fitnesse.responders.versions;

import org.junit.Test;

import util.RegexTestCase;
import util.StringUtil;

public class VersionComparerTest extends RegexTestCase {

  @Test
  public void testSimpleCompare() {
    VersionComparer comparer = new VersionComparer();
    String originalContent = "First line in content\nLine in first content\nLast line in content.";
    String revisedContent = "First line in content\nLine in second content\nLast line in content.";
    comparer.compare("1", originalContent, "2", revisedContent);
    for(String line : comparer.getDifferences())
      System.out.println(line);
    assertSubString("-Line in first content", StringUtil.join(comparer.getDifferences(), System.getProperty("line.separator")));
    assertSubString("+Line in second content", StringUtil.join(comparer.getDifferences(), System.getProperty("line.separator")));
  }
}
