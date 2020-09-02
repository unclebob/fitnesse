package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SyntaxTreeTest {

  @Test
  public void getHeaderLines() {
    final Symbol parent = new Symbol(Literal.symbolType);
    final Symbol child = new Symbol(HeaderLine.symbolType);
    parent.add(child);
    assertEquals(child, new SyntaxTree(parent).findHeaderLines().get(0));
  }

  @Test
  public void findPaths() {
    WikiPage page = new TestRoot().makePage("TestPage", "!path stuff\n!note and\n!path nonsense");
    List<String> paths = new SyntaxTree(ParserTestHelper.parse(page)).findPaths(new HtmlTranslator(new WikiSourcePage(page), new ParsingPage(new WikiSourcePage(page))));
    assertEquals(2, paths.size());
    assertEquals("stuff", paths.get(0));
    assertEquals("nonsense", paths.get(1));
  }
}
