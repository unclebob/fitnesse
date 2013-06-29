package fitnesse.testrunner;

import fitnesse.responders.run.SuiteContentsFinder;
import fitnesse.wiki.*;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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
    WikiPage slimPage = WikiPageUtil.addPage(testPage, PathParser.parse("SlimPageTest"));
    WikiPage setUp = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    WikiPage tearDown = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    WikiPage setUp2 = WikiPageUtil.addPage(slimPage, PathParser.parse("SuiteSetUp"));
    WikiPage tearDown2 = WikiPageUtil.addPage(slimPage, PathParser.parse("SuiteTearDown"));

    ArrayList<WikiTestPage> testPages = MakeTestPageList();
    surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(testPages);

    assertEquals(6, testPages.size());

    assertEquals(setUp2, testPages.get(0).getSourcePage());
    assertEquals(slimPage, testPages.get(1).getSourcePage());
    assertEquals(tearDown2, testPages.get(2).getSourcePage());
    assertEquals(setUp, testPages.get(3).getSourcePage());
    assertEquals(testPage, testPages.get(4).getSourcePage());
    assertEquals(tearDown, testPages.get(5).getSourcePage());
  }

    private ArrayList<WikiTestPage> MakeTestPageList() throws Exception {
        SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
        ArrayList<WikiTestPage> testPages = new ArrayList<WikiTestPage>();
        for (WikiPage page : finder.getAllPagesToRunForThisSuite()) testPages.add(new WikiTestPage(page));
        return testPages;
    }

  @Test
  public void testSetUpAndTearDown() throws Exception {
    WikiPage setUp = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

    ArrayList<WikiTestPage> testPages = MakeTestPageList();
    surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(testPages);
    assertEquals(3, testPages.size());
    assertEquals(setUp, testPages.get(0).getSourcePage());
    assertEquals(testPage, testPages.get(1).getSourcePage());
    assertEquals(tearDown, testPages.get(2).getSourcePage());
  }
}
