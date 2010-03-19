package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class AnchorReferenceTokenTest {
    @Test public void scansAnchors() {
        ParserTest.assertScans(".#anchorName", "AnchorReference,Word=anchorName");
        ParserTest.assertScans(".# anchorName", "AnchorReference,Whitespace= ,Word=anchorName");
        ParserTest.assertScans(".#anchor Name", "AnchorReference,Word=anchor,Whitespace= ,Word=Name");
        ParserTest.assertScans("blah.#anchorName", "Word=blah,AnchorReference,Word=anchorName");
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslates(".#anchorName", "<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl);
        ParserTest.assertTranslates(".# anchorName", ".# anchorName");
        ParserTest.assertTranslates(".#anchorName stuff", "<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more.#anchorName stuff", "more<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more\r\n.#anchorName stuff", "more\r\n<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
    }
}
