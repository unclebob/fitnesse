package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class PathTest {
    @Test
    public void scansPaths() {
        ParserTest.assertScansTokenType("!path stuff", SymbolType.Path, true);
    }

    @Test
    public void translatesPaths() throws Exception {
        ParserTest.assertTranslatesTo("!path stuff", "<span class=\"meta\">classpath: stuff</span>");
        ParserTest.assertTranslatesTo("!path stuff\n",
                "<span class=\"meta\">classpath: stuff</span><br/>" + HtmlElement.endl);
    }

}
