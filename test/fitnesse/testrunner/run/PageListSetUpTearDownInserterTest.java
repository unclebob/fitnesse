package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fitnesse.wiki.PageData.SUITE_SETUP_NAME;
import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;
import static org.junit.Assert.assertEquals;

public class PageListSetUpTearDownInserterTest extends PageListSetUpTearDownTestBase {

  private PageListSetUpTearDownInserter inserter;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    inserter = new PageListSetUpTearDownInserter();
  }

  @Override
  protected List<WikiPage> addSuiteSetUpsAndTearDowns(List<WikiPage> pages) {
    return inserter.addSuiteSetUpsAndTearDowns(pages);
  }

  @Test
  public void testPagesSuiteSetupsAndTeardownsAreAddedAsExpected() {
    addChildPage(root, SUITE_SETUP_NAME);
    addChildPage(root, SUITE_TEARDOWN_NAME);
    WikiPage slimPage = addChildPage(testPage, "SlimPageTest");
    addChildPage(slimPage, SUITE_SETUP_NAME);
    addChildPage(slimPage, SUITE_TEARDOWN_NAME);

    List<WikiPage> testPages = addSuiteSetUpsAndTearDowns(makeTestPageList());

    assertEquals(6, testPages.size());
    List<String> paths = getPagePaths(testPages);
    assertEquals(
      "[SuiteSetUp, " +
        "SuitePageName.TestPage, " +
        "SuitePageName.TestPage.SlimPageTest.SuiteSetUp, " +
        "SuitePageName.TestPage.SlimPageTest, " +
        "SuitePageName.TestPage.SlimPageTest.SuiteTearDown, " +
        "SuiteTearDown]",
      paths.toString());
  }

  @Test
  public void testSuiteOrderingDoesNotDependOnPresenceOfSuiteSetup() {
    addChildPage(root, SUITE_SETUP_NAME);
    addChildPage(root, SUITE_TEARDOWN_NAME);
    addChildPage(suite, "AtoplevelTest");
    WikiPage slimSuite1 = addChildPage(suite, "SlimPage1Suite");
    addChildPage(slimSuite1, "SlimPage1Test");
    addChildPage(slimSuite1, SUITE_TEARDOWN_NAME);
    WikiPage slimSuite2 = addChildPage(suite, "SlimPage2Suite");
    addChildPage(slimSuite2, "SlimPage2Test");

    List<WikiPage> noNestedSetUpPages = addSuiteSetUpsAndTearDowns(makeTestPageList());
    List<String> testPagePaths1 = getNormalPagePaths(noNestedSetUpPages);

    addChildPage(slimSuite2, SUITE_SETUP_NAME);
    List<WikiPage> nestedSetUpPages = addSuiteSetUpsAndTearDowns(makeTestPageList());
    List<String> testPagePaths2 = getNormalPagePaths(nestedSetUpPages);

    assertEquals(testPagePaths1, testPagePaths2);
  }

  @Test
  public void testSetUpAndTearDownExecutedOnlyOnce() {
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
    Set<String> uniquePaths = new HashSet<>(paths);
    assertEquals(uniquePaths.size(), paths.size());
  }

  @Test
  public void testAllSetUpsRunBeforeTest() {
    WikiPage test = addChildPage(suite, "SlimPage1Test");

    List<WikiPage> pages = addSuiteSetUpsAndTearDowns(Collections.singletonList(test));
    List<String> paths = getPagePaths(pages);
    assertEquals(paths.toString(), 1, paths.size());

    addChildPage(root, SUITE_SETUP_NAME);
    pages = addSuiteSetUpsAndTearDowns(Collections.singletonList(test));
    paths = getPagePaths(pages);
    assertEquals(paths.toString(), 2, paths.size());

    addChildPage(suite, SUITE_SETUP_NAME);
    pages = addSuiteSetUpsAndTearDowns(Collections.singletonList(test));
    paths = getPagePaths(pages);
    assertEquals(paths.toString(), 3, paths.size());
    assertEquals("Wrong order",
      "[SuiteSetUp, SuitePageName.SuiteSetUp, SuitePageName.SlimPage1Test]",
      paths.toString());
  }

  @Test
  public void testAllTearDownsRunAfterTest() {
    WikiPage test = addChildPage(suite, "SlimPage1Test");

    List<WikiPage> pages = addSuiteSetUpsAndTearDowns(Collections.singletonList(test));
    List<String> paths = getPagePaths(pages);
    assertEquals(paths.toString(), 1, paths.size());

    addChildPage(root, SUITE_TEARDOWN_NAME);
    pages = addSuiteSetUpsAndTearDowns(Collections.singletonList(test));
    paths = getPagePaths(pages);
    assertEquals(paths.toString(), 2, paths.size());

    addChildPage(suite, SUITE_TEARDOWN_NAME);
    pages = addSuiteSetUpsAndTearDowns(Collections.singletonList(test));
    paths = getPagePaths(pages);
    assertEquals(paths.toString(), 3, paths.size());
    assertEquals("Wrong order",
      "[SuitePageName.SlimPage1Test, SuitePageName.SuiteTearDown, SuiteTearDown]",
      paths.toString());
  }

  private List<String> getNormalPagePaths(List<WikiPage> pages) {
    List<String> paths = getPagePaths(pages);
    paths.removeIf(p -> p.endsWith(SUITE_SETUP_NAME) || p.endsWith(SUITE_TEARDOWN_NAME));
    return paths;
  }
}
