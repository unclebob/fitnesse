package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class SeeTest {
    @Test
    public void scansSees() {
        ParserTest.assertScansTokenType("!see Stuff", SymbolType.See, true);
        ParserTest.assertScansTokenType("!seeStuff", SymbolType.See, false);
    }

    @Test public void parsesSees() throws Exception {
        ParserTest.assertParses("!see SomeStuff", "SymbolList[See[WikiWord]]");
        ParserTest.assertParses("!see ya", "SymbolList[Text, Whitespace, Text]");
    }

    @Test public void translatesSees() throws Exception{
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne", "!see PageTwo");
        root.makePage("PageTwo", "hi");
        ParserTest.assertTranslatesTo(page, "<b>See: <a href=\"PageTwo\">PageTwo</a></b>");
    }
}
