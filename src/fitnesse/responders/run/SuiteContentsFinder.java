package fitnesse.responders.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;

public class SuiteContentsFinder {

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";
  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";
  
  private final WikiPage suitePage;
  private final WikiPage wikiRootPage;
  private final String suiteFilter;

  public SuiteContentsFinder(final WikiPage suitePage, final WikiPage root,
      final String suite) {
    this.suitePage = suitePage;
    this.wikiRootPage = root;
    this.suiteFilter = suite;

  }
  
  public List<WikiPage> makePageListForSingleTest() throws Exception {
    LinkedList<WikiPage> pages = new LinkedList<WikiPage>();
    pages.add(suitePage);
    addSetupAndTeardown(pages);

    return pages;
  }
  
  public List<WikiPage> makePageList() throws Exception {
    LinkedList<WikiPage> pages = getAllPagesToRunForThisSuite();

    if (pages.isEmpty()) {
      String name = new WikiPagePath(suitePage).toString();
      WikiPageDummy dummy = new WikiPageDummy("",
        "|Comment|\n|No test found with suite filter '" + suiteFilter + "' in subwiki !-" + name + "-!!|\n"
      );
      dummy.setParent(wikiRootPage);
      pages.add(dummy);
    }
    return pages;
  }

  private void addSetupAndTeardown(LinkedList<WikiPage> pages) throws Exception {
    WikiPage suiteSetUp = PageCrawlerImpl.getInheritedPage(SUITE_SETUP_NAME, suitePage);
    if (suiteSetUp != null) {
      if (pages.contains(suiteSetUp))
        pages.remove(suiteSetUp);
      pages.addFirst(suiteSetUp);
    }
    WikiPage suiteTearDown = PageCrawlerImpl.getInheritedPage(SUITE_TEARDOWN_NAME, suitePage);
    if (suiteTearDown != null) {
      if (pages.contains(suiteTearDown))
        pages.remove(suiteTearDown);
      pages.addLast(suiteTearDown);
    }
  }


  public LinkedList<WikiPage> getAllPagesToRunForThisSuite() throws Exception {
    LinkedList<WikiPage> pages = getAllTestPagesUnder();
    List<WikiPage> referencedPages = gatherCrossReferencedTestPages();
    pages.addAll(referencedPages);
    addSetupAndTeardown(pages);
    return pages;
  }
  
  private LinkedList<WikiPage> getAllTestPagesUnder() throws Exception {
    LinkedList<WikiPage> testPages = new LinkedList<WikiPage>();
    addTestPagesToList(testPages, suitePage);

    Collections.sort(testPages, new Comparator<WikiPage>() {
      public int compare(WikiPage p1, WikiPage p2) {
        try {
          PageCrawler crawler = p1.getPageCrawler();
          WikiPagePath path1 = crawler.getFullPath(p1);
          WikiPagePath path2 = crawler.getFullPath(p2);

          return path1.compareTo(path2);
        }
        catch (Exception e) {
          e.printStackTrace();
          return 0;
        }
      }
    }
    );

    return testPages;
  }
  
  private void addTestPagesToList(List<WikiPage> testPages, WikiPage context) throws Exception {
    PageData data = context.getData();
    if (!data.hasAttribute(PageData.PropertyPRUNE)) {
      if (data.hasAttribute("Test")) {
        if (belongsToSuite(context)) {
          testPages.add(context);
        }
      }

      ArrayList<WikiPage> children = new ArrayList<WikiPage>();
      children.addAll(context.getChildren());
      if (context.hasExtension(VirtualCouplingExtension.NAME)) {
        VirtualCouplingExtension extension = (VirtualCouplingExtension) context.getExtension(
          VirtualCouplingExtension.NAME
        );
        children.addAll(extension.getVirtualCoupling().getChildren());
      }
      for (WikiPage page : children) {
        addTestPagesToList(testPages, page);
      }
    }
  }
  
  private boolean belongsToSuite(WikiPage context) {
    if ((suiteFilter == null) || (suiteFilter.trim().length() == 0)) {
      return true;
    }
    try {
      String suitesStr = context.getData().getAttribute(PageData.PropertySUITES);
      if (suitesStr != null) {
        StringTokenizer t = new StringTokenizer(suitesStr, ",");
        while (t.hasMoreTokens()) {
          if (t.nextToken().trim().equalsIgnoreCase(suiteFilter)) {
            return true;
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  protected List<WikiPage> gatherCrossReferencedTestPages() throws Exception {
    LinkedList<WikiPage> pages = new LinkedList<WikiPage>();
    PageData data = suitePage.getData();
    List<String> pageReferences = data.getXrefPages();
    PageCrawler crawler = suitePage.getPageCrawler();
    WikiPagePath testPagePath = crawler.getFullPath(suitePage);
    WikiPage parent = crawler.getPage(wikiRootPage, testPagePath.parentPath());
    for (String pageReference : pageReferences) {
      WikiPagePath path = PathParser.parse(pageReference);
      WikiPage referencedPage = crawler.getPage(parent, path);
      if (referencedPage != null)
        pages.add(referencedPage);
    }
    return pages;
  }
}
