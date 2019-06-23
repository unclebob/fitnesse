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

public abstract class PageListSetUpTearDownTestBase {
  protected WikiPage root;
  protected WikiPage suite;
  protected WikiPage testPage;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    root.commit(data);
    suite = addChildPage(root, "SuitePageName", "The is the test suite\n");
    testPage = addChildPage(suite, "TestPage", "My test and has some content");
  }

  @Test
  public void testSetUpAndTearDown() {
    WikiPage setUp = addChildPage(root, SUITE_SETUP_NAME, "suite set up");
    WikiPage tearDown = addChildPage(root, SUITE_TEARDOWN_NAME, "suite tear down");

    List<WikiPage> testPages = addSuiteSetUpsAndTearDowns(makeTestPageList());
    assertEquals(3, testPages.size());
    assertEquals(setUp, testPages.get(0));
    assertEquals(testPage, testPages.get(1));
    assertEquals(tearDown, testPages.get(2));
  }

  protected abstract List<WikiPage> addSuiteSetUpsAndTearDowns(List<WikiPage> pages);

  protected List<WikiPage> makeTestPageList() {
    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    return finder.getAllPagesToRunForThisSuite();
  }

  protected List<String> getPagePaths(List<WikiPage> pages) {
    List<String> list = new ArrayList<>(pages.size());
    for (WikiPage page : pages) {
      list.add(page.getFullPath().toString());
    }
    return list;
  }

  protected WikiPage addChildPage(WikiPage suite, String childName) {
    return addChildPage(suite, childName, "");
  }

  protected WikiPage addChildPage(WikiPage suite, String childName, String s) {
    return WikiPageUtil.addPage(suite, PathParser.parse(childName), s);
  }
}
