package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class ImageTest {
    @Test
    public void scansImages() {
        ParserTest.assertScansTokenType("!img name", "Image", true);
    }

    @Test
    public void parsesImages() throws Exception {
        ParserTest.assertParses("!img name", "SymbolList[Link[SymbolList[Text]]]");
        ParserTest.assertParses("!img http://name", "SymbolList[Link[SymbolList[Text]]]");
        ParserTest.assertParses("!imgx name", "SymbolList[Text, Whitespace, Text]");
        ParserTest.assertParses("!img-l name", "SymbolList[Link[SymbolList[Text]]]");
        ParserTest.assertParses("!img-r name", "SymbolList[Link[SymbolList[Text]]]");
    }

    @Test
    public void translatesImages() {
        ParserTest.assertTranslatesTo("!img name", "<img src=\"name\"/>");
        ParserTest.assertTranslatesTo("!img http://name", "<img src=\"http://name\"/>");
        ParserTest.assertTranslatesTo("!img-l name", "<img src=\"name\" class=\"left\"/>");
        ParserTest.assertTranslatesTo("!img-r name", "<img src=\"name\" class=\"right\"/>");
    }
}
