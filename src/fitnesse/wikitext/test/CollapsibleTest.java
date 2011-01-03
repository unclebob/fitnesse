package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.Collapsible;
import org.junit.Test;

public class CollapsibleTest {
    @Test public void scansCollapsible() {
        ParserTestHelper.assertScansTokenType("!* title\ncontent\n*!", "Collapsible", true);
    }

    @Test public void parsesCollapsible() throws Exception {
        ParserTestHelper.assertParses("!* title\ncontent\n*!", "SymbolList[Collapsible[SymbolList[Text], SymbolList[Text, Newline]]]");
        ParserTestHelper.assertParses("!* title\n\n*!", "SymbolList[Collapsible[SymbolList[Text], SymbolList[Newline]]]");
        ParserTestHelper.assertParses("!**\n**!", "SymbolList[Text, Newline, CloseCollapsible]");
        ParserTestHelper.assertParses("!* title\n!path x\n**!", "SymbolList[Collapsible[SymbolList[Text], SymbolList[Path[SymbolList[Text]], Newline]]]");
    }

    @Test public void translatesCollapsible() {
        Collapsible.resetId();
        ParserTestHelper.assertTranslatesTo("!* Some title\n''content''\n*!",
          sectionWithClass("collapsable", "Open", "<i>content</i><br/>"));

        Collapsible.resetId();
        ParserTestHelper.assertTranslatesTo("!* Some title\n\n*!",
          sectionWithClass("collapsable", "Open", "<br/>"));

        Collapsible.resetId();
        ParserTestHelper.assertTranslatesTo("!*> Some title\n content \n*!",
          sectionWithClass("hidden", "Closed", " content <br/>"));

        Collapsible.resetId();
        ParserTestHelper.assertTranslatesTo("!*< Some title\n content \n*!",
          "<div class=\"invisible\"> content <br/></div>" + HtmlElement.endl);
    }

    private String sectionWithClass(String sectionClass, String image, String content) {
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
        "\t<div class=\"" + sectionClass + "\" id=\"1\">" + content + "</div>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl;
    }
}
