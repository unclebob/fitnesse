package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.VariableFinder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefineTest {
    @Test public void scansDefine() {
        ParserTest.assertScansTokenType("!define x {y}", SymbolType.Define, true);
    }

    @Test public void translatesDefines() throws Exception {
        assertTranslatesDefine("!define x {y}", "x=y");
        assertTranslatesDefine("!define x_x {y}", "x_x=y");
        assertTranslatesDefine("!define x.x {y}", "x.x=y");
        assertTranslatesDefine("!define x (y)", "x=y");
        assertTranslatesDefine("!define x [y]", "x=y");
        assertTranslatesDefine("!define x {''y''}", "x=''y''");
    }

    @Test public void definesValues() throws Exception {
        assertDefinesValue("!define x {y}", "x", "y");
        //todo: move to variableTest?
        //assertDefinesValue("!define x {''y''}", "x", "<i>y</i>");
        //assertDefinesValue("!define x {!note y\n}", "x", "<span class=\"note\">y</span><br/>");
        //assertDefinesValue("!define z {y}\n!define x {${z}}", "x", "y");
        //assertDefinesValue("!define z {''y''}\n!define x {${z}}", "x", "<i>y</i>");
        //assertDefinesValue("!define z {y}\n!define x {''${z}''}", "x", "<i>y</i>");
    }

    private void assertDefinesValue(String input, String name, String definedValue) throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", input);
        ParsingPage page = new ParsingPage(pageOne);
        Parser.make(page, input).parse();
        assertEquals(definedValue, page.findVariable(name).getValue());
    }

    private void assertTranslatesDefine(String input, String definition) throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne");
        ParserTest.assertTranslatesTo(pageOne, input,
                "<span class=\"meta\">variable defined: " + definition + "</span>" + HtmlElement.endl);
    }

}
