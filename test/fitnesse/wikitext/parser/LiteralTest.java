package fitnesse.wikitext.parser;

import org.junit.Test;

public class LiteralTest {
    @Test
    public void scansLiteral() {
        ParserTestHelper.assertScansTokenType("!- stuff -!", "Literal", true);
    }

    @Test
    public void translatesLiteral() {
        ParserTestHelper.assertTranslatesTo("!-stuff-!", "stuff");
        ParserTestHelper.assertTranslatesTo("!-''not italic''-!", "''not italic''");
        ParserTestHelper.assertTranslatesTo("!-break\n-!|", "break\n|");
    }

    @Test
    public void parses() {
        ParserTestHelper.assertParsesWithOffset("!-stuff-!", "SymbolList<0..9>[Literal<0..9>]");
        ParserTestHelper.assertParsesWithOffset("foo!-stuff-!bar", "SymbolList<0..15>[Text<0..3>, Literal<3..12>, Text<12..15>]");
    }
}
