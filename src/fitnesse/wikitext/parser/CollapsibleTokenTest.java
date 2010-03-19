package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class CollapsibleTokenTest {
    @Test public void scansCollapsible() {
        ParserTest.assertScans("!* Some title\n content \n*!",
                "Collapsible=collapsable,Whitespace= ,Word=Some,Whitespace= ,Word=title,Newline=\n,Whitespace= ,Word=content,Whitespace= ,Newline=\n,EndSection=*!");
    }

    @Test public void translatesCollapsible() {
        CollapsibleToken.resetId();
        ParserTest.assertTranslates("!* Some title\n content \n*!",
                "<div class=\"collapse_rim\">" + HtmlElement.endl +
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
                "\t<div class=\"collapsable\" id=\"1\"> content \n</div>" + HtmlElement.endl +
                "</div>" + HtmlElement.endl);

        ParserTest.assertTranslates("!*> Some title\n content \n*!",
                "<div class=\"collapse_rim\">" + HtmlElement.endl +
                "\t<div style=\"float: right;\" class=\"meta\">" + HtmlElement.endl +
                "\t\t<a href=\"javascript:expandAll();\">Expand All</a>" + HtmlElement.endl +
                " | " + HtmlElement.endl +
                "\t\t<a href=\"javascript:collapseAll();\">Collapse All</a>" + HtmlElement.endl +
                "\t</div>"  + HtmlElement.endl +
                "\t<a href=\"javascript:toggleCollapsable('2');\">" + HtmlElement.endl +
                "\t\t<img src=\"/files/images/collapsableOpen.gif\" class=\"left\" id=\"img2\"/>" + HtmlElement.endl +
                "\t</a>" + HtmlElement.endl +
                "&nbsp;" + HtmlElement.endl +
                "\t<span class=\"meta\">Some title</span>" + HtmlElement.endl +
                "\t<div class=\"hidden\" id=\"2\"> content \n</div>" + HtmlElement.endl +
                "</div>" + HtmlElement.endl);
        ParserTest.assertTranslates("!**\n**!", "!**\n**!");
    }
}
