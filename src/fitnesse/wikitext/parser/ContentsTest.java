package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class ContentsTest {
    @Test public void scansContents() {
        ParserTest.assertScansTokenType("!contents", SymbolType.Contents, true);
    }

    @Test public void translatesContents() throws Exception {
         TestRoot root = new TestRoot();
         WikiPage pageOne = root.makePage("PageOne");
         root.makePage(pageOne, "PageTwo");
         root.makePage(pageOne, "PageThree");

         ParserTest.assertTranslatesTo(pageOne, "!contents",
                "<div class=\"toc1\">" + HtmlElement.endl +
                "\t<div class=\"contents\">" + HtmlElement.endl +
                "\t\t<b>Contents:</b>" + HtmlElement.endl +
                "\t\t<ul>" + HtmlElement.endl +
                "\t\t\t<li>" + HtmlElement.endl +
                "\t\t\t\t<a href=\"PageOne.PageThree\">PageThree</a>" + HtmlElement.endl +
                "\t\t\t</li>" + HtmlElement.endl +
                "\t\t\t<li>" + HtmlElement.endl +
                "\t\t\t\t<a href=\"PageOne.PageTwo\">PageTwo</a>" + HtmlElement.endl +
                "\t\t\t</li>" + HtmlElement.endl +
                "\t\t</ul>" + HtmlElement.endl +
                "\t</div>" + HtmlElement.endl +
                "</div>" + HtmlElement.endl);
     }
 }
