package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.ParsedPage;
import fitnesse.wikitext.parser.ParsingPage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParsedPageTest {
  @Test
  public void parsesContent() {
    ParsedPage parsedPage = new ParsedPage(new ParsingPage(new TestSourcePage()), "''hi''");
    assertEquals("<i>hi</i>", parsedPage.toHtml());
  }

  @Test
  public void usesVariablesFromSourcePage() {
    ParsedPage sourcePage = new ParsedPage(new ParsingPage(new TestSourcePage()), "!define x {hi}");
    ParsedPage parsedPage = new ParsedPage(sourcePage, "${x}");
    assertEquals("hi", parsedPage.toHtml());
  }

  @Test
  public void addsPageToFront() {
    ParsedPage pageToAddFrom = new ParsedPage(new ParsingPage(new TestSourcePage()), "'''hi'''");
    ParsedPage pageToAddTo = new ParsedPage(new ParsingPage(new TestSourcePage()), "''mom''");
    pageToAddTo.addToFront(pageToAddFrom);
    assertEquals("<b>hi</b><i>mom</i>", pageToAddTo.toHtml());
  }
}
