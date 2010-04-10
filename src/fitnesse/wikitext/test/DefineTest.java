package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.test.TestRoot;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DefineTest {
    @Test public void scansDefine() {
        ParserTest.assertScansTokenType("!define x {y}", SymbolType.Define, true);
    }

    @Test public void translatesDefines() throws Exception {
        assertTranslatesDefine("!define x {y}", "x=y");
        assertTranslatesDefine("!define x (y)", "x=y");
        assertTranslatesDefine("!define x [y]", "x=y");
        assertTranslatesDefine("!define x {''y''}", "x=''y''");
    }

    @Test public void definesValues() throws Exception {
        assertDefinesValue("!define x {y}", "x", "y");
        assertDefinesValue("!define x {''y''}", "x", "<i>y</i>");
        assertDefinesValue("!define x {!note y\n}", "x", "<span class=\"note\">y</span><br/>");
        assertDefinesValue("!define z {y}\n!define x {${z}}", "x", "y");
        assertDefinesValue("!define z {''y''}\n!define x {${z}}", "x", "<i>y</i>");
        assertDefinesValue("!define z {y}\n!define x {''${z}''}", "x", "<i>y</i>");
    }

    private void assertDefinesValue(String input, String name, String definedValue) throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne");
        PageData data = new PageData(pageOne, input);
        pageOne.commit(data);
        assertEquals(definedValue, pageOne.getData().getLocalVariable(name).getValue());
    }

    private void assertTranslatesDefine(String input, String definition) throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne");
        ParserTest.assertTranslatesTo(pageOne, input,
                "<span class=\"meta\">variable defined: " + definition + "</span>" + HtmlElement.endl);
    }

}
