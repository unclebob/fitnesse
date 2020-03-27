package fitnesse.testrunner.run;

import fitnesse.testrunner.SuiteContentsFinder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PageListTestBase {
  private String suiteContent;

  protected Map<String, String> customProperties = new HashMap<>();
  protected WikiPage root;
  protected WikiPage suite;
  protected WikiPage testPage;

  public PageListTestBase() {
    this("The is the test suite\n");
  }

  public PageListTestBase(String suiteContent) {
    this.suiteContent = suiteContent;
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT", customProperties);
    PageData data = root.getData();
    root.commit(data);
    suite = addChildPage(root, "SuitePageName", suiteContent);
    testPage = addChildPage(suite, "TestPage", "My test and has some content");
  }

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
