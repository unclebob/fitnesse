package fitnesse.wikitext.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class VariableTest {
    @Test public void scansVariables() {
        ParserTestHelper.assertScansTokenType("${x}", "Variable", true);
    }

    @Test public void translatesVariables() {
        ParserTestHelper.assertTranslatesTo("${x}", new TestVariableSource("x", "y"), "y");
        ParserTestHelper.assertTranslatesTo("${BoBo}", new TestVariableSource("BoBo", "y"), "y");
        assertTranslatesVariable("${x}", "y");
        assertTranslatesVariable("${z}", "<span class=\"meta\">undefined variable: z</span>");
      assertTranslatesVariable("${}", "${}");
      assertTranslatesVariable("${x" /* eof */, "y");
    }

    private void assertTranslatesVariable(String variable, String expected) {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n" + variable);
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
            ParserTestHelper.newLineRendered + expected);
    }

    @Test public void translatesVariableContents() {
      assertTrue(ParserTestHelper.translateTo("!define x {''y''}\n|${x}|\n").contains("<i>y</i>"));
      assertTrue(ParserTestHelper.translateTo("!define x {b}\n!define y (a${x}c)\n${y}\n").contains("abc"));
    }

    @Test public void translatesVariableContentsInLiteralTable() {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {''y''}\n!|${x}|\n");
        String result = ParserTestHelper.translateTo(pageOne);
        assertFalse(result.contains("<i>y</i>"));
        assertTrue(result.indexOf("''y''", result.indexOf("table")) >= 0);
    }

    @Test public void evaluatesVariablesAtCurrentLocation() {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n${x}\n!define x {z}\n${x}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "y" + ParserTestHelper.newLineRendered
            + "<span class=\"meta\">variable defined: x=z</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "z");
    }

    @Test public void evaluatesNestedVariableDefinition() {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n!define z {${x}}\n${z}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered
            + "y");
    }

    @Test public void evaluatesForwardNestedVariableDefinition() {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define z {${x}}\n!define x {y}\n${z}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered +
            "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered +
            "y");
    }

    @Test public void evaluatesEmptyNestedVariableDefinition() {
        WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {}\n!define z {${x}}\n${z}");
        ParserTestHelper.assertTranslatesTo(pageOne,
          "<span class=\"meta\">variable defined: x=</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered +
            "<span class=\"meta\">variable defined: z=${x}</span>" + HtmlElement.endl + ParserTestHelper.newLineRendered);
    }

    @Test public void evaluatesVariablesFromParent() {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {y}\n");
        WikiPage child = root.makePage(parent, "PageTwo");
        ParserTestHelper.assertTranslatesTo(child, "${x}", "y");
    }

    @Test public void evaluatesVariablesFromParentInCurrentContext() {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne", "!define x {${y}}\n");
        WikiPage child = root.makePage(parent, "PageTwo");
        assertTrue(ParserTestHelper.translateTo(child, "!define y {stuff}\n${x}").endsWith("stuff"));
    }

    @Test public void evaluatesVariablesFromInclude() {
        TestRoot root = new TestRoot();
        WikiPage includer = root.makePage("PageOne", "!include -seamless PageTwo\n${x}");
        root.makePage("PageTwo", "!define x {y}");
        ParserTestHelper.assertTranslatesTo(includer, "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
          "y");
    }
}
