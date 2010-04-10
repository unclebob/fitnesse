package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.translator.CollapsibleBuilder;
import org.junit.Test;

public class CollapsibleTest {
    @Test public void scansCollapsible() {
        ParserTest.assertScansTokenType("!* title\ncontent\n*!", SymbolType.Collapsible, true);
    }

    @Test public void parsesCollapsible() {
        ParserTest.assertParses("!* title\ncontent\n*!", "SymbolList[Collapsible[Text, SymbolList[Text], SymbolList[Text]]]");
        ParserTest.assertParses("!**\n**!", "SymbolList[Text, Newline, CloseCollapsible]");
    }

    @Test public void translatesCollapsible() {
        CollapsibleBuilder.resetId();
        ParserTest.assertTranslatesTo("!* Some title\n content \n*!",
                sectionWithClass("collapsable", "Open"));

        CollapsibleBuilder.resetId();
        ParserTest.assertTranslatesTo("!*> Some title\n content \n*!",
                sectionWithClass("hidden", "Closed"));

        CollapsibleBuilder.resetId();
        ParserTest.assertTranslatesTo("!*< Some title\n content \n*!",
                "<div class=\"invisible\"> content </div>" + HtmlElement.endl);

        ParserTest.assertTranslatesTo("!**\n**!", "!**<br/>**!");
    }

    private String sectionWithClass(String sectionClass, String image) {
        return "<div class=\"collapse_rim\">" + HtmlElement.endl +
        "\t<div style=\"float: right;\" class=\"meta\">" + HtmlElement.endl +
        "\t\t<a href=\"javascript:expandAll();\">Expand All</a>" + HtmlElement.endl +
        " | " + HtmlElement.endl +
        "\t\t<a href=\"javascript:collapseAll();\">Collapse All</a>" + HtmlElement.endl +
        "\t</div>"  + HtmlElement.endl +
        "\t<a href=\"javascript:toggleCollapsable('1');\">" + HtmlElement.endl +
        "\t\t<img src=\"/files/images/collapsable" + image + ".gif\" class=\"left\" id=\"img1\"/>" + HtmlElement.endl +
        "\t</a>" + HtmlElement.endl +
        "&nbsp;" + HtmlElement.endl +
        "\t<span class=\"meta\">Some title</span>" + HtmlElement.endl +
        "\t<div class=\"" + sectionClass + "\" id=\"1\"> content </div>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl;
    }
}
