package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class AnchorReferenceTokenTest {
    @Test public void scansAnchors() {
        ParserTest.assertScans(".#anchorName", "AnchorReferenceToken=anchorName");
        ParserTest.assertScans(".# anchorName", "TextToken=.# anchorName");
        ParserTest.assertScans(".#anchor Name", "AnchorReferenceToken=anchor,TextToken= Name");
        ParserTest.assertScans("blah.#anchorName", "TextToken=blah,AnchorReferenceToken=anchorName");
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslates(".#anchorName", "<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl);
        ParserTest.assertTranslates(".#anchorName stuff", "<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more.#anchorName stuff", "more<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
        ParserTest.assertTranslates("more\r\n.#anchorName stuff", "more\r\n<a href=\"#anchorName\">.#anchorName</a>" + HtmlElement.endl + " stuff");
    }
}
