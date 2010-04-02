package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.test.TestRoot;
import org.junit.Test;

public class TextTest {
    @Test public void scansTextAsWords() {
        ParserTest.assertScans("hi mom", "Text=hi,Whitespace= ,Text=mom");
    }

    @Test public void translatesText() {
        ParserTest.assertTranslatesTo("hi mom", "hi mom");
        ParserTest.assertTranslatesTo("Hi MOM", "Hi MOM");
        ParserTest.assertTranslatesTo("Hi+Mom", "Hi+Mom");
        ParserTest.assertTranslatesTo("A", "A");
        ParserTest.assertTranslatesTo("Aa", "Aa");
        ParserTest.assertTranslatesTo(".", ".");
        ParserTest.assertTranslatesTo("<hi>", "&lt;hi&gt;");
        ParserTest.assertTranslatesTo("\rmore\rstuff\r", "morestuff");
    }

    @Test public void translatesWikiWords() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage pageOne = root.makePage("PageOne");
        WikiPage pageOneTwo = root.makePage(pageOne, "PageTwo");
        WikiPage pageOneTwoThree = root.makePage(pageOneTwo, "PageThree");
        WikiPage pageOneThree = root.makePage(pageOne, "PageThree");

        ParserTest.assertTranslatesTo(pageOne, "PageOne", wikiLink("PageOne", "PageOne"));
        ParserTest.assertTranslatesTo(pageOneTwo, "PageTwo", wikiLink("PageOne.PageTwo", "PageTwo"));

        ParserTest.assertTranslatesTo(pageOneThree, ".PageOne", wikiLink("PageOne", ".PageOne"));

        ParserTest.assertTranslatesTo(pageOne, ">PageTwo", wikiLink("PageOne.PageTwo", "&gt;PageTwo"));

        ParserTest.assertTranslatesTo(pageOneTwoThree, "<PageOne", wikiLink("PageOne", "&lt;PageOne"));
    }

    private String wikiLink(String link, String text) {
        return "<a href=\"" + link + "\">" + text + "</a>" + HtmlElement.endl;
    }
}
