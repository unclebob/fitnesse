package fitnesse.responders.run;

import fitnesse.wiki.PageData;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

public class TestPage {
    public TestPage(WikiPage sourcePage) {
        this.sourcePage = sourcePage;
    }

    public TestPage(PageData data) {
        this.data = data;
        this.sourcePage = data.getWikiPage();
    }

    public WikiPage getSourcePage() { return sourcePage; }
    public PageData getData() { return data == null ? sourcePage.getData() : data; }
    public ReadOnlyPageData parsedData() { return sourcePage.readOnlyData(); }
    public PageData getDecoratedData() { return decoratedData != null ? decoratedData : getData(); }
    public String getName() { return sourcePage.getName(); }

    public void decorate(String decoratedContent) {
        decoratedData = new PageData(sourcePage, decoratedContent);
   }

    public boolean isSlim() {
        return "slim".equalsIgnoreCase(parsedData().getVariable("TEST_SYSTEM"));
    }

    public boolean isTestPage() {
      return parsedData().hasAttribute("Test");
    }

    private WikiPage sourcePage;
    private PageData data;
    private PageData decoratedData;
}
