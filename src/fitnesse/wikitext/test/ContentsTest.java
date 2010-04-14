package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class ContentsTest {
    @Test public void scansContents() {
        ParserTest.assertScansTokenType("!contents", SymbolType.Contents, true);
    }

    @Test public void parsesContents() {
        ParserTest.assertParses("!contents -f -g", "SymbolList[Contents[Text, Text]]");
        ParserTest.assertParses("!contents hi", "SymbolList[Text, Whitespace, Text]");
        ParserTest.assertParses("!contents ]", "SymbolList[Text, Whitespace, CloseBracket]");
    }

    @Test public void translatesContents() throws Exception {
        WikiPage pageOne = makePages();
         ParserTest.assertTranslatesTo(pageOne, "!contents",
                 contentsWithPages("PageThree", "PageTwo", ""));
     }

    @Test public void translatesRecursiveContents() throws Exception {
        WikiPage pageOne = makePages();
         ParserTest.assertTranslatesTo(pageOne, "!contents -R",
                 contentsWithPages("PageThree", "PageTwo", nestedContents()));
     }

    private WikiPage makePages() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage pageOne = root.makePage("PageOne");
        WikiPage pageTwo = root.makePage(pageOne, "PageTwo");
        root.makePage(pageTwo, "PageTwoChild");
        root.makePage(pageOne, "PageThree");
        return pageOne;
    }


    private String contentsWithPages(String name1, String name2, String nested) {
        return "<div class=\"toc1\">" + HtmlElement.endl +
        "\t<div class=\"contents\">" + HtmlElement.endl +
        "\t\t<b>Contents:</b>" + HtmlElement.endl +
        "\t\t<ul>" + HtmlElement.endl +
        "\t\t\t<li>" + HtmlElement.endl +
        "\t\t\t\t<a href=\"PageOne.PageThree\">" + name1 + "</a>" + HtmlElement.endl +
        "\t\t\t</li>" + HtmlElement.endl +
        "\t\t\t<li>" + HtmlElement.endl +
        "\t\t\t\t<a href=\"PageOne.PageTwo\">" + name2 + "</a>" + HtmlElement.endl + nested +
        "\t\t\t</li>" + HtmlElement.endl +
        "\t\t</ul>" + HtmlElement.endl +
        "\t</div>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl;
    }

    private String nestedContents() {
        String indent = "\t\t\t\t";
        return
                indent + "<div class=\"toc2\">" + HtmlElement.endl +
                indent + "\t<div class=\"nested-contents\">" + HtmlElement.endl +
                indent + "\t\t<ul>" + HtmlElement.endl +
                indent + "\t\t\t<li>" + HtmlElement.endl +
                indent + "\t\t\t\t<a href=\"PageOne.PageTwo.PageTwoChild\">PageTwoChild</a>" + HtmlElement.endl +
                indent + "\t\t\t</li>" + HtmlElement.endl +
                indent + "\t\t</ul>" + HtmlElement.endl +
                indent + "\t</div>" + HtmlElement.endl +
                indent + "</div>" + HtmlElement.endl;
    }
}
