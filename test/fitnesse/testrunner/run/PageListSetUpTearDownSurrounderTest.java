package fitnesse.testrunner.run;

import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static fitnesse.wiki.PageData.SUITE_SETUP_NAME;
import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;
import static org.junit.Assert.assertEquals;

public class PageListSetUpTearDownSurrounderTest {
  private WikiPage root;
  private WikiPage suite;
  private WikiPage testPage;

  private PageListSetUpTearDownSurrounder surrounder;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    root.commit(data);
    suite = addChildPage(root, "SuitePageName", "The is the test suite\n");
    testPage = addChildPage(suite, "TestPage", "My test and has some content");
    surrounder = new PageListSetUpTearDownSurrounder();
  }

  @Test
  public void testPagesForTestSystemAreSurroundedByRespectiveSuiteSetupAndTeardown() {
    WikiPage slimPage = addChildPage(testPage, "SlimPageTest");
    WikiPage setUp = addChildPage(root, SUITE_SETUP_NAME);
    WikiPage tearDown = addChildPage(root, SUITE_TEARDOWN_NAME);
    WikiPage setUp2 = addChildPage(slimPage, SUITE_SETUP_NAME);
    WikiPage tearDown2 = addChildPage(slimPage, SUITE_TEARDOWN_NAME);

    List<WikiPage> testPages = surrounder.addSuiteSetUpsAndTearDowns(makeTestPageList());

    assertEquals(6, testPages.size());
    assertPageIsBetween(slimPage, setUp2, tearDown2, testPages);
    assertPageIsBetween(testPage, setUp, tearDown, testPages);
  }

  @Test
  public void testSetUpAndTearDown() {
    WikiPage setUp = addChildPage(root, SUITE_SETUP_NAME, "suite set up");
    WikiPage tearDown = addChildPage(root, SUITE_TEARDOWN_NAME, "suite tear down");

    List<WikiPage> testPages = surrounder.addSuiteSetUpsAndTearDowns(makeTestPageList());
    assertEquals(3, testPages.size());
    assertEquals(setUp, testPages.get(0));
    assertEquals(testPage, testPages.get(1));
    assertEquals(tearDown, testPages.get(2));
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

    List<WikiPage> pages = surrounder.addSuiteSetUpsAndTearDowns(makeTestPageList());

    List<String> paths = getPagePaths(pages);
    assertEquals(
      "[SuiteSetUp, SuitePageName.SlimPage1Suite.SlimPage1Test, SuitePageName.SlimPage1Suite.SuiteTearDown, " +
        "SuitePageName.SlimPage2Suite.SuiteSetUp, SuitePageName.SlimPage2Suite.SlimPage2Test, SuiteTearDown, " +
        "SuiteSetUp, SuitePageName.SlimPage3Suite.SlimPage3Test, SuitePageName.TestPage, SuiteTearDown]",
      paths.toString());
  }

  private List<WikiPage> makeTestPageList() {
    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    return finder.getAllPagesToRunForThisSuite();
  }

  private List<String> getPagePaths(List<WikiPage> pages) {
    List<String> list = new ArrayList<>(pages.size());
    for (WikiPage page : pages) {
      list.add(page.getFullPath().toString());
    }
    return list;
  }

  private void assertPageIsBetween(WikiPage curr, WikiPage prev, WikiPage next,
                                   List<WikiPage> pages) {
    int currIndex = pages.indexOf(curr);
    assertEquals(prev, pages.get(currIndex - 1));
    assertEquals(next, pages.get(currIndex + 1));
  }

  private WikiPage addChildPage(WikiPage suite, String childName) {
    return addChildPage(suite, childName, "");
  }

  private WikiPage addChildPage(WikiPage suite, String childName, String s) {
    return WikiPageUtil.addPage(suite, PathParser.parse(childName), s);
  }
}
