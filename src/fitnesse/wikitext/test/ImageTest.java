package fitnesse.wikitext.test;

import org.junit.Test;

import fitnesse.wiki.WikiPage;

public class ImageTest {
    @Test
    public void scansImages() {
        ParserTestHelper.assertScansTokenType("!img name", "Image", true);
    }

    @Test
    public void parsesImages() throws Exception {
        ParserTestHelper.assertParses("!img name", "SymbolList[Link[SymbolList[Text]]]");
        ParserTestHelper.assertParses("!img http://name", "SymbolList[Link[SymbolList[Text]]]");
        ParserTestHelper.assertParses("!imgx name", "SymbolList[Text, Whitespace, Text]");
        ParserTestHelper.assertParses("!img-l name", "SymbolList[Link[SymbolList[Text]]]");
        ParserTestHelper.assertParses("!img-r name", "SymbolList[Link[SymbolList[Text]]]");
    }

    @Test
    public void translatesImages() {
        ParserTestHelper.assertTranslatesTo("!img name", "<img src=\"name\"/>");
        ParserTestHelper.assertTranslatesTo("!img http://name", "<img src=\"http://name\"/>");
        ParserTestHelper.assertTranslatesTo("!img-l name", "<img src=\"name\" class=\"left\"/>");
        ParserTestHelper.assertTranslatesTo("!img-r name", "<img src=\"name\" class=\"right\"/>");
    }

    @Test
    public void imageWithWidth() throws Exception {
        ParserTestHelper.assertParses("!img-w640 name", "SymbolList[Link[SymbolList[Text]]]");
        TestRoot root = new TestRoot();
        WikiPage testPage = root.makePage("ImagePage", "!img-w640 name");
        ParserTestHelper.assertTranslatesTo(testPage, "<img src=\"name\" width=\"640\"/>");
    }

    @Test
    public void imageWidthHandleMistyped() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage testPage = root.makePage("ImagePage", "!img-w name");
        ParserTestHelper.assertTranslatesTo(testPage, "!img-w name");
        testPage = root.makePage("ImagePage", "!img-wNNN name");
        ParserTestHelper.assertTranslatesTo(testPage, "<img src=\"name\" width=\"NNN\"/>");
        testPage = root.makePage("ImagePage", "!img-wN");
        ParserTestHelper.assertTranslatesTo(testPage, "!img-wN");
        testPage = root.makePage("ImagePage", "'''!img-wN'''");
        ParserTestHelper.assertTranslatesTo(testPage, "<b>!img-wN</b>");
    }
}
