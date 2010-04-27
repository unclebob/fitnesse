package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class LastModifiedTest {
    @Test
    public void scansLastModified() {
        ParserTest.assertScansTokenType("!lastmodified", SymbolType.LastModified, true);
    }

    @Test public void translatesLastModified() throws Exception {
        WikiPage page = new TestRoot().makePage("PageOne", "!lastmodified");
        ParserTest.assertTranslatesTo(page, "<span class=\"meta\">Last modified anonymously on xxx</span>");
    }
}
