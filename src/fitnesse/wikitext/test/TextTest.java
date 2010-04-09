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
    }
}
