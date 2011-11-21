package fitnesse.responders.run;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class TestPage {
    public TestPage(WikiPage sourcePage) {
        this.sourcePage = sourcePage;
        try {
            data = sourcePage.getData();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TestPage(PageData data) {
        this.data = data;
        this.sourcePage = data.getWikiPage();
    }

    public WikiPage getSourcePage() { return sourcePage; }
    public PageData getData() { return data; }
    public PageData getDecoratedData() { return decoratedData != null ? decoratedData : data; }
    public String getName() { return sourcePage.getName(); }

    public void decorate(String decoratedContent) {
        try {
            decoratedData = new PageData(sourcePage, decoratedContent);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSlim() {
        try {
            return "slim".equalsIgnoreCase(data.getVariable("TEST_SYSTEM"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isTestPage() {
      return data.hasAttribute("Test");
    }

    private WikiPage sourcePage;
    private PageData data;
    private PageData decoratedData;
}
