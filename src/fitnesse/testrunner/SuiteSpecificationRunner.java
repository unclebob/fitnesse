package fitnesse.testrunner;

import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.Table;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.PrunedPagePruningStrategy;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.search.SuiteSpecificationMatchFinder;

import java.util.List;

public class SuiteSpecificationRunner {
  public String titleRegEx;
  public String contentRegEx;
  public WikiPage searchRoot;
  public PageCrawler crawler;
  private SuiteSpecificationTraverser traverser = new SuiteSpecificationTraverser();


  public SuiteSpecificationRunner(WikiPage root) {
    searchRoot = root;
    titleRegEx = "";
    contentRegEx = "";
    crawler = root.getPageCrawler();
  }


  public void findPageMatches() {
    SuiteSpecificationMatchFinder finder = new SuiteSpecificationMatchFinder(titleRegEx, contentRegEx, traverser);
    finder.search(searchRoot, new PrunedPagePruningStrategy());
  }


  public boolean getPageListFromPageContent(String pageContent) {
    HtmlTableScanner scanner = new HtmlTableScanner(pageContent);
    for (int tableIndex = 0; tableIndex < scanner.getTableCount(); tableIndex++) {
      Table table = scanner.getTable(tableIndex);
      if (!getPageListFromTable(table))
        return false;
    }
    return true;
  }

  private boolean getPageListFromTable(Table table) {
    if (!getImportantTableInformation(table))
      return false;
    findPageMatches();
    titleRegEx = "";
    contentRegEx = "";
    return true;
  }

  public boolean getImportantTableInformation(Table table) {
    if (!isASuiteSpecificationsTable(table))
      return false;
    for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++)
      getImportantRowInformation(table, rowIndex);
    return true;
  }

  private void getImportantRowInformation(Table table, int rowIndex) {
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

  private void getSearchRoot(Table table, int rowIndex) {
    if (table.getCellContents(1, rowIndex) != null) {
      String searchRootPath = table.getCellContents(1, rowIndex);
      searchRoot = crawler.getPage(PathParser.parse(searchRootPath));
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
    return table.getRowCount() < 3;
  }

  public static boolean isASuiteSpecificationsTable(Table table) {
    return !tableIsTooSmall(table) && table.getCellContents(0, 0).equals("Suite");
  }


  public static boolean isASuiteSpecificationsPage(String page) {
    HtmlTableScanner scanner = new HtmlTableScanner(page);
    if (scanner.getTableCount() > 0) {
      Table table = scanner.getTable(0);
      return isASuiteSpecificationsTable(table);
    }
    return false;
  }

  public List<WikiPage> testPages() {
    return traverser.testPages();
  }
}


