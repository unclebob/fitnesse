package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IncludeTest {
    @Test public void scansIncludes() {
        ParserTest.assertScansTokenType("!include name", "Include", true);
    }

    @Test public void parsesIncludes() throws Exception {
        ParserTest.assertParses("!include PageTwo\n", "SymbolList[Include[Text, WikiWord, Meta[Text]], Newline]");
        ParserTest.assertParses("|!include PageTwo|\n", "SymbolList[Table[SymbolList[SymbolList[Include[Text, WikiWord, Meta[Text]]]]]]");
        ParserTest.assertParses("!include PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]");
        ParserTest.assertParses("!include -c PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]");
        ParserTest.assertParses("!include <PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]");
        ParserTest.assertParses("!include <PageTwo>", "SymbolList[Include[Text, WikiWord, Meta[Text]], Text]");
    }

    @Test public void translatesIncludedSibling() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage currentPage = root.makePage("PageOne", "!include PageTwo");
        root.makePage("PageTwo", "page ''two''");

        String result = ParserTest.translateTo(currentPage);

        assertContains(result, "class=\"collapsable\"");
        assertContains(result, "Included page: <a href=\"PageTwo\">PageTwo</a> <a href=\"PageTwo?edit&amp;redirectToReferer=true&amp;redirectAction=\">(edit)</a>");
        assertContains(result, "page <i>two</i>");
    }

    @Test public void translatesSetup() throws Exception {
        String result = ParserTest.translateTo(makeIncludingPage());

        assertContains(result, "class=\"hidden\"");
        assertContains(result, "<a href=\"PageTwo.SetUp\">");
    }

    private TestSourcePage makeIncludingPage() {
        return new TestSourcePage()
                .withContent("!include -setup >SetUp")
                .withTarget("PageTwo.SetUp")
                .withIncludedPage(new TestSourcePage().withContent("setup"));
    }

    @Test public void translatesSetupWithoutCollapse() throws Exception {
        String result = ParserTest.translateTo(makeIncludingPage(), new TestVariableSource("COLLAPSE_SETUP", "false"));

        assertContains(result, "class=\"collapsable\"");
        assertContains(result, "<a href=\"PageTwo.SetUp\">");
    }

    @Test public void translatesCollapsed() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage includingPage = root.makePage("PageOne", "!include -c PageTwo");
        root.makePage("PageTwo", "two");

        String result = ParserTest.translateTo(includingPage);

        assertContains(result, "class=\"hidden\"");
    }

    @Test public void translatesSeamless() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage includingPage = root.makePage("PageOne", "!include -seamless PageTwo");
        root.makePage("PageTwo", "two");

        ParserTest.assertTranslatesTo(includingPage, "two");
    }

    @Test public void doesNotIncludeParent() throws Exception {
        TestRoot root = new TestRoot();
        WikiPage parent = root.makePage("ParentPage", "stuff");
        WikiPage currentPage = root.makePage(parent, "PageOne", "!include <ParentPage");
        ParserTest.assertTranslatesTo(currentPage,
                "<span class=\"meta\">Error! Cannot include parent page (&lt;ParentPage).\n</span>");

    }
    private void assertContains(String result, String substring) {
        assertTrue(result, result.indexOf(substring) >= 0);
    }

}
