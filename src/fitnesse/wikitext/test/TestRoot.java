package fitnesse.wikitext.test;

import fitnesse.wiki.*;

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

    public WikiPage makePage(String pageName, String content) throws Exception {
        return makePage(root, pageName, content);
    }

    public WikiPage makePage(WikiPage parent, String pageName, String content) throws Exception {
        WikiPage page = makePage(parent, pageName);
        PageData data = page.getData();
        data.setContent(content);
        page.commit(data);
        return page;
    }
}
