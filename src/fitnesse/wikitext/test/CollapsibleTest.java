package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.translator.CollapsibleBuilder;
import org.junit.Test;

public class CollapsibleTest {
    @Test public void scansCollapsible() {
        ParserTest.assertScansTokenType("!* title\ncontent\n*!", SymbolType.Collapsible, true);
    }

    @Test public void parsesCollapsible() {
        ParserTest.assertParses("!* title\ncontent\n*!", "SymbolList[Collapsible[Text, SymbolList[Text], SymbolList[Text, Newline]]]");
    }

    @Test public void translatesCollapsible() {
        CollapsibleBuilder.resetId();
        ParserTest.assertTranslatesTo("!* Some title\n content \n*!",
                sectionWithClass("collapsable"));

        CollapsibleBuilder.resetId();
        ParserTest.assertTranslatesTo("!*> Some title\n content \n*!",
                sectionWithClass("hidden"));
        
        ParserTest.assertTranslatesTo("!**\n**!", "!**<br/>" + HtmlElement.endl + "**!");
    }

    private String sectionWithClass(String sectionClass) {
        return "<div class=\"collapse_rim\">" + HtmlElement.endl +
        "\t<div style=\"float: right;\" class=\"meta\">" + HtmlElement.endl +
        "\t\t<a href=\"javascript:expandAll();\">Expand All</a>" + HtmlElement.endl +
        " | " + HtmlElement.endl +
        "\t\t<a href=\"javascript:collapseAll();\">Collapse All</a>" + HtmlElement.endl +
        "\t</div>"  + HtmlElement.endl +
        "\t<a href=\"javascript:toggleCollapsable('1');\">" + HtmlElement.endl +
        "\t\t<img src=\"/files/images/collapsableOpen.gif\" class=\"left\" id=\"img1\"/>" + HtmlElement.endl +
        "\t</a>" + HtmlElement.endl +
        "&nbsp;" + HtmlElement.endl +
        "\t<span class=\"meta\">Some title</span>" + HtmlElement.endl +
        "\t<div class=\"" + sectionClass + "\" id=\"1\"> content <br/>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl;
    }
}
