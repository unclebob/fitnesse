package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
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
                 contentsWithPages("PageThree", "PageTwo"));
     }

    @Test public void translatesContentsWithFilters() throws Exception {
        WikiPage pageOne = makePages();
        setProperties(pageOne.getChildPage("PageTwo"), new String[]{"Suites=F1"});
         ParserTest.assertTranslatesTo(pageOne, "!contents -f",
                 contentsWithPages("PageThree", "PageTwo (F1)"));
     }

    private WikiPage makePages() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage pageOne = root.makePage("PageOne");
        root.makePage(pageOne, "PageTwo");
        root.makePage(pageOne, "PageThree");
        return pageOne;
    }

    private void setProperties(WikiPage page, String[] propList) throws Exception {
      PageData data = page.getData();
      WikiPageProperties props = data.getProperties();
      for (int i = 0; i < propList.length; i++) {
        String[] parts = propList[i].split("=");
        if (parts.length == 1) props.set(parts[0]);
        else props.set(parts[0], parts[1]);
      }

      page.commit(data);
    }

    private String contentsWithPages(String name1, String name2) {
        return "<div class=\"toc1\">" + HtmlElement.endl +
        "\t<div class=\"contents\">" + HtmlElement.endl +
        "\t\t<b>Contents:</b>" + HtmlElement.endl +
        "\t\t<ul>" + HtmlElement.endl +
        "\t\t\t<li>" + HtmlElement.endl +
        "\t\t\t\t<a href=\"PageOne.PageThree\">" + name1 + "</a>" + HtmlElement.endl +
        "\t\t\t</li>" + HtmlElement.endl +
        "\t\t\t<li>" + HtmlElement.endl +
        "\t\t\t\t<a href=\"PageOne.PageTwo\">" + name2 + "</a>" + HtmlElement.endl +
        "\t\t\t</li>" + HtmlElement.endl +
        "\t\t</ul>" + HtmlElement.endl +
        "\t</div>" + HtmlElement.endl +
        "</div>" + HtmlElement.endl;
    }
}
