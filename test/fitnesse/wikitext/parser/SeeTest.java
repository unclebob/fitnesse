package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class SeeTest {
    @Test
    public void scansSees() {
        ParserTestHelper.assertScansTokenType("!see Stuff", "See", true);
        ParserTestHelper.assertScansTokenType("!seeStuff", "See", false);
        ParserTestHelper.assertScansTokenType(" !see Stuff", "See", false);
        ParserTestHelper.assertScansTokenType("| !see Stuff|", "See", true);
        ParserTestHelper.assertScansTokenType("!note !see Stuff", "See", true);
    }

    @Test
	public void parsesSees() throws Exception {
        ParserTestHelper.assertParses("!see SomeStuff", "SymbolList[See[WikiWord]]");
        ParserTestHelper.assertParses("!see ya", "SymbolList[Text, Whitespace, Text]");
    }

    @Test
	public void translatesSees() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne", "!see PageTwo");
        root.makePage("PageTwo", "hi");
        ParserTestHelper.assertTranslatesTo(page, "<b>See: <a href=\"PageTwo\">PageTwo</a></b>");
    }
	
	@Test
	public void handlesAlias() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne", "!see [[page 2][PageTwo]]");
        root.makePage("PageTwo", "hi");
        ParserTestHelper.assertTranslatesTo(page, "<b>See: <a href=\"PageTwo\">page 2</a></b>");
	}

  @Test
  public void handlesMalformedAlias() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage page = root.makePage("PageOne", "!see [[looks like alias but is not");
        root.makePage("PageTwo", "hi");
        ParserTestHelper.assertTranslatesTo(page, "!see [[looks like alias but is not");
  }
}
