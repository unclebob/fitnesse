package fitnesse.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static fitnesse.wiki.PageType.*;

import org.junit.Test;


public class PageTypeTest {

  @Test
  public void fromString() {
    assertEquals(SUITE, PageType.fromString("Suite"));
    assertEquals(TEST, PageType.fromString(TEST.toString()));
    assertEquals(NORMAL, PageType.fromString("Normal"));

    try {
      PageType.fromString("unknown");
      fail("should have thrown an exception");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void fromSuiteWikiPage() throws Exception {
    WikiPage page = createSuitePage();
    assertEquals(SUITE, PageType.fromWikiPage(page));
  }

  private WikiPage createSuitePage() throws Exception {
    WikiPage page = createDefaultPage();
    setPageTypeAttribute(page, SUITE);
    return page;
  }

  private void setPageTypeAttribute(WikiPage page, PageType attribute) throws Exception {
    PageData pageData = page.getData();
    pageData.setAttribute(attribute.toString(), "true");
    page.commit(pageData);
  }

  private WikiPage createDefaultPage() throws Exception {
    WikiPage page = InMemoryPage.makeRoot("RooT");
    return page;
  }

  @Test
  public void fromTestWikiPage() throws Exception {
    WikiPage page = createTestPage();
    assertEquals(PageType.TEST, PageType.fromWikiPage(page));
  }

  private WikiPage createTestPage() throws Exception {
    WikiPage page = createDefaultPage();
    setPageTypeAttribute(page, TEST);
    return page;
  }

  @Test
  public void fromNormalWikiPage() throws Exception {
    WikiPage page = createDefaultPage();
    assertEquals(NORMAL, PageType.fromWikiPage(page));
  }


}
