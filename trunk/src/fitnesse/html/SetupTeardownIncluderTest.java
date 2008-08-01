package fitnesse.html;

import fitnesse.testutil.RegexTestCase;
import fitnesse.wiki.*;

public class SetupTeardownIncluderTest extends RegexTestCase {
  private PageData pageData;

  protected void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageCrawler crawler = root.getPageCrawler();
    WikiPage page = crawler.addPage(root,
      PathParser.parse("TestPage"),
      "the content"
    );
    crawler.addPage(root, PathParser.parse("SetUp"), "setup");
    crawler.addPage(root, PathParser.parse("TearDown"), "teardown");
    crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suiteSetUp");
    crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suiteTearDown");
    pageData = page.getData();
  }

  public void testIncludeSetupTearDownOutsideOfSuite()
    throws Exception {
    String html = SetupTeardownIncluder.render(pageData);
    assertSubString(".SetUp", html);
    assertSubString("setup", html);
    assertSubString(".TearDown", html);
    assertSubString("teardown", html);
    assertSubString("the content", html);
    assertSubString("class=\"collapsable\"", html);
    assertNotSubString(".SuiteSetUp", html);
    assertNotSubString("suiteSetUp", html);
    assertNotSubString(".SuiteTearDown", html);
    assertNotSubString("suitTearDown", html);
  }

  public void testIncludeSetupTearDownInsideOfSuite() throws Exception {
    String html = SetupTeardownIncluder.render(pageData, true);
    assertSubString(".SetUp", html);
    assertSubString("setup", html);
    assertSubString(".TearDown", html);
    assertSubString("teardown", html);
    assertSubString("the content", html);
    assertSubString("class=\"collapsable\"", html);
    assertSubString(".SuiteSetUp", html);
    assertSubString("suiteSetUp", html);
    assertSubString(".SuiteTearDown", html);
    assertSubString("suiteTearDown", html);
  }
}
