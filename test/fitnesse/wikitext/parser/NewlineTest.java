package fitnesse.wikitext.parser;

import org.junit.Test;

import static fitnesse.wikitext.parser.ParserTestHelper.translateTo;
import static org.junit.Assert.assertEquals;

public class NewlineTest {
    @Test
    public void parsesNewLine() {
        ParserTestHelper.assertParses("\n", "SymbolList[Newline]");
        ParserTestHelper.assertParses("\r\n", "SymbolList[Newline]");
    }

    @Test public void translatesWindowsStyleNewlines() {
        ParserTestHelper.assertTranslatesTo("hi\r\nmom", "hi" + ParserTestHelper.newLineRendered + "mom");
    }

    @Test public void translatesNewlines() {
        ParserTestHelper.assertTranslatesTo("hi\nmom", "hi" + ParserTestHelper.newLineRendered + "mom");
    }

    @Test public void translatesTables() {
      assertEquals(translateTo("|a|\n|b|"), translateTo("|a|\r\n|b|"));
    }
}
