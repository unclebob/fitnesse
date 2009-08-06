package fitnesse.responders.run;

import fitnesse.components.SearchObserver;
import fitnesse.components.SuiteSpecificationMatchFinder;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.htmlparser.util.ParserException;

import java.util.LinkedList;

public class SuiteSpecificationRunner implements SearchObserver {
  public String titleRegEx;
  public String contentRegEx;
  public LinkedList<WikiPage> testPageList = new LinkedList<WikiPage>();
  public WikiPage searchRoot;
  private WikiPage root;
  public PageCrawler crawler;


  public SuiteSpecificationRunner(WikiPage root) {
    this.root = root;
    searchRoot = root;
    titleRegEx = "";
    contentRegEx = "";
    crawler = root.getPageCrawler();
  }


  public void findPageMatches() throws Exception {
    SuiteSpecificationMatchFinder finder = new SuiteSpecificationMatchFinder(titleRegEx, contentRegEx, this);
    finder.search(searchRoot);
  }


  public boolean getPageListFromPageContent(String pageContent) throws Exception {
    HtmlTableScanner scanner = new HtmlTableScanner(pageContent);
    for (int tableIndex = 0; tableIndex < scanner.getTableCount(); tableIndex++) {
      Table table = scanner.getTable(tableIndex);
      if (!getPageListFromTable(table))
        return false;
    }
    return true;
  }

  private boolean getPageListFromTable(Table table) throws Exception {
    if (!getImportantTableInformation(table))
      return false;
    findPageMatches();
    titleRegEx = "";
    contentRegEx = "";
    return true;
  }

  public boolean getImportantTableInformation(Table table) throws Exception {
    if (!isASuiteSpecificationsTable(table))
      return false;
    for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++)
      getImportantRowInformation(table, rowIndex);
    return true;
  }

  private void getImportantRowInformation(Table table, int rowIndex) throws Exception {
    String cellContent = table.getCellContents(0, rowIndex);
    if (isPageRootRow(cellContent))
      getSearchRoot(table, rowIndex);
    if (isTitleRegExRow(cellContent))
      setTitleRegEx(table, rowIndex);
    if (isContentRegExRow(cellContent))
      setContentRegEx(table, rowIndex);
  }

  private boolean isPageRootRow(String cellContent) {
    return cellContent != null && cellContent.equals("Page");
  }

  private void getSearchRoot(Table table, int rowIndex) throws Exception {
    if (table.getCellContents(1, rowIndex) != null) {
      String searchRootPath = table.getCellContents(1, rowIndex);
      searchRoot = crawler.getPage(root, PathParser.parse(searchRootPath));
    }
  }

  private void setContentRegEx(Table table, int rowIndex) {
    if (table.getCellContents(1, rowIndex) != null) {
      contentRegEx = table.getCellContents(1, rowIndex);
    }
  }

  private boolean isContentRegExRow(String cellContent) {
    return cellContent != null && cellContent.equals("Content");
  }

  private void setTitleRegEx(Table table, int rowIndex) {
    if (table.getCellContents(1, rowIndex) != null) {
      titleRegEx = table.getCellContents(1, rowIndex);
    }
  }

  private boolean isTitleRegExRow(String cellContent) {
    return cellContent != null && cellContent.equals("Title");
  }


  private static boolean tableIsTooSmall(Table table) {
    if (table.getRowCount() < 3)
      return true;
    return false;
  }

  public static boolean isASuiteSpecificationsTable(Table table) {
    return !tableIsTooSmall(table) && table.getCellContents(0, 0).equals("Suite");
  }


  public void hit(WikiPage page) throws Exception {
    for (WikiPage hit : testPageList) {
      if (hit == page)
        return;
    }
    if (page.getData().hasAttribute("Test"))
      testPageList.add(page);
  }

  public static boolean isASuiteSpecificationsPage(String page) throws ParserException {
    HtmlTableScanner scanner = new HtmlTableScanner(page);
    if (scanner.getTableCount() > 0) {
      Table table = scanner.getTable(0);
      return isASuiteSpecificationsTable(table);
    }
    return false;
  }
}
