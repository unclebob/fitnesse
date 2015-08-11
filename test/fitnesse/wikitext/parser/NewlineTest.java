package fitnesse.wikitext.parser;

import org.junit.Test;

public class NewlineTest {
    @Test
    public void parsesNewLine() throws Exception {
        ParserTestHelper.assertParses("\n", "SymbolList[Newline]");
        ParserTestHelper.assertParses("\r\n", "SymbolList[Newline]");
    }

    @Test public void translatesWindowsStyleNewlines() {
        ParserTestHelper.assertTranslatesTo("hi\r\nmom", "hi" + ParserTestHelper.newLineRendered + "mom");
    }

    @Test public void translatesNewlines() {
        ParserTestHelper.assertTranslatesTo("hi\nmom", "hi" + ParserTestHelper.newLineRendered + "mom");
    }
}
