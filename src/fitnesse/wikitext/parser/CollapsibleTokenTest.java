package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class CollapsibleTokenTest {
    @Test public void scansCollapsible() {
        ParserTest.assertScans("!* Some title\n content \n*!",
                "Collapsible=collapsable,Whitespace= ,Text=Some,Whitespace= ,Text=title,Newline=\n,Whitespace= ,Text=content,Whitespace= ,Newline=\n,EndSection=*!");
    }

    @Test public void translatesCollapsible() {
        CollapsibleToken.resetId();
        ParserTest.assertTranslates("!* Some title\n content \n*!",
                sectionWithClass("collapsable"));

        CollapsibleToken.resetId();
        ParserTest.assertTranslates("!*> Some title\n content \n*!",
                sectionWithClass("hidden"));
        
        ParserTest.assertTranslates("!**\n**!", "!**<br/>" + HtmlElement.endl + "**!");
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
