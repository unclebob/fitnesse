package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class CommentTest {
    @Test public void scansComments() {
        ParserTest.assertScansTokenType("# comment\n", "Comment", true);
        ParserTest.assertScansTokenType(" # comment\n", "Comment", false);
    }

    @Test public void parsesComments() throws Exception {
        ParserTest.assertParses("# comment\n", "SymbolList[Comment[Text]]");
        ParserTest.assertParses("# comment", "SymbolList[Comment[Text]]");
    }

    @Test public void translatesComments() {
        ParserTest.assertTranslatesTo("# comment\n", "");
        ParserTest.assertTranslatesTo("# comment", "");
    }
}
