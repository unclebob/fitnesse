package fitnesse.wikitext.parser;

import static org.junit.Assert.assertTrue;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

public class ContentsTest {
    @Test public void scansContents() {
        ParserTestHelper.assertScansTokenType("!contents", "Contents", true);
        ParserTestHelper.assertScansTokenType(" !contents", "Contents", false);
        ParserTestHelper.assertScansTokenType("!note !contents", "Contents", true);
        ParserTestHelper.assertScansTokenType("| !contents|\n", "Contents", true);
    }

    @Test public void parsesContents() throws Exception {
        ParserTestHelper.assertParses("!contents -f -g", "SymbolList[Contents]");
        ParserTestHelper.assertParses("!contents hi", "SymbolList[Text, Whitespace, Text]");
        ParserTestHelper.assertParses("!contents ]", "SymbolList[Text, Whitespace, CloseBracket]");
    }

    @Test public void translatesContents() throws Exception {
        WikiPage pageOne = makePages();
         ParserTestHelper.assertTranslatesTo(pageOne, "!contents",
                 contentsWithPages("PageThree", "PageTwo", ""));
     }

    @Test public void translatesContentsInInclude() throws Exception {
        WikiPage pageOne = makePages();
        assertContains(ParserTestHelper.translateTo(pageOne, "!include >PageTwo"),
                  contentsWithPages("PageThree", "PageTwo", ""));
     }

    @Test public void translatesRecursiveContents() throws Exception {
        WikiPage pageOne = makePages();
         ParserTestHelper.assertTranslatesTo(pageOne, "!contents -R",
           contentsWithPages("PageThree", "PageTwo",
             nestedContents("\t\t\t", "2", "<a href=\"PageOne.PageTwo.PageTwoChild\" class=\"static\">PageTwoChild</a>",
               nestedContents("\t\t\t\t\t", "3", "<a href=\"PageOne.PageTwo.PageTwoChild.PageTwoGrandChild\" class=\"static\">PageTwoGrandChild</a>", ""))));
     }

    @Test public void translatesRecursiveContentsToLevel() throws Exception {
        WikiPage pageOne = makePages();
         ParserTestHelper.assertTranslatesTo(pageOne, "!contents -R2",
           contentsWithPages("PageThree", "PageTwo",
             nestedContents("\t\t\t", "2", "<a href=\"PageOne.PageTwo.PageTwoChild\" class=\"static\">PageTwoChild</a>",  " ...")));
     }

    @Test public void translatesContentsWithInvalidRecursionLimit() throws Exception {
        WikiPage pageOne = makePages();
         ParserTestHelper.assertTranslatesTo(pageOne, "!contents -Rx",
           contentsWithPages("PageThree", "PageTwo", ""));
     }

    private WikiPage makePages() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage pageOne = root.makePage("PageOne");
        WikiPage pageTwo = root.makePage(pageOne, "PageTwo", "!contents");
        WikiPage pageTwoChild = root.makePage(pageTwo, "PageTwoChild");
        root.makePage(pageTwoChild, "PageTwoGrandChild");
        root.makePage(pageOne, "PageThree");
        return pageOne;
    }

    private String contentsWithPages(String name1, String name2, String nested) {
        return
        "<div class=\"contents\">" + HtmlElement.endl +
        "\t<b>Contents:</b>" + HtmlElement.endl +
        "\t<ul class=\"toc1\">" + HtmlElement.endl +
        "\t\t<li>" + HtmlElement.endl +
        "\t\t\t<a href=\"PageOne.PageThree\" class=\"static\">" + name1 + "</a>" + HtmlElement.endl +
        "\t\t</li>" + HtmlElement.endl +
        "\t\t<li>" + HtmlElement.endl +
        "\t\t\t<a href=\"PageOne.PageTwo\" class=\"static\">" + name2 + "</a>" + HtmlElement.endl + nested +
        "\t\t</li>" + HtmlElement.endl +
        "\t</ul>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl;
    }

    private String nestedContents(String indent, String level, String pageReference, String nested) {
        return
                indent + "<ul class=\"toc" + level + "\">" + HtmlElement.endl +
                indent + "\t<li>" + HtmlElement.endl +
                indent + "\t\t" + pageReference + HtmlElement.endl + nested +
                indent + "\t</li>" + HtmlElement.endl +
                indent + "</ul>" + HtmlElement.endl;
    }

    private void assertContains(String result, String substring) {
      assertTrue(result, result.contains(substring));
    }
}
