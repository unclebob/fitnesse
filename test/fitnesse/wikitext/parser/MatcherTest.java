package fitnesse.wikitext.parser;

import org.junit.Assert;
import org.junit.Test;

public class MatcherTest {
  @Test public void optionalMatch() {
    assertOptionalMatch(1, ">", ">");
    assertOptionalMatch(1, "<", "<");
    assertOptionalMatch(0, "", "x");
  }

  @Test public void newLine() {
    Matcher matcher = new Matcher().newLine();
    Assert.assertEquals(2, matcher.makeMatch(new ScanString("\r\n", 0)).getLength());
    Assert.assertEquals(1, matcher.makeMatch(new ScanString("\n", 0)).getLength());
    Assert.assertFalse(matcher.makeMatch(new ScanString("\r", 0)).isMatched());
  }

  @Test public void findMatch() {
    Matcher matcher = new Matcher().string("stuff");
    ScanString input = new ScanString("somestuff", 0);
    Assert.assertEquals(5, matcher.findMatch(input).getLength());
    Assert.assertEquals(4, input.getOffset());
    Assert.assertFalse(matcher.findMatch(new ScanString("stuff not here", 5)).isMatched());
  }

  private void assertOptionalMatch(int length, String options, String input) {
    Matcher matcher = new Matcher().optional("<", ">");
    MatchResult result = matcher.makeMatch(new ScanString(input, 0));
    Assert.assertEquals(length, result.getLength());
    Assert.assertEquals(options, String.join("", result.getOptions()));
  }
}
