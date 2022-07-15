package fitnesse.wikitext.parser;

import org.junit.Test;

public class NestingTest {
    @Test
    public void scansNesting() {
        ParserTestHelper.assertScansTokenType("!(nesting)!", "Nesting", true);
        ParserTestHelper.assertScansTokenType("!(nesting)!", "CloseNesting", true);
    }

    @Test
    public void nestsTableInTable() {
        ParserTestHelper.assertTranslatesTo("|!(\n|a|\n)!|", ParserTestHelper.tableWithCell(
                ParserTestHelper.newLineRendered + ParserTestHelper.tableWithCell("a").trim()));
        ParserTestHelper.assertTranslatesTo("|!(|a|\n)!|", ParserTestHelper.tableWithCell(ParserTestHelper.tableWithCell("a").trim()));
        ParserTestHelper.assertTranslatesTo("|!(|a|)!|", ParserTestHelper.tableWithCell(ParserTestHelper.tableWithCell("a").trim()));
    }

    @Test
    public void translatesIncomplete() {
        ParserTestHelper.assertTranslatesTo("!(''body''", "!(<i>body</i>");
    }
}
