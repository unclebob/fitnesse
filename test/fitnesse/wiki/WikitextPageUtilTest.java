package fitnesse.wiki;

import fitnesse.wikitext.parser.HeaderLine;
import fitnesse.wikitext.parser.Literal;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WikitextPageUtilTest {

  @Test
  public void testGetSymbols() {
    final Symbol parent = new Symbol(Literal.symbolType);
    final Symbol child = new Symbol(HeaderLine.symbolType);
    parent.add(child);
    assertEquals(child, WikitextPageUtil.getSymbols(new WikitextPage() {
      @Override
      public Symbol getSyntaxTree() {
        return parent;
      }

      @Override
      public ParsingPage getParsingPage() {
        return null;
      }
    }, HeaderLine.symbolType).get(0));
  }

}
