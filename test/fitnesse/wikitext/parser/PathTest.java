package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.List;

public class PathTest {
    @Test
    public void scansPaths() {
        ParserTestHelper.assertScansTokenType("!path stuff", "Path", true);
        ParserTestHelper.assertScansTokenType(" !path stuff", "Path", false);
        ParserTestHelper.assertScansTokenType("| !path stuff|", "Path", true);
        ParserTestHelper.assertScansTokenType("!note !path stuff", "Path", true);
    }

    @Test
    public void translatesPaths() throws Exception {
        ParserTestHelper.assertTranslatesTo("!path stuff", "<span class=\"meta\">classpath: stuff</span>");
        ParserTestHelper.assertTranslatesTo("!path stuff\n",
          "<span class=\"meta\">classpath: stuff</span>" + ParserTestHelper.newLineRendered);
    }

    @Test
    public void translatesVariableInPath() throws Exception {
        WikiPage page = new TestRoot().makePage("TestPage", "!define x {stuff}\n!path ${x}y\n");
        ParserTestHelper.assertTranslatesTo(page,
          "<span class=\"meta\">variable defined: x=stuff</span>" + HtmlElement.endl +
            ParserTestHelper.newLineRendered + "<span class=\"meta\">classpath: stuffy</span>" + ParserTestHelper.newLineRendered);
    }

}
