package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.translator.Paths;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.List;

public class PathTest {
    @Test
    public void scansPaths() {
        ParserTest.assertScansTokenType("!path stuff", SymbolType.Path, true);
    }

    @Test
    public void translatesPaths() throws Exception {
        ParserTest.assertTranslatesTo("!path stuff", "<span class=\"meta\">classpath: stuff</span>");
        ParserTest.assertTranslatesTo("!path stuff\n",
                "<span class=\"meta\">classpath: stuff</span><br/>");
    }

    @Test
    public void translatesVariableInPath() throws Exception {
        WikiPage page = new TestRoot().makePage("TestPage", "!define x {stuff}\n!path ${x}y\n");
        ParserTest.assertTranslatesTo(page, 
                "<span class=\"meta\">variable defined: x=stuff</span>" + HtmlElement.endl +
                        "<br/><span class=\"meta\">classpath: stuffy</span><br/>");
    }

    @Test
    public void findsDefinitions() throws Exception {
        WikiPage page = new TestRoot().makePage("TestPage", "!path stuff\n!note and\n!path nonsense");
        List<String> paths = new Paths(page, ParserTest.parse(page)).getPaths();
        assertEquals(2, paths.size());
        assertEquals("stuff", paths.get(0));
        assertEquals("nonsense", paths.get(1));
    }
}
