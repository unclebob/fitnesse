package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class CollapsibleTest {
    @Test public void scansCollapsible() {
        ParserTestHelper.assertScansTokenType("!* title\ncontent\n*!", "Collapsible", true);
    }

    @Test public void parsesCollapsible() {
        ParserTestHelper.assertParses("!* title\ncontent\n*!", "SymbolList[Collapsible[SymbolList[Text], SymbolList[Text, Newline]]]");
        ParserTestHelper.assertParses("!* title\n\n*!", "SymbolList[Collapsible[SymbolList[Text], SymbolList[Newline]]]");
        ParserTestHelper.assertParses("!**\n**!", "SymbolList[Text, Newline, CloseCollapsible]");
        ParserTestHelper.assertParses("!* title\n!path x\n**!", "SymbolList[Collapsible[SymbolList[Text], SymbolList[Path[SymbolList[Text]], Newline]]]");
    }

    @Test public void translatesCollapsible() {
        ParserTestHelper.assertTranslatesTo("!* Some title\n''content''\n*!",
          sectionWithClass("collapsible", "<i>content</i><br/>"));

        ParserTestHelper.assertTranslatesTo("!* Some title\n\n*!",
          sectionWithClass("collapsible", "<br/>"));

        ParserTestHelper.assertTranslatesTo("!*> Some title\n content \n*!",
          sectionWithClass("collapsible closed", " content <br/>"));

        ParserTestHelper.assertTranslatesTo("!*< Some title\n content \n*!",
          sectionWithClass("collapsible invisible", " content <br/>"));
    }

    @Test public void translatesIncomplete() {
      ParserTestHelper.assertTranslatesTo("!* '''title'''\n''body''\n", "!* <b>title</b><br/><i>body</i><br/>");
    }

    private String sectionWithClass(String sectionClasses, String content) {
        return "<div class=\"" + sectionClasses + "\">" +
        "<ul>" +
        "<li><a href='#' class='expandall'>Expand</a></li>" +
        "<li><a href='#' class='collapseall'>Collapse</a></li>" +
        "</ul>"  + HtmlElement.endl +
        "\t<p class=\"title\">Some title</p>" + HtmlElement.endl +
        "\t<div>" + content + "</div>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl;
    }
}
