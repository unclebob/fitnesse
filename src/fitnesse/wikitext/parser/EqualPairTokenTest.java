package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class EqualPairTokenTest {
    @Test public void scansTripleQuotes() {
        ParserTest.assertScans("'''bold'''", "EqualPairToken=''',TextToken=bold,EqualPairToken='''");
        ParserTest.assertScans("''''bold''''", "EqualPairToken=''',TextToken='bold,EqualPairToken=''',TextToken='");
        ParserTest.assertScans("'' 'not bold' ''", "EqualPairToken='',TextToken= 'not bold' ,EqualPairToken=''");
        ParserTest.assertScans("''''some text' '''", "EqualPairToken=''',TextToken='some text' ,EqualPairToken='''");
    }

    @Test public void translatesBold() {
        ParserTest.assertTranslates("'''bold text'''", "<b>bold text</b>" + HtmlElement.endl);
    }

    @Test public void scansDoubleQuotes() {
        ParserTest.assertScans("''italic''", "EqualPairToken='',TextToken=italic,EqualPairToken=''");
        ParserTest.assertScans("'' 'italic' ''", "EqualPairToken='',TextToken= 'italic' ,EqualPairToken=''");
    }

    @Test public void translatesItalic() {
        ParserTest.assertTranslates("''italic text''", "<i>italic text</i>" + HtmlElement.endl);
    }

    @Test public void translatesBoldItalic() {
        ParserTest.assertTranslates("'''''stuff'''''", "<b><i>stuff</i>" + HtmlElement.endl + "</b>" + HtmlElement.endl);
    }

    @Test public void ignoresAdjacentItalics() {
        ParserTest.assertTranslates("''''", "''''");
    }

    @Test public void translatesItalicQuote() {
        ParserTest.assertTranslates("'''''", "<i>'</i>" + HtmlElement.endl);
    }

    @Test public void scansDoubleDashes() {
        ParserTest.assertScans("abc--123--def", "TextToken=abc,EqualPairToken=--,TextToken=123,EqualPairToken=--,TextToken=def");
        ParserTest.assertScans("--- -", "EqualPairToken=--,TextToken=- -");
    }

    @Test public void translatesStrike() {
        ParserTest.assertTranslates("--some text--", "<span class=\"strike\">some text</span>" + HtmlElement.endl);
        ParserTest.assertTranslates("--embedded-dash--", "<span class=\"strike\">embedded-dash</span>" + HtmlElement.endl);
    }

    @Test public void testEvilExponentialMatch() throws Exception {
        long startTime = System.currentTimeMillis();

        ParserTest.assertTranslates("--1234567890123456789012", "--1234567890123456789012");

        long endTime = System.currentTimeMillis();
        assertTrue("took too long", endTime - startTime < 20);
    }
}
