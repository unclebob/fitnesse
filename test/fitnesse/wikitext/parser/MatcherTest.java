package fitnesse.wikitext.parser;

import org.junit.Assert;
import org.junit.Test;

public class MatcherTest {
  @Test public void optionalMatch() {
    assertOptionalMatch(1, ">", ">");
    assertOptionalMatch(1, "<", "<");
    assertOptionalMatch(0, "", "x");
  }

  private void assertOptionalMatch(int length, String options, String input) {
    Matcher matcher = new Matcher().optional("<", ">");
    MatchResult result = matcher.makeMatch(new ScanString(input, 0), new SymbolStream());
    Assert.assertEquals(length, result.getLength());
    Assert.assertEquals(options, String.join("", result.getOptions()));
  }
}
