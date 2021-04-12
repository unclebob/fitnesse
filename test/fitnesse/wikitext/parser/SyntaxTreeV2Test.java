package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.SyntaxTree;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SyntaxTreeV2Test {

  @Test
  public void findPaths() {
    WikiPage page = new TestRoot().makePage("TestPage", "!path stuff\n!note and\n!path nonsense");
    SyntaxTree syntaxTree = ParserTestHelper.parseSyntax(page);
    List<String> paths = new ArrayList<>();
    syntaxTree.findPaths(paths::add);
    assertEquals(2, paths.size());
    assertEquals("stuff", paths.get(0));
    assertEquals("nonsense", paths.get(1));
  }
}
