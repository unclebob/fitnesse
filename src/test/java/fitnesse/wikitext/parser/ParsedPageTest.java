package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.ParsedPage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParsedPageTest {
  @Test
  public void parsesContent() {
    ParsedPage parsedPage = new ParsedPage(new TestSourcePage(), "''hi''");
    assertEquals("<i>hi</i>", parsedPage.toHtml());
  }

  @Test
  public void usesVariablesFromSourcePage() {
    ParsedPage sourcePage = new ParsedPage(new TestSourcePage(), "!define x {hi}");
    ParsedPage parsedPage = new ParsedPage(sourcePage, "${x}");
    assertEquals("hi", parsedPage.toHtml());
  }

  @Test
  public void addsPageToFront() {
    ParsedPage pageToAddFrom = new ParsedPage(new TestSourcePage(), "'''hi'''");
    ParsedPage pageToAddTo = new ParsedPage(new TestSourcePage(), "''mom''");
    pageToAddTo.addToFront(pageToAddFrom);
    assertEquals("<b>hi</b><i>mom</i>", pageToAddTo.toHtml());
  }
}
