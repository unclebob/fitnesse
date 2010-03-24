package fitnesse.wikitext.parser;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TestRoot {
    private WikiPage root;
    private PageCrawler crawler;

    public TestRoot() throws Exception {
        root = InMemoryPage.makeRoot("RooT");
        crawler = root.getPageCrawler();
    }

    public WikiPage makePage(String pageName) throws Exception {
        return makePage(root, pageName);
    }

    public WikiPage makePage(WikiPage parent, String pageName) throws Exception {
        return crawler.addPage(parent, PathParser.parse(pageName));
    }
}
