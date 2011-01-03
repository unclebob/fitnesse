package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class SeeTest {
    @Test
    public void scansSees() {
        ParserTestHelper.assertScansTokenType("!see Stuff", "See", true);
        ParserTestHelper.assertScansTokenType("!seeStuff", "See", false);
    }

    @Test public void parsesSees() throws Exception {
        ParserTestHelper.assertParses("!see SomeStuff", "SymbolList[See[WikiWord]]");
        ParserTestHelper.assertParses("!see ya", "SymbolList[Text, Whitespace, Text]");
    }

    @Test public void translatesSees() throws Exception{
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne", "!see PageTwo");
        root.makePage("PageTwo", "hi");
        ParserTestHelper.assertTranslatesTo(page, "<b>See: <a href=\"PageTwo\">PageTwo</a></b>");
    }
}
