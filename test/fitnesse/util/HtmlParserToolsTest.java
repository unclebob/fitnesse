package fitnesse.util;

import static fitnesse.util.HtmlParserTools.deepClone;
import static fitnesse.util.HtmlParserTools.flatClone;
import static fitnesse.util.HtmlParserTools.nodeHasClass;
import static org.junit.Assert.*;

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
    NodeList tree = parseToTree(html);

    NodeList cloneTree = deepClone(tree);

    assertEquals(html, cloneTree.toHtml());
    assertEquals(tree.toString(), cloneTree.toString());
    assertFalse(tree.elementAt(0).getChildren().elementAt(1) == cloneTree.elementAt(0).getChildren().elementAt(1));
    assertFalse(tree.elementAt(0).getChildren().elementAt(1).getParent() == cloneTree.elementAt(0).getChildren().elementAt(1).getParent());
  }

  @Test
  public void shouldAlsoCloneAttributes() throws ParserException, CloneNotSupportedException {
    NodeList tree = parseToTree("<div class='foo'>funky <em>content</em></div>");

    NodeList cloneTree = deepClone(tree);

    assertSame(Div.class, cloneTree.elementAt(0).getClass());

    ((Div) cloneTree.elementAt(0)).setAttribute("id", "blah-div");

    assertFalse(tree.toHtml().equals(cloneTree.toHtml()));
  }

  @Test
  public void flatCloneShouldJustGiveACopyOfANode() throws ParserException {
    NodeList tree = parseToTree("<div class='foo'>funky <em>content</em></div>");

    Node copy = flatClone(tree.elementAt(0));

    assertNull(copy.getParent());
    assertEquals(0, copy.getChildren().size());
  }

  @Test
  public void hasClassShouldSayNoOnNoClasses() throws ParserException {
    NodeList tree = parseToTree("<div>content</div>");

    assertFalse(nodeHasClass(tree.elementAt(0), "foo"));
  }

  @Test
  public void hasClassShouldSayNoOnOtherClasses() throws ParserException {
    NodeList tree = parseToTree("<div class='fooe foor'>content</div>");

    assertFalse(nodeHasClass(tree.elementAt(0), "foo"));
  }

  @Test
  public void hasClassShouldSayYesWhenFound() throws ParserException {
    NodeList tree = parseToTree("<div class='fooe foo foor'>content</div>");

    assertTrue(nodeHasClass(tree.elementAt(0), "foo"));
  }

  @Test
  public void hasClassShouldSayNoForNonTagNode() throws ParserException {
    NodeList tree = parseToTree("text node");

    assertFalse(nodeHasClass(tree.elementAt(0), "foo"));
  }

  private static NodeList parseToTree(String html) throws ParserException {
    Parser parser = new Parser(new Lexer(new Page(html)));
    return parser.parse(null);
  }
}
