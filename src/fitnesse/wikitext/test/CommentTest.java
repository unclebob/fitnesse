package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class CommentTest {
    @Test public void scansComments() {
        ParserTest.assertScansTokenType("# comment\n", SymbolType.Comment, true);
        ParserTest.assertScansTokenType(" # comment\n", SymbolType.Comment, false);
    }

    @Test public void parsesComments() throws Exception {
        ParserTest.assertParses("# comment\n", "SymbolList[Comment]");
        ParserTest.assertParses("# comment", "SymbolList[Comment]");
    }

    @Test public void translatesComments() {
        ParserTest.assertTranslatesTo("# comment\n", "");
        ParserTest.assertTranslatesTo("# comment", "");
    }
}
