package fitnesse.wikitext.parser;

import org.junit.Test;

public class LiteralTokenTest {
    @Test public void scansLiteral() {
        ParserTest.assertScansTokenType("!- stuff -!", TokenType.OpenLiteral, true);
    }

    @Test public void translatesLiteral() {
        ParserTest.assertTranslates("!-stuff-!", "stuff");
        ParserTest.assertTranslates("!-''not italic''-!", "''not italic''");
    }
}
