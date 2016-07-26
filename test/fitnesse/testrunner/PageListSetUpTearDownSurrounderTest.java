package fitnesse.testrunner;

import fitnesse.wiki.*;
import static org.junit.Assert.assertEquals;

import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
    suite = WikiPageUtil.addPage(root, PathParser.parse("SuitePageName"), "The is the test suite\n");
    testPage = WikiPageUtil.addPage(suite, PathParser.parse("TestPage"), "My test and has some content");
    surrounder = new PageListSetUpTearDownSurrounder(root);
  }

  @Test
  public void testPagesForTestSystemAreSurroundedByRespectiveSuiteSetupAndTeardown() throws Exception {
    WikiPage slimPage = WikiPageUtil.addPage(testPage, PathParser.parse("SlimPageTest"), "");
    WikiPage setUp = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "");
    WikiPage tearDown = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "");
    WikiPage setUp2 = WikiPageUtil.addPage(slimPage, PathParser.parse("SuiteSetUp"), "");
    WikiPage tearDown2 = WikiPageUtil.addPage(slimPage, PathParser.parse("SuiteTearDown"), "");

    List<WikiPage> testPages = surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(makeTestPageList());

    assertEquals(6, testPages.size());
    assertPageIsBetween(slimPage, setUp2, tearDown2, testPages);
    assertPageIsBetween(testPage, setUp, tearDown, testPages);
  }

    private ArrayList<WikiPage> makeTestPageList() throws Exception {
        SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
        ArrayList<WikiPage> testPages = new ArrayList<>();
        for (WikiPage page : finder.getAllPagesToRunForThisSuite()) testPages.add(page);
        return testPages;
    }

  @Test
  public void testSetUpAndTearDown() throws Exception {
    WikiPage setUp = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

    List<WikiPage> testPages = surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(makeTestPageList());
    assertEquals(3, testPages.size());
    assertEquals(setUp, testPages.get(0));
    assertEquals(testPage, testPages.get(1));
    assertEquals(tearDown, testPages.get(2));
  }

  private void assertPageIsBetween(WikiPage curr, WikiPage prev, WikiPage next,
          List<WikiPage> pages) {
    int currIndex = pages.indexOf(curr);
    assertEquals(prev, pages.get(currIndex - 1));
    assertEquals(next, pages.get(currIndex + 1));
  }
}
