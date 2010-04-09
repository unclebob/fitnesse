package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IncludeTest {
    @Test public void scansIncludes() {
        ParserTest.assertScansTokenType("!include name", SymbolType.Include, true);
    }

    @Test public void parsesIncludes() {
        ParserTest.assertParses("!include PageTwo\n", "SymbolList[Include[Text, WikiWord], Newline]");
        ParserTest.assertParses("!include PageTwo", "SymbolList[Include[Text, WikiWord]]");
        ParserTest.assertParses("!include <PageTwo", "SymbolList[Include[Text, WikiWord]]");
        ParserTest.assertParses("!include <PageTwo>", "SymbolList[Include[Text, WikiWord], Text]");
    }

    @Test public void translatesIncludedSibling() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage currentPage = root.makePage("PageOne", "!include PageTwo");
        root.makePage("PageTwo", "page ''two''");

        String result = ParserTest.translateTo(currentPage);

        assertContains(result, "class=\"collapsable\"");
        assertContains(result, "Included page: <a href=\"PageTwo\">PageTwo</a>");
        assertContains(result, "page <i>two</i>");
    }

    @Test public void translatesSetup() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("PageOne");
        WikiPage child = root.makePage(parent, "PageTwo", "!include -setup >SetUp");
        root.makePage(child, "SetUp", "page ''setup''");

        String result = ParserTest.translateTo(child);

        assertContains(result, "class=\"hidden\"");
        assertContains(result, "<a href=\"PageOne.PageTwo.SetUp\">");
    }

    @Test public void doesNotIncludeParent() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("ParentPage", "stuff");
        WikiPage currentPage = root.makePage(parent, "PageOne", "!include <ParentPage");
        ParserTest.assertTranslatesTo(currentPage,
                "<span class=\"meta\">Error! Cannot include parent page (&lt;ParentPage).\n</span>");

    }
    private void assertContains(String result, String substring) {
        assertTrue(result.indexOf(substring) >= 0);
    }

}
