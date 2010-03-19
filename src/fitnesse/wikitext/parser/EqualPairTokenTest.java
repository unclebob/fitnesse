package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class EqualPairTokenTest {
    @Test public void scansTripleQuotes() {
        ParserTest.assertScans("'''bold'''", "Bold,Word=bold,Bold");
        ParserTest.assertScans("''''bold''''", "Bold,Text=',Word=bold,Bold,Text='");
        ParserTest.assertScans("'' 'not bold' ''", "Italic,Whitespace= ,Text=',Word=not,Whitespace= ,Word=bold,Text=',Whitespace= ,Italic");
        ParserTest.assertScans("''''some text' '''", "Bold,Text=',Word=some,Whitespace= ,Word=text,Text=',Whitespace= ,Bold");
    }

    @Test public void translatesBold() {
        ParserTest.assertTranslates("'''bold text'''", "<b>bold text</b>" + HtmlElement.endl);
    }

    @Test public void scansDoubleQuotes() {
        ParserTest.assertScans("''italic''", "Italic,Word=italic,Italic");
        ParserTest.assertScans("'' 'italic' ''", "Italic,Whitespace= ,Text=',Word=italic,Text=',Whitespace= ,Italic");
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
        ParserTest.assertScans("abc--123--def", "Word=abc,Strike,Word=123,Strike,Word=def");
        ParserTest.assertScans("--- -", "Strike,Text=-,Whitespace= ,Text=-");
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
