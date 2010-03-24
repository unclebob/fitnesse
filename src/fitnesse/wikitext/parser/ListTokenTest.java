package fitnesse.wikitext.parser;

import org.junit.Test;

public class ListTokenTest {
    @Test
    public void scansAnchors() {
        ParserTest.assertScansTokenType(" * item", TokenType.List, true);
        ParserTest.assertScansTokenType("  * item", TokenType.List, true);
        ParserTest.assertScansTokenType("* item", TokenType.List, false);
    }
}
