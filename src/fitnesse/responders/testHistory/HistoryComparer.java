package fitnesse.responders.testHistory;

import fitnesse.responders.run.TestExecutionReport;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;
import org.htmlparser.util.ParserException;

import java.io.File;

public class HistoryComparer {
  public String secondFileContent = "";
  public String firstFileContent = "";
  public File resultFile;
  public static String resultContent;

  public String getFileContent(String filePath) {
    TestExecutionReport report;
    try {
      report = new TestExecutionReport(new File(filePath));
      if (report.getResults().size() != 1)
        return null;
      return report.getResults().get(0).getContent();
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  public boolean compare(String firstFilePath, String secondFilePath) throws Exception {
    if (firstFilePath.equals(secondFilePath))
      return false;
    initializeFileContents(firstFilePath, secondFilePath);
    resultContent = "pass";
    return grabAndCompareTablesFromHtml();
  }

  private boolean grabAndCompareTablesFromHtml() throws ParserException {
    HtmlTableScanner firstScanner = new HtmlTableScanner(firstFileContent);
    HtmlTableScanner secondScanner = new HtmlTableScanner(secondFileContent);
    if (firstScanner.getTableCount() == 0 || secondScanner.getTableCount() == 0)
      return false;
    int numTables = firstScanner.getTableCount() >= secondScanner.getTableCount() ? secondScanner.getTableCount() : firstScanner.getTableCount();
    for (int i = 0; i < numTables; i++)
      compareTables(firstScanner.getTable(i), secondScanner.getTable(i));
    return true;
  }

  private void initializeFileContents(String firstFilePath, String secondFilePath) {
    String content = getFileContent(firstFilePath);
    firstFileContent = content == null ? "" : content;
    content = getFileContent(secondFilePath);
    secondFileContent = content == null ? "" : content;
  }

  public String getResultContent() {
    return resultContent;
  }

  public static boolean compareTables(Table table1, Table table2) {
    //int numRows = table1.getRowCount() >= table2.getRowCount() ? table2.getRowCount() : table1.getRowCount();
    if(table1.getRowCount() != table2.getRowCount())
      return tablesDiffer();
    for (int i = 0; i < table1.getRowCount(); i++) {
      if(table1.getColumnCountInRow(i) != table2.getColumnCountInRow(i))
        return tablesDiffer();
      for (int j = 0; j < table1.getColumnCountInRow(i); j++) {
        String content1 = table1.getCellResult(j, i);
        String content2 = table2.getCellResult(j, i);
        if (!content1.equals(content2))
          tablesDiffer();
      }
    }
    return resultContent != "fail";
  }

  private static boolean tablesDiffer() {
    resultContent = "fail";
    return false;
  }
}
