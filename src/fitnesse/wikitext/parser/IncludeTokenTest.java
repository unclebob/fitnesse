package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class IncludeTokenTest {
    @Test public void scansIncludes() {
        ParserTest.assertScans("!include name", "Include,Whitespace= ,Word=name");
    }

    @Test public void translatesIncludedSibling() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT");
        PageCrawler crawler = root.getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPage currentPage = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
        crawler.addPage(root, PathParser.parse("PageTwo"), "page two");
        String result = new Translator(currentPage).translate("!include PageTwo\n");

        assertContains(result, "class=\"collapsable\"");
        assertContains(result, "page two");
    }

    private void assertContains(String result, String substring) {
        assertTrue(result.indexOf(substring) >= 0);
    }

}
