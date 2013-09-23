package fitnesse.wikitext;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

  @Test
  public void shouldEscapeOnlyXmlCharacters() {
    assertEquals("ab&amp;cd&lt;ef&gt;", Utils.escapeHTML("ab&cd<ef>"));
  }

  @Test
  public void shouldEscapeMultipleOccurencesOfTheSameCharacter() {
    assertEquals("ab&amp;cd&amp;ef&amp;", Utils.escapeHTML("ab&cd&ef&"));
  }
}
