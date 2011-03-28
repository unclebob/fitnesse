package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class VariableTest {
    @Test public void scansVariables() {
        ParserTestHelper.assertScansTokenType("${x}", "Variable", true);
    }

    @Test public void translatesVariables() throws Exception {
        ParserTestHelper.assertTranslatesTo("${x}", new TestVariableSource("x", "y"), "y");
        ParserTestHelper.assertTranslatesTo("${BoBo}", new TestVariableSource("BoBo", "y"), "y");
        assertTranslatesVariable("${x}", "y");
        assertTranslatesVariable("${z}", "<span class=\"meta\">undefined variable: z</span>");
        assertTranslatesVariable("${}", "${}");
    }

    private void assertTranslatesVariable(String variable, String expected) throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n" + variable);
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
            ParserTestHelper.newLineRendered + expected);
    }

    @Test public void translatesVariableContents() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {''y''}\n|${x}|\n");
        String result = ParserTestHelper.translateTo(pageOne);
        assertTrue(result.indexOf("<i>y</i>") >= 0);
    }

    @Test public void translatesVariableContentsInLiteralTable() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {''y''}\n!|${x}|\n");
        String result = ParserTestHelper.translateTo(pageOne);
        assertTrue(result.indexOf("<i>y</i>") < 0);
        assertTrue(result.indexOf("''y''", result.indexOf("table")) >= 0);
    }

    @Test public void evaluatesVariablesAtCurrentLocation() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n${x}\n!define x {z}\n${x}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "y" + ParserTestHelper.newLineRendered
            + "<span class=\"meta\">variable defined: x=z</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "z");
    }

    @Test public void evaluatesNestedVariableDefinition() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n!define z {${x}}\n${z}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "y");
    }

    @Test public void evaluatesForwardNestedVariableDefinition() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define z {${x}}\n!define x {y}\n${z}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered +
            "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered +
            "y");
    }

    @Test public void evaluatesEmptyNestedVariableDefinition() throws Exception {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {}\n!define z {${x}}\n${z}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered +
            "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered);
    }

    @Test public void evaluatesVariablesFromParent() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {y}\n");
        WikiPage child = root.makePage(parent, "PageTwo");
        ParserTestHelper.assertTranslatesTo(child, "${x}", "y");
    }

    @Test public void evaluatesVariablesFromParentInCurrentContext() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {${y}}\n");
        WikiPage child = root.makePage(parent, "PageTwo", "!define y {stuff}\n${x}");
        assertTrue(ParserTestHelper.translateTo(child).endsWith("stuff"));
    }

    @Test public void evaluatesVariablesFromInclude() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage includer = root.makePage("PageOne", "!include -seamless PageTwo\n${x}");
        root.makePage("PageTwo", "!define x {y}");
        ParserTestHelper.assertTranslatesTo(includer, "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
          ParserTestHelper.newLineRendered + "y");
    }
}
