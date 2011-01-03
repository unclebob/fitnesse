package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class EqualPairTest {
    @Test public void scansTripleQuotes() {
        ParserTest.assertScansTokenType("'''bold'''", "Bold", true);
        ParserTest.assertScansTokenType("''''bold''''", "Bold", true);
        ParserTest.assertScansTokenType("'' 'not bold' ''", "Bold", false);
        ParserTest.assertScansTokenType("''''some text' '''", "Bold", true);
    }

    @Test public void translatesBold() {
        ParserTest.assertTranslatesTo("'''bold text'''", "<b>bold text</b>");
    }

    @Test public void scansDoubleQuotes() {
        ParserTest.assertScansTokenType("''italic''", "Italic", true);
        ParserTest.assertScansTokenType("'' 'italic' ''", "Italic", true);
    }

    @Test public void translatesItalic() {
        ParserTest.assertTranslatesTo("''italic text''", "<i>italic text</i>");
    }

    @Test public void translatesBoldItalic() {
        ParserTest.assertTranslatesTo("'''''stuff&nonsense'''''",
                "<b><i>stuff&amp;nonsense</i></b>");
    }

    @Test public void ignoresAdjacentItalics() {
        ParserTest.assertTranslatesTo("''''", "''''");
    }

    @Test public void translatesItalicQuote() {
        ParserTest.assertTranslatesTo("'''''", "<i>'</i>");
    }

    @Test public void scansDoubleDashes() {
        ParserTest.assertScansTokenType("abc--123--def", "Strike", true);
        ParserTest.assertScansTokenType("--- -", "Strike", true);
    }

    @Test public void translatesStrike() {
        ParserTest.assertTranslatesTo("--some text--", "<span class=\"strike\">some text</span>");
        ParserTest.assertTranslatesTo("--embedded-dash--", "<span class=\"strike\">embedded-dash</span>");
    }

    @Test public void testEvilExponentialMatch() throws Exception {
        long startTime = System.currentTimeMillis();

        ParserTest.assertTranslatesTo("--1234567890123456789012", "--1234567890123456789012");

        long endTime = System.currentTimeMillis();
        assertTrue("took too long", endTime - startTime < 20);
    }
}
