package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.test.ParserTest;
import org.junit.Test;

public class AnchorReferenceTest {
    @Test public void scansAnchors() {
        ParserTest.assertScansTokenType(".#anchorName", "AnchorReference", true);
        ParserTest.assertScansTokenType(".# anchorName", "AnchorReference", true);
        ParserTest.assertScansTokenType(". #anchor Name", "AnchorReference", false);
        ParserTest.assertScansTokenType("blah.#anchorName", "AnchorReference", true);
    }

    @Test public void parsesAnchors() throws Exception {
        ParserTest.assertParses(".#anchorName", "SymbolList[AnchorReference[Text]]");
        ParserTest.assertParses(".# anchorName", "SymbolList[Text, Whitespace, Text]");
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslatesTo(".#anchorName", anchorReferenceWithName("anchorName"));
        ParserTest.assertTranslatesTo(".#anchorName stuff", anchorReferenceWithName("anchorName") + " stuff");
        ParserTest.assertTranslatesTo("more.#anchorName stuff", "more" + anchorReferenceWithName("anchorName") + " stuff");
        ParserTest.assertTranslatesTo("more\n.#anchorName stuff",
                "more" + ParserTest.newLineRendered + anchorReferenceWithName("anchorName") + " stuff");
    }

    private String anchorReferenceWithName(String name) {
        return "<a href=\"#" + name + "\">.#" + name + "</a>" + HtmlElement.endl;
    }
}
