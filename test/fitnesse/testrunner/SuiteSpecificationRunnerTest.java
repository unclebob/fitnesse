package fitnesse.testrunner;

import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.Table;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//Proper table format:
//|suite|
//|Page|RootPageForSearch|
//|Title|titleRegEx|
//|Content|contentRegEx|

public class SuiteSpecificationRunnerTest {
  private SuiteSpecificationRunner runner;
  private WikiPage root;


  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("TestPageOne"), "TestPageOne has some testing content and a child");
    WikiPage child = WikiPageUtil.addPage(root, PathParser.parse("TestPageOne.ChildPage"), "ChildPage is a child of TestPageOne");
    PageData data = child.getData();
    data.setAttribute("Test");
    child.commit(data);
    WikiPageUtil.addPage(root, PathParser.parse("TestPageTwo"), "TestPageTwo has a bit of content too");
    runner = new SuiteSpecificationRunner(root);
  }


  @Test
  public void shouldBeAbleToGetAListOfTestPagesGivenATitleAndRootPage() throws Exception {
    runner.titleRegEx = "Test";
    runner.findPageMatches();
    assertEquals(2, runner.testPages().size());
  }

  @Test
  public void shouldBeAbleToGetAListOfTestPagesGivenAContentAndRootPage() throws Exception {
    runner.contentRegEx = "child";
    runner.findPageMatches();
    assertEquals(2, runner.testPages().size());
  }

  @Test
  public void shouldNotGetAPageMoreThanOnce() throws Exception {
    runner.titleRegEx = "Test";
    runner.findPageMatches();
    assertEquals(2, runner.testPages().size());
    runner.titleRegEx = "";
    runner.contentRegEx = "child";
    runner.findPageMatches();
    assertEquals(3, runner.testPages().size());
  }

  @Test
  public void canSeeIfItIsASpecifcationsTable() throws Exception {
     String page = "<table><tr><td>Suite</td></tr><tr><td>Page</td><td>TestPageOne</td></tr><tr><td></td></tr></table>";
    HtmlTableScanner scanner = new HtmlTableScanner(page);
    Table table = scanner.getTable(0);
    assertTrue(SuiteSpecificationRunner.isASuiteSpecificationsTable(table));
    page = "<table><tr><td>no suite</td></tr><tr><td>Page</td><td>TestPageOne</td></tr></table>";
    scanner = new HtmlTableScanner(page);
    table = scanner.getTable(0);
    assertFalse(SuiteSpecificationRunner.isASuiteSpecificationsTable(table));
  }

  @Test
  public void cannTellIfItIsASuiteSpecificationsPage() throws Exception {
    String page = "<table><tr><td>Suite</td></tr><tr><td>Page</td><td>TestPageOne</td></tr><tr><td></td></tr></table>";
    assertTrue(SuiteSpecificationRunner.isASuiteSpecificationsPage(page));
    page = "<table><tr><td>Suite</td></tr><tr><td>Page</td><td>TestPageOne</td></tr></table>";
    assertFalse(SuiteSpecificationRunner.isASuiteSpecificationsPage(page));
  }

  @Test
  public void shouldBeAbleToParseASmallTableAndGetThePageName() throws Exception {
    String page = "<table><tr><td>Suite</td></tr><tr><td>Page</td><td>TestPageOne</td></tr><tr><td></td></tr></table>";
    HtmlTableScanner scanner = new HtmlTableScanner(page);
    Table table = scanner.getTable(0);
    assertTrue(runner.getImportantTableInformation(table));
    assertEquals("TestPageOne",runner.searchRoot.getName());
    assertEquals(0,runner.testPages().size());
  }

  @Test
  public void gettingTableContentShouldFailIfPageHasTablesWithWrongFormat() throws Exception {
    String page = "<table>badTable</table>";
    assertFalse(runner.getPageListFromPageContent(page));
    page = "<table><tr><td>ThisTableIsTooSmall</td></tr></table>";
    assertFalse(runner.getPageListFromPageContent(page));
    page = "<table><tr><td>NotASuite</td></tr><tr><td>But it is</td><td>The right Size</td></tr><tr><td></td></tr></table>";
    assertFalse(runner.getPageListFromPageContent(page));
  }

  @Test
  public void shouldBeAbleToGetTheTitleRegExFromATable() throws Exception {
    String page = "<table><tr><td>Suite</td></tr><tr><td>Page</td><td>TestPageOne</td></tr><tr><td>Title</td><td>ChildPage</td></tr></table>";
    HtmlTableScanner scanner = new HtmlTableScanner(page);
    Table table = scanner.getTable(0);
    assertTrue(runner.getImportantTableInformation(table));
    assertEquals("TestPageOne", runner.searchRoot.getName());
    assertEquals("ChildPage", runner.titleRegEx);
  }

  @Test
  public void shouldBeAbleToGetTheContentRegExFromTable() throws Exception {
    String page = "<table><tr><td>Suite</td></tr><tr><td>Page</td><td>TestPageOne</td></tr><tr><td>Content</td><td>has.*content</td></tr></table>";
    HtmlTableScanner scanner = new HtmlTableScanner(page);
    Table table = scanner.getTable(0);
    assertTrue(runner.getImportantTableInformation(table));
    assertEquals("has.*content", runner.contentRegEx);
  }

  @Test
  public void shouldGetPagesFromPageContent() throws Exception {
    String page = "<table><tr><td>Suite</td></tr><tr><td>Title</td><td>Test</td></tr><tr><td>Content</td><td>has.*content</td></tr></table>";
    assertTrue(runner.getPageListFromPageContent(page));
    assertEquals(2, runner.testPages().size());
  }

  @Test
  public void canGetPagesFromMultipleTables() throws Exception {
    String page = "<table><tr><td>Suite</td></tr><tr><td>Title</td><td>Test</td></tr><tr><td></td><td></td></tr></table>";
    page += "<table><tr><td>Suite</td></tr><tr><td>Content</td><td>child</td></tr><tr><td></td><td></td></tr></table>";
    assertTrue(runner.getPageListFromPageContent(page));
    assertEquals(3, runner.testPages().size());
  }

  @Test
  public void shouldntIncludeSuitesInThePageList() throws Exception {
    WikiPage testSuitePage = WikiPageUtil.addPage(root, PathParser.parse("SuitePageOne"));
    PageData data = testSuitePage.getData();
    data.setAttribute("Suite");
    testSuitePage.commit(data);
    String page = "<table><tr><td>Suite</td></tr><tr><td>Title</td><td>One</td></tr><tr><td>Content</td><td></td></tr></table>";
    assertTrue(runner.getPageListFromPageContent(page));
    assertEquals(1,runner.testPages().size());
  }

  @Test
  public void shouldExcludeSkippedPages() throws Exception {
    prunePage("TestPageTwo");
    runner.titleRegEx = ".*";
    runner.findPageMatches();
    assertEquals(2, runner.testPages().size());
    assertEquals("TestPageOne", runner.testPages().get(0).getName());
    assertEquals("ChildPage", runner.testPages().get(1).getName());
  }

  @Test
  public void shouldExcludeChildrenOfSkippedPages() throws Exception {
    prunePage("TestPageOne");
    runner.titleRegEx = ".*";
    runner.findPageMatches();
    assertEquals(1, runner.testPages().size());
    assertEquals("TestPageTwo", runner.testPages().get(0).getName());
  }

  private void prunePage(String pageName) {
    WikiPage childPage = root.getChildPage(pageName);
    PageData data = childPage.getData();
    data.setAttribute(PageData.PropertyPRUNE);
    childPage.commit(data);
  }
}
