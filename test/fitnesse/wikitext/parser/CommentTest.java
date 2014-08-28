package fitnesse.wikitext.parser;

import org.junit.Test;

public class CommentTest {
    @Test public void scansComments() {
        ParserTestHelper.assertScansTokenType("# comment\n", "Comment", true);
        ParserTestHelper.assertScansTokenType(" # comment\n", "Comment", false);
        ParserTestHelper.assertScansTokenType("| # comment|\n", "Comment", false);
        ParserTestHelper.assertScansTokenType("!c # comment|\n", "Comment", false);
    }

    @Test public void parsesComments() throws Exception {
        ParserTestHelper.assertParses("# comment\n", "SymbolList[Comment[Text]]");
        ParserTestHelper.assertParses("# comment", "SymbolList[Comment[Text]]");
    }

    @Test public void translatesComments() {
        ParserTestHelper.assertTranslatesTo("# comment\n", "");
        ParserTestHelper.assertTranslatesTo("# comment", "");
    }
}
