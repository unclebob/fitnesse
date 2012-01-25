package fitnesse.wikitext.test;

import fitnesse.wiki.*;

public class TestRoot {
    public WikiPage root;
    private PageCrawler crawler;

    public TestRoot() {
        root = InMemoryPage.makeRoot("root");
        crawler = root.getPageCrawler();
    }

    public WikiPage makePage(String pageName) {
        return makePage(root, pageName);
    }

    public WikiPage makePage(WikiPage parent, String pageName) {
        return crawler.addPage(parent, PathParser.parse(pageName));
    }

    public WikiPage makePage(String pageName, String content) {
        return makePage(root, pageName, content);
    }

    public WikiPage makePage(WikiPage parent, String pageName, String content) {
        WikiPage page = makePage(parent, pageName);
        setPageData(page, content);
        return page;
    }

    public void setPageData(WikiPage page, String content) {
        PageData data = page.getData();
        data.setContent(content);
        page.commit(data);
    }
}
