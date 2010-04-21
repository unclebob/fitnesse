package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class AnchorNameTest {
    @Test public void scansAnchors() {
        ParserTest.assertScansTokenType("!anchor name", SymbolType.AnchorName, true);
        ParserTest.assertScansTokenType("! anchor name", SymbolType.AnchorName, false);
    }

    @Test public void parsesAnchors() throws Exception {
        ParserTest.assertParses("!anchor name", "SymbolList[AnchorName[Text]]");
        ParserTest.assertParses("!anchor 1234", "SymbolList[AnchorName[Text]]");
        ParserTest.assertParses("!anchor @#$@#%", "SymbolList[Text, Whitespace, Text]");
        ParserTest.assertParses("!anchorname", "SymbolList[Text]");
    }

    @Test public void translatesAnchors() {
        ParserTest.assertTranslatesTo("!anchor name", anchorWithName("name"));
        ParserTest.assertTranslatesTo("!anchor name stuff", anchorWithName("name") + " stuff");
        ParserTest.assertTranslatesTo("more!anchor name stuff", "more" + anchorWithName("name") + " stuff");
        ParserTest.assertTranslatesTo("more !anchor name stuff", "more " + anchorWithName("name") + " stuff");
    }

    private String anchorWithName(String name) {
        return "<a name=\"" + name + "\"/>";
    }
}
