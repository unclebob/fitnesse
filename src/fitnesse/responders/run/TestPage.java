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

    public WikiPage getSourcePage() { return sourcePage; }
    public PageData getData() { return data; }
    public String getName() { return sourcePage.getName(); }

    private WikiPage sourcePage;
    private PageData data;
}
