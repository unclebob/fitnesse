package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;

public class TestRoot {
    public WikiPage root;

    public TestRoot() {
        root = InMemoryPage.makeRoot("root");
    }

    public WikiPage makePage(String pageName) {
        return makePage(root, pageName);
    }

    public WikiPage makePage(WikiPage parent, String pageName) {
        return WikiPageUtil.addPage(parent, PathParser.parse(pageName), "");
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
