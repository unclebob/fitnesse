package fitnesse.util;

import static fitnesse.util.HtmlParserTools.deepClone;
import static fitnesse.util.HtmlParserTools.flatClone;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.Div;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;

public class HtmlParserToolsTest {

  @Test
  public void shoudlMakeExactCopy() throws ParserException, CloneNotSupportedException {
    String html = "<div class='foo'>funky <em>content</em></div>";
    Parser parser = new Parser(new Lexer(new Page(html)));
    NodeList tree = parser.parse(null);

    NodeList cloneTree = deepClone(tree);

    assertEquals(html, cloneTree.toHtml());
    assertEquals(tree.toString(), cloneTree.toString());
    assertFalse(tree.elementAt(0).getChildren().elementAt(1) == cloneTree.elementAt(0).getChildren().elementAt(1));
    assertFalse(tree.elementAt(0).getChildren().elementAt(1).getParent() == cloneTree.elementAt(0).getChildren().elementAt(1).getParent());
  }

  @Test
  public void shouldAlsoCloneAttributes() throws ParserException, CloneNotSupportedException {
    String html = "<div class='foo'>funky <em>content</em></div>";
    Parser parser = new Parser(new Lexer(new Page(html)));
    NodeList tree = parser.parse(null);

    NodeList cloneTree = deepClone(tree);

    assertSame(Div.class, cloneTree.elementAt(0).getClass());

    ((Div) cloneTree.elementAt(0)).setAttribute("id", "blah-div");

    assertFalse(tree.toHtml().equals(cloneTree.toHtml()));
  }

  @Test
  public void flatCloneShouldJustGiveACopyOfANode() throws ParserException {
    String html = "<div class='foo'>funky <em>content</em></div>";
    Parser parser = new Parser(new Lexer(new Page(html)));
    NodeList tree = parser.parse(null);

    Node copy = flatClone(tree.elementAt(0));

    assertNull(copy.getParent());
    assertEquals(0, copy.getChildren().size());
  }
}
