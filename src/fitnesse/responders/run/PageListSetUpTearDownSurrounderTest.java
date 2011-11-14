package fitnesse.responders.run;

import fitnesse.wiki.*;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class PageListSetUpTearDownSurrounderTest {
  private WikiPage root;
  private WikiPage suite;
  private WikiPage testPage;
  private PageCrawler crawler;

  private PageListSetUpTearDownSurrounder surrounder;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    PageData data = root.getData();
    root.commit(data);
    suite = crawler.addPage(root, PathParser.parse("SuitePageName"), "The is the test suite\n");
    testPage = crawler.addPage(suite, PathParser.parse("TestPage"), "My test and has some content");
    surrounder = new PageListSetUpTearDownSurrounder(root);
  }

  @Test
  public void testPagesForTestSystemAreSurroundedByRespectiveSuiteSetupAndTeardown() throws Exception {
    WikiPage slimPage = crawler.addPage(testPage, PathParser.parse("SlimPageTest"));
    WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"));
    WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"));
    WikiPage setUp2 = crawler.addPage(slimPage, PathParser.parse("SuiteSetUp"));
    WikiPage tearDown2 = crawler.addPage(slimPage, PathParser.parse("SuiteTearDown"));

    ArrayList<TestPage> testPages = MakeTestPageList();
    surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(testPages);

    assertEquals(6, testPages.size());

    assertSame(setUp2, testPages.get(0).getSourcePage());
    assertSame(slimPage, testPages.get(1).getSourcePage());
    assertSame(tearDown2, testPages.get(2).getSourcePage());
    assertSame(setUp, testPages.get(3).getSourcePage());
    assertSame(testPage, testPages.get(4).getSourcePage());
    assertSame(tearDown, testPages.get(5).getSourcePage());
  }

    private ArrayList<TestPage> MakeTestPageList() throws Exception {
        SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
        ArrayList<TestPage> testPages = new ArrayList<TestPage>();
        for (WikiPage page : finder.getAllPagesToRunForThisSuite()) testPages.add(new TestPage(page));
        return testPages;
    }

  @Test
  public void testSetUpAndTearDown() throws Exception {
    WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

    ArrayList<TestPage> testPages = MakeTestPageList();
    surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(testPages);
    assertEquals(3, testPages.size());
    assertSame(setUp, testPages.get(0).getSourcePage());
    assertSame(testPage, testPages.get(1).getSourcePage());
    assertSame(tearDown, testPages.get(2).getSourcePage());
  }
}
