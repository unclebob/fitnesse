package fitnesse.wikitext.parser;

import org.junit.Test;

public class EqualPairTest {
    @Test public void scansTripleQuotes() {
        ParserTestHelper.assertScansTokenType("'''bold'''", "Bold", true);
        ParserTestHelper.assertScansTokenType("''''bold''''", "Bold", true);
        ParserTestHelper.assertScansTokenType("'' 'not bold' ''", "Bold", false);
        ParserTestHelper.assertScansTokenType("''''some text' '''", "Bold", true);
    }

    @Test public void translatesBold() {
        ParserTestHelper.assertTranslatesTo("'''bold text'''", "<b>bold text</b>");
    }

    @Test public void scansDoubleQuotes() {
        ParserTestHelper.assertScansTokenType("''italic''", "Italic", true);
        ParserTestHelper.assertScansTokenType("'' 'italic' ''", "Italic", true);
    }

    @Test public void translatesItalic() {
        ParserTestHelper.assertTranslatesTo("''italic text''", "<i>italic text</i>");
    }

    @Test public void translatesBoldItalic() {
        ParserTestHelper.assertTranslatesTo("'''''stuff&nonsense'''''",
          "<b><i>stuff&amp;nonsense</i></b>");
    }

    @Test public void ignoresAdjacentItalics() {
        ParserTestHelper.assertTranslatesTo("''", "''");
        ParserTestHelper.assertTranslatesTo("''''", "''''");
    }

    @Test public void translatesItalicQuote() {
        ParserTestHelper.assertTranslatesTo("'''''", "<i>'</i>");
    }

    @Test public void scansDoubleDashes() {
        ParserTestHelper.assertScansTokenType("abc--123--def", "Strike", true);
        ParserTestHelper.assertScansTokenType("--- -", "Strike", true);
    }

    @Test public void translatesStrike() {
        ParserTestHelper.assertTranslatesTo("--some text--", "<strike>some text</strike>");
        ParserTestHelper.assertTranslatesTo("--embedded-dash--", "<strike>embedded-dash</strike>");
    }
}
