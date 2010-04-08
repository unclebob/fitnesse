package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.test.ParserTest;
import org.junit.Test;

public class AnchorReferenceTest {
    @Test public void scansAnchors() {
        ParserTest.assertScansTokenType(".#anchorName", SymbolType.AnchorReference, true);
        ParserTest.assertScansTokenType(".# anchorName", SymbolType.AnchorReference, true);
        ParserTest.assertScansTokenType(". #anchor Name", SymbolType.AnchorReference, false);
        ParserTest.assertScansTokenType("blah.#anchorName", SymbolType.AnchorReference, true);
    }

    @Test public void parsesAnchors() {
        ParserTest.assertParses(".#anchorName", "SymbolList[AnchorReference[Text]]");
        ParserTest.assertParses(".# anchorName", "SymbolList[Text, Whitespace, Text]");
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslatesTo(".#anchorName", anchorReferenceWithName("anchorName"));
        ParserTest.assertTranslatesTo(".#anchorName stuff", anchorReferenceWithName("anchorName") + " stuff");
        ParserTest.assertTranslatesTo("more.#anchorName stuff", "more" + anchorReferenceWithName("anchorName") + " stuff");
        ParserTest.assertTranslatesTo("more\n.#anchorName stuff",
                "more<br/>" + HtmlElement.endl
                + anchorReferenceWithName("anchorName")
                + " stuff");
    }

    private String anchorReferenceWithName(String name) {
        return "<a href=\"#" + name + "\">.#" + name + "</a>" + HtmlElement.endl;
    }
}
