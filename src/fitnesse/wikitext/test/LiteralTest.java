package fitnesse.wikitext.test;

import org.junit.Test;

public class LiteralTest {
    @Test public void scansLiteral() {
        ParserTestHelper.assertScansTokenType("!- stuff -!", "Literal", true);
    }

    @Test public void translatesLiteral() {
        ParserTestHelper.assertTranslatesTo("!-stuff-!", "stuff");
        ParserTestHelper.assertTranslatesTo("!-''not italic''-!", "''not italic''");
        ParserTestHelper.assertTranslatesTo("!-break\n-!|", "break\n|");
    }
}
