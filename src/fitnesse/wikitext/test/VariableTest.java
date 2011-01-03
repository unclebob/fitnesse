package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class VariableTest {
    @Test public void scansVariables() {
        ParserTest.assertScansTokenType("${x}", "Variable", true);
    }

    @Test public void translatesVariables() throws Exception {
        ParserTest.assertTranslatesTo("${x}", new TestVariableSource("x", "y"), "y");
        ParserTest.assertTranslatesTo("${BoBo}", new TestVariableSource("BoBo", "y"), "y");
        assertTranslatesVariable("${x}", "y");
        assertTranslatesVariable("${z}", "<span class=\"meta\">undefined variable: z</span>");
    }

    private void assertTranslatesVariable(String variable, String expected) throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n" + variable);
        ParserTest.assertTranslatesTo(pageOne,
                "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
                ParserTest.newLineRendered + expected);
    }

    @Test public void translatesVariableContents() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {''y''}\n|${x}|\n");
        String result = ParserTest.translateTo(pageOne);
        assertTrue(result.indexOf("<i>y</i>") >= 0);
    }

    @Test public void translatesVariableContentsInLiteralTable() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {''y''}\n!|${x}|\n");
        String result = ParserTest.translateTo(pageOne);
        assertTrue(result.indexOf("<i>y</i>") < 0);
        assertTrue(result.indexOf("''y''", result.indexOf("table")) >= 0);
    }

    @Test public void evaluatesVariablesAtCurrentLocation() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n${x}\n!define x {z}\n${x}");
        ParserTest.assertTranslatesTo(pageOne,
                "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTest.newLineRendered
                + "y" + ParserTest.newLineRendered
                + "<span class=\"meta\">variable defined: x=z</span>" + HtmlElement.endl + ParserTest.newLineRendered
                + "z");
    }

    @Test public void evaluatesNestedVariableDefinition() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n!define z {${x}}\n${z}");
        ParserTest.assertTranslatesTo(pageOne,
                "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTest.newLineRendered
                + "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTest.newLineRendered
                + "y");
    }

    @Test public void evaluatesForwardNestedVariableDefinition() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define z {${x}}\n!define x {y}\n${z}");
        ParserTest.assertTranslatesTo(pageOne,
                "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTest.newLineRendered +
                "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTest.newLineRendered +
                "y");
    }

    @Test public void evaluatesEmptyNestedVariableDefinition() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {}\n!define z {${x}}\n${z}");
        ParserTest.assertTranslatesTo(pageOne,
                "<span class=\"meta\">variable defined: x=</span>" + HtmlElement.endl + ParserTest.newLineRendered +
                "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTest.newLineRendered);
    }

    @Test public void evaluatesVariablesFromParent() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {y}\n");
        WikiPage child = root.makePage(parent, "PageTwo");
        ParserTest.assertTranslatesTo(child, "${x}", "y");
    }

    @Test public void evaluatesVariablesFromParentInCurrentContext() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {${y}}\n");
        WikiPage child = root.makePage(parent, "PageTwo", "!define y {stuff}\n${x}");
        assertTrue(ParserTest.translateTo(child).endsWith("stuff"));
    }

    @Test public void evaluatesVariablesFromInclude() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage includer = root.makePage("PageOne", "!include -seamless PageTwo\n${x}");
        root.makePage("PageTwo", "!define x {y}");
        ParserTest.assertTranslatesTo(includer, "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
                ParserTest.newLineRendered + "y");
    }
}
