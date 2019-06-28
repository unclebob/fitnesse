package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.List;

import static fitnesse.wiki.PageData.SUITE_SETUP_NAME;
import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;
import static org.junit.Assert.assertEquals;

public class PageListSetUpTearDownSurrounderTest extends PageListSetUpTearDownTestBase {

  private PageListSetUpTearDownSurrounder surrounder;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    surrounder = new PageListSetUpTearDownSurrounder();
  }

  @Override
  protected List<WikiPage> addSuiteSetUpsAndTearDowns(List<WikiPage> pages) {
    return surrounder.addSuiteSetUpsAndTearDowns(pages);
  }

  @Test
  public void testPagesForTestSystemAreSurroundedByRespectiveSuiteSetupAndTeardown() {
    WikiPage slimPage = addChildPage(testPage, "SlimPageTest");
    WikiPage setUp = addChildPage(root, SUITE_SETUP_NAME);
    WikiPage tearDown = addChildPage(root, SUITE_TEARDOWN_NAME);
    WikiPage setUp2 = addChildPage(slimPage, SUITE_SETUP_NAME);
    WikiPage tearDown2 = addChildPage(slimPage, SUITE_TEARDOWN_NAME);

    List<WikiPage> testPages = addSuiteSetUpsAndTearDowns(makeTestPageList());

    assertEquals(6, testPages.size());
    List<String> paths = getPagePaths(testPages);
    assertPageIsBetween(slimPage, setUp2, tearDown2, testPages);
    assertPageIsBetween(testPage, setUp, tearDown, testPages);
  }

  @Test
  public void testSetUpAndTearDownAroundEachGroup() {
    addChildPage(root, SUITE_SETUP_NAME);
    addChildPage(root, SUITE_TEARDOWN_NAME);
    WikiPage slimSuite1 = addChildPage(suite, "SlimPage1Suite");
    addChildPage(slimSuite1, "SlimPage1Test");
    addChildPage(slimSuite1, SUITE_TEARDOWN_NAME);
    WikiPage slimSuite2 = addChildPage(suite, "SlimPage2Suite");
    addChildPage(slimSuite2, "SlimPage2Test");
    WikiPage slimSuite3 = addChildPage(suite, "SlimPage3Suite");
    addChildPage(slimSuite3, "SlimPage3Test");
    addChildPage(slimSuite2, SUITE_SETUP_NAME);

    List<WikiPage> pages = addSuiteSetUpsAndTearDowns(makeTestPageList());

    List<String> paths = getPagePaths(pages);
    assertEquals(
      "[SuiteSetUp, SuitePageName.SlimPage1Suite.SlimPage1Test, SuitePageName.SlimPage1Suite.SuiteTearDown, " +
        "SuitePageName.SlimPage2Suite.SuiteSetUp, SuitePageName.SlimPage2Suite.SlimPage2Test, SuiteTearDown, " +
        "SuiteSetUp, SuitePageName.SlimPage3Suite.SlimPage3Test, SuitePageName.TestPage, SuiteTearDown]",
      paths.toString());
  }

  private void assertPageIsBetween(WikiPage curr, WikiPage prev, WikiPage next,
                                     List<WikiPage> pages) {
    int currIndex = pages.indexOf(curr);
    assertEquals(prev.getFullPath(), pages.get(currIndex - 1).getFullPath());
    assertEquals(next.getFullPath(), pages.get(currIndex + 1).getFullPath());
  }
}
