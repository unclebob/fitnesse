package fitnesse.wikitext.parser;

import org.junit.Test;

public class ImageTest {
    @Test
    public void scansImages() {
        ParserTestHelper.assertScansTokenType("!img name", "Image", true);
    }

    @Test
    public void parsesImages() {
        ParserTestHelper.assertParses("!img name", "SymbolList[Image[Link[SymbolList[Text]]]]");
        ParserTestHelper.assertParses("!img http://name", "SymbolList[Image[Link[SymbolList[Text]]]]");
        ParserTestHelper.assertParses("!imgx name", "SymbolList[Text, Whitespace, Text]");
        ParserTestHelper.assertParses("!img-l name", "SymbolList[Image[Link[SymbolList[Text]]]]");
        ParserTestHelper.assertParses("!img-r name", "SymbolList[Image[Link[SymbolList[Text]]]]");
        ParserTestHelper.assertParses("!img SomeName", "SymbolList[Image[Link[SymbolList[Text]]]]");
    }

    @Test
    public void translatesImages() {
        ParserTestHelper.assertTranslatesTo("!img name", "<img src=\"name\"/>");
        ParserTestHelper.assertTranslatesTo("!img http://name", "<img src=\"http://name\"/>");
        ParserTestHelper.assertTranslatesTo("!img-l name", "<img src=\"name\" class=\"left\"/>");
        ParserTestHelper.assertTranslatesTo("!img-r name", "<img src=\"name\" class=\"right\"/>");
        ParserTestHelper.assertTranslatesTo("!img SomeName", "<img src=\"SomeName\"/>");
    }

    @Test
    public void imageWithWidth() {
        ParserTestHelper.assertTranslatesTo("!img -w 640 name", "<img src=\"name\" width=\"640\"/>");
    }

    @Test
    public void imageWidthHandleMistyped() {
        ParserTestHelper.assertTranslatesTo("!img -w name", "!img -w name");
        ParserTestHelper.assertTranslatesTo("!img -w nnn name", "<img src=\"name\" width=\"nnn\"/>");
        ParserTestHelper.assertTranslatesTo("!img -w N", "!img -w N");
        ParserTestHelper.assertTranslatesTo("'''!img -w N'''", "<b>!img -w N</b>");
    }

    @Test
    public void imageWithMargin() {
        ParserTestHelper.assertTranslatesTo("!img -m 10 name", "<img src=\"name\" style=\"margin:10px;\"/>");
    }

    @Test
    public void imageWithBorder() {
        ParserTestHelper.assertTranslatesTo("!img -b 1 name", "<img src=\"name\" style=\"border:1px solid black;\"/>");
    }

    @Test
    public void imageWithMarginBorderWidth() {
        ParserTestHelper.assertTranslatesTo("!img -m 5 -b 1 -w 50 name", "<img src=\"name\" width=\"50\" style=\"border:1px solid black;margin:5px;\"/>");
    }
/*  @Test public void ImageIsPreserved() {
    assertRoundTrip("!img-l -m 5 -w 10 -b 2 PrettyNice");
    assertRoundTrip("!img-l -m 5 -w 10 -b 2 prettynice");
  }
*/

    @Test
    public void parseBase64() {
        ParserTestHelper.assertParses("!img data:mediatype;base64,somedata", "SymbolList[Image[Link[SymbolList[Text]]]]");
    }

    @Test
    public void parseBase64Wikiword() {
        ParserTestHelper.assertParses("!img data:mediatype;base64,somedataQm2Ic50", "SymbolList[Image[Link[SymbolList[Text]]]]");
    }

    @Test
    public void parseBase64Delta() {
        ParserTestHelper.assertParses("!img data:mediatype;base64,somedata+1m-3k", "SymbolList[Image[Link[SymbolList[Text]]]]");
    }

    @Test
    public void parseBase64InvalidPrefix() {
        ParserTestHelper.assertParses("!img xyz:mediatype;base64,somedata", "SymbolList[Image[Link[SymbolList[Text]]], Colon, Text, Comma, Text]");
    }

    @Test
    public void parseBase64InvalidFormat() {
        ParserTestHelper.assertParses("!img data,mediatype;base64:somedata", "SymbolList[Image[Link[SymbolList[Text]]], Comma, Text, Colon, Text]");
    }

    @Test
    public void parseBase64ToWhitespace() {
        ParserTestHelper.assertParses("!img data:mediatype;base64,somedata otherdata", "SymbolList[Image[Link[SymbolList[Text]]], Whitespace, Text]");
    }

    @Test
    public void parseBase64ToOtherSymbol() {
        ParserTestHelper.assertParses("!img data:mediatype;base64,somedata}}}", "SymbolList[Image[Link[SymbolList[Text]]], ClosePreformat]");
    }

    @Test
    public void translateBase64() {
        ParserTestHelper.assertTranslatesTo("!img data:mediatype;base64,somedata", "<img src=\"data:mediatype;base64,somedata\"/>");
    }

    @Test
    public void translateBase64Wikiword() {
        ParserTestHelper.assertTranslatesTo("!img data:mediatype;base64,somedataQm2Ic50", "<img src=\"data:mediatype;base64,somedataQm2Ic50\"/>");
    }

    @Test
    public void translateBase64Delta() {
        ParserTestHelper.assertTranslatesTo("!img data:mediatype;base64,somedata+1m-3k", "<img src=\"data:mediatype;base64,somedata+1m-3k\"/>");
    }

    @Test
    public void translateBase64ButInvalidPrefix() {
        ParserTestHelper.assertTranslatesTo("!img xyz:mediatype;base64,somedata", "<img src=\"xyz\"/>:mediatype;base64,somedata");
    }

    @Test
    public void translateBase64InvalidFormat() {
        ParserTestHelper.assertTranslatesTo("!img data,mediatype;base64:somedata", "<img src=\"data\"/>,mediatype;base64:somedata");
    }

    @Test
    public void translateBase64ToWhitespace() {
        ParserTestHelper.assertTranslatesTo("!img data:mediatype;base64,somedata otherdata", "<img src=\"data:mediatype;base64,somedata\"/> otherdata");
    }

    @Test
    public void translateBase64ToOtherSymbol() {
        ParserTestHelper.assertTranslatesTo("!img data:mediatype;base64,somedata}}}", "<img src=\"data:mediatype;base64,somedata\"/>}}}");
    }
}
