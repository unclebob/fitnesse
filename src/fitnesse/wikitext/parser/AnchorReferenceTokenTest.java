package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class AnchorReferenceTokenTest {
    @Test public void scansAnchors() {
        ParserTest.assertScansTokenType(".#anchorName", TokenType.AnchorReference, true);
        ParserTest.assertScansTokenType(".# anchorName", TokenType.AnchorReference, true);
        ParserTest.assertScansTokenType(".#anchor Name", TokenType.AnchorReference, true);
        ParserTest.assertScansTokenType("blah.#anchorName", TokenType.AnchorReference, true);
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslates(".#anchorName", "<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl);
        ParserTest.assertTranslates(".# anchorName", ".# anchorName");
        ParserTest.assertTranslates(".#anchorName stuff", "<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more.#anchorName stuff", "more<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more\r\n.#anchorName stuff", "more\r\n<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
    }
}
