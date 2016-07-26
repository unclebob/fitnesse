package fitnesse.wiki;

import static fitnesse.wiki.PageType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Test;


public class PageTypeTest {

  @Test
  public void fromString() {
    assertEquals(SUITE, PageType.fromString("Suite"));
    assertEquals(TEST, PageType.fromString(TEST.toString()));
    assertEquals(STATIC, PageType.fromString("Static"));

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
    assertEquals(STATIC, PageType.fromWikiPage(page));
  }

  private Collection<Object[]> pageTypeFromPageNameData() {
    List<Object[]> values = new ArrayList<>();

    addTestData(values, SUITE, "SuitePage");
    addTestData(values, SUITE, "PageSuite");
    addTestData(values, SUITE, "PageExamples");

    addTestData(values, TEST, "TestPage");
    addTestData(values, TEST, "PageTest");
    addTestData(values, TEST, "ExamplePage");
    addTestData(values, TEST, "PageExample");

    addTestData(values, STATIC, "NormalPage");

    addTestData(values, STATIC, "SuiteSetUp");
    addTestData(values, STATIC, "SetUp");

    addTestData(values, STATIC, "SuiteTearDown");
    addTestData(values, STATIC, "TearDown");

    addTestData(values, STATIC, "ExamplesNormal");

    return values;
  }

  private static void addTestData(List<Object[]> values, PageType test,
      String string) {
    values.add(new Object[] { test, string});
  }

  @Test
  public void pageTypeFromPageName() {
    Collection<Object[]> testData = pageTypeFromPageNameData();

    for (Object[] testItem: testData) {
      PageType pageType = (PageType) testItem[0];
      String pageName = (String) testItem[1];
      assertEquals(pageType, getPageTypeForPageName(pageName));
    }
  }

}
