/** this class is adapted from the trinidad project (http://fitnesse.info/trinidad) */

package fitnesse.trinidad;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fitnesse.ComponentFactory;
import fitnesse.FitNesseContext;
import fitnesse.WikiPageFactory;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.responders.run.SuiteContentsFinder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualEnabledPageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class FitNesseRepository implements TestRepository {
  private FitNesseContext context;
  private String fitnesseRoot;
  public static final String SUITE_SETUP_NAME = "SuiteSetUp";
  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

  public FitNesseRepository() {
  }

  public void setUri(String uri) throws IOException {
    context = makeContext(uri);
    fitnesseRoot = uri;
  }

  public FitNesseRepository(String rootDir) throws IOException {
    setUri(rootDir);
  }

  public void prepareResultRepository(TestResultRepository resultRepository)
      throws IOException {
    File filesFolder = new File(
        new File(new File(fitnesseRoot), "FitNesseRoot"), "files");
    File cssDir = new File(filesFolder, "css");
    resultRepository.addFile(new File(cssDir, "fitnesse_base.css"),
        "fitnesse.css");
    File javascriptDir = new File(filesFolder, "javascript");
    resultRepository.addFile(new File(javascriptDir, "fitnesse.js"),
        "fitnesse.js");
    File imagesDir = new File(filesFolder, "images");
    resultRepository.addFile(new File(imagesDir, "collapsableOpen.gif"),
        "images/collapsableOpen.gif");
    resultRepository.addFile(new File(imagesDir, "collapsableClosed.gif"),
        "images/collapsableClosed.gif");
  }

  public List<TestDescriptor> getSuite(String name) throws IOException {
    try {
      List<TestDescriptor> tests = new ArrayList<TestDescriptor>();
      WikiPagePath path = PathParser.parse(name);
      PageCrawler crawler = context.root.getPageCrawler();
      crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
      WikiPage suiteRoot = crawler.getPage(context.root, path);
      if (!suiteRoot.getData().hasAttribute("Suite")) {
        throw new IllegalArgumentException("page " + name + " is not a suite");
      }
      WikiPage root = crawler.getPage(context.root, PathParser.parse("."));

      SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(suiteRoot,
        null, root);
      List<WikiPage> pages = suiteTestFinder.getAllPagesToRunForThisSuite();

      // List<WikiPage> pages=SuiteResponder.makePageList(suiteRoot,root,null);
      for (WikiPage p : pages) {
        if (p.getData().hasAttribute("Test") || isSuiteSetUpOrTearDown(p)) {
          String testName = crawler.getFullPath(p).toString();
          String content = formatWikiPage(testName, p);
          tests.add(new InMemoryTestImpl(testName, content));
        }
      }
      return tests;
    } catch (Exception e) {
      IOException ioe = new IOException("error reading suite " + name);
      ioe.initCause(e);
      throw ioe;
    }
  }

  private boolean isSuiteSetUpOrTearDown(WikiPage p) throws Exception {
    return p.getName().equals("SuiteSetUp")
        || p.getName().equals("SuiteTearDown")
        || p.getName().endsWith(".SuiteSetUp")
        || p.getName().endsWith(".SuiteTearDown");
  }

  // WikiPage suiteSetUp = PageCrawlerImpl.getInheritedPage(SUITE_SETUP_NAME,
  // suitePage);
  // if (suiteSetUp != null) {
  // if (pages.contains(suiteSetUp))
  // pages.remove(suiteSetUp);
  // pages.addFirst(suiteSetUp);
  // }
  // WikiPage suiteTearDown =
  // PageCrawlerImpl.getInheritedPage(SUITE_TEARDOWN_NAME, suitePage);
  // if (suiteTearDown != null) {
  // if (pages.contains(suiteTearDown))
  // pages.remove(suiteTearDown);
  // pages.addLast(suiteTearDown);
  // }

  public TestDescriptor getTest(String name) throws IOException {
    try {
      WikiPagePath path = PathParser.parse(name);
      PageCrawler crawler = context.root.getPageCrawler();
      WikiPage page = crawler.getPage(context.root, path);
      if (page == null)
        throw new Error("Test " + name + " not found!");
      WikiPage suiteSetUp = PageCrawlerImpl.getClosestInheritedPage(
          SUITE_SETUP_NAME, page);
      WikiPage suiteTearDown = PageCrawlerImpl.getClosestInheritedPage(
          SUITE_TEARDOWN_NAME, page);
      String content = formatWikiPage(name, page, suiteSetUp, suiteTearDown);
      return new InMemoryTestImpl(name, content);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new IOException("Error reading test " + name + " " + ex);
    }
  }

  private String formatWikiPage(String name, WikiPage page) throws Exception {
    return formatWikiPage(name, page, null, null);
  }

  private String formatWikiPage(String name, WikiPage page,
      WikiPage suiteSetUp, WikiPage suiteTearDown) throws Exception {
    PageData pd = page.getData();
    SetupTeardownIncluder.includeInto(pd);
    HtmlPage html = context.htmlPageFactory.newPage();
    html.title.use(name);
    html.header.use(name);

    StringBuffer content = new StringBuffer();
    content.append(pd.getHeaderPageHtml());
    if (suiteSetUp != null)
      content.append(suiteSetUp.getData().getHtml());
    content.append(pd.getHtml());
    if (suiteTearDown != null)
      content.append(suiteTearDown.getData().getHtml());
    pd.setContent(content.toString());
    content.append(pd.getFooterPageHtml());
    html.main.use(content.toString());
    String result = html.html();
    result = result.replace("href=\"/files/css/", "href=\"");
    result = result.replaceAll("/files/javascript/", "");
    result = result.replaceAll("/files/images/", "images/");
    return result;
  }

  private FitNesseContext makeContext(String rootPath) throws IOException {
    try {
      // this should ideally execute FitNesseMain.loadContext
      FitNesseContext context = new FitNesseContext();
      context.rootPath = rootPath;
      ComponentFactory componentFactory = new ComponentFactory(context.rootPath);
      context.rootDirectoryName = "FitNesseRoot";
      context.setRootPagePath();
      String defaultNewPageContent = componentFactory
          .getProperty(ComponentFactory.DEFAULT_NEWPAGE_CONTENT);
      if (defaultNewPageContent != null)
        context.defaultNewPageContent = defaultNewPageContent;
      WikiPageFactory wikiPageFactory = new WikiPageFactory();
      context.responderFactory = new ResponderFactory(context.rootPagePath);
      context.htmlPageFactory = componentFactory
          .getHtmlPageFactory(new HtmlPageFactory());
      context.root = wikiPageFactory.makeRootPage(context.rootPath,
          context.rootDirectoryName, componentFactory);

      WikiImportTestEventListener.register();

      return context;

    } catch (Exception e) {
      IOException ioe = new IOException(rootPath
          + " is not a fitnesse root url");
      ioe.initCause(e);
      throw ioe;
    }
  }

}
