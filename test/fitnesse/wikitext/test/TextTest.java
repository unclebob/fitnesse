package fitnesse.wikitext.test;

import org.junit.Test;

public class TextTest {
    @Test public void scansTextAsWords() {
        ParserTestHelper.assertScans("hi mom", "Text=hi,Whitespace= ,Text=mom");
    }

    @Test public void translatesText() {
        ParserTestHelper.assertTranslatesTo("hi mom", "hi mom");
        ParserTestHelper.assertTranslatesTo("Hi MOM", "Hi MOM");
        ParserTestHelper.assertTranslatesTo("Hi+Mom", "Hi+Mom");
        ParserTestHelper.assertTranslatesTo("A", "A");
        ParserTestHelper.assertTranslatesTo("Aa", "Aa");
        ParserTestHelper.assertTranslatesTo(".", ".");
        ParserTestHelper.assertTranslatesTo("<hi>", "&lt;hi&gt;");
        ParserTestHelper.assertTranslatesTo("text &bar; &bang; &dollar;", "text | ! $");
        ParserTestHelper.assertTranslatesTo("HiMOM02", "HiMOM02");
    }
}
