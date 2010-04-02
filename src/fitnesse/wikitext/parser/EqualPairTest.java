package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class EqualPairTest {
    @Test public void scansTripleQuotes() {
        ParserTest.assertScansTokenType("'''bold'''", SymbolType.Bold, true);
        ParserTest.assertScansTokenType("''''bold''''", SymbolType.Bold, true);
        ParserTest.assertScansTokenType("'' 'not bold' ''", SymbolType.Bold, false);
        ParserTest.assertScansTokenType("''''some text' '''", SymbolType.Bold, true);
    }

    @Test public void translatesBold() {
        ParserTest.assertTranslatesTo("'''bold text'''", "<b>bold text</b>" + HtmlElement.endl);
    }

    @Test public void scansDoubleQuotes() {
        ParserTest.assertScansTokenType("''italic''", SymbolType.Italic, true);
        ParserTest.assertScansTokenType("'' 'italic' ''", SymbolType.Italic, true);
    }

    @Test public void translatesItalic() {
        ParserTest.assertTranslatesTo("''italic text''", "<i>italic text</i>" + HtmlElement.endl);
    }

    @Test public void translatesBoldItalic() {
        ParserTest.assertTranslatesTo("'''''stuff&nonsense'''''",
                "<b><i>stuff&amp;nonsense</i>" + HtmlElement.endl + "</b>" + HtmlElement.endl);
    }

    @Test public void ignoresAdjacentItalics() {
        ParserTest.assertTranslatesTo("''''", "''''");
    }

    @Test public void translatesItalicQuote() {
        ParserTest.assertTranslatesTo("'''''", "<i>'</i>" + HtmlElement.endl);
    }

    @Test public void scansDoubleDashes() {
        ParserTest.assertScansTokenType("abc--123--def", SymbolType.Strike, true);
        ParserTest.assertScansTokenType("--- -", SymbolType.Strike, true);
    }

    @Test public void translatesStrike() {
        ParserTest.assertTranslatesTo("--some text--", "<span class=\"strike\">some text</span>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("--embedded-dash--", "<span class=\"strike\">embedded-dash</span>" + HtmlElement.endl);
    }

    @Test public void testEvilExponentialMatch() throws Exception {
        long startTime = System.currentTimeMillis();

        ParserTest.assertTranslatesTo("--1234567890123456789012", "--1234567890123456789012");

        long endTime = System.currentTimeMillis();
        assertTrue("took too long", endTime - startTime < 20);
    }
}
