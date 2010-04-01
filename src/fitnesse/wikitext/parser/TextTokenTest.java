package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class TextTokenTest {
    @Test public void scansTextAsWords() {
        ParserTest.assertScans("hi mom", "Text=hi,Whitespace= ,Text=mom");
    }

    @Test public void translatesText() {
        ParserTest.assertTranslates("hi mom", "hi mom");
        ParserTest.assertTranslates("Hi MOM", "Hi MOM");
        ParserTest.assertTranslates("Hi+Mom", "Hi+Mom");
        ParserTest.assertTranslates("A", "A");
        ParserTest.assertTranslates("Aa", "Aa");
        ParserTest.assertTranslates(".", ".");
        ParserTest.assertTranslates("<hi>", "&lt;hi&gt;");
        ParserTest.assertTranslates("\rmore\rstuff\r", "morestuff");
    }

    @Test public void translatesWikiWords() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage pageOne = root.makePage("PageOne");
        WikiPage pageOneTwo = root.makePage(pageOne, "PageTwo");
        WikiPage pageOneTwoThree = root.makePage(pageOneTwo, "PageThree");
        WikiPage pageOneThree = root.makePage(pageOne, "PageThree");

        ParserTest.assertTranslates(pageOne, "PageOne", wikiLink("PageOne", "PageOne"));
        ParserTest.assertTranslates(pageOneTwo, "PageTwo", wikiLink("PageOne.PageTwo", "PageTwo"));

        ParserTest.assertTranslates(pageOneThree, ".PageOne", wikiLink("PageOne", ".PageOne"));

        ParserTest.assertTranslates(pageOne, ">PageTwo", wikiLink("PageOne.PageTwo", "&gt;PageTwo"));

        ParserTest.assertTranslates(pageOneTwoThree, "<PageOne", wikiLink("PageOne", "&lt;PageOne"));
    }

    private String wikiLink(String link, String text) {
        return "<a href=\"" + link + "\">" + text + "</a>" + HtmlElement.endl;
    }
}
