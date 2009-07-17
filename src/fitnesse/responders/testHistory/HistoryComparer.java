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
  public static String[] resultContent;
  public HtmlTableScanner firstScanner;
  public HtmlTableScanner secondScanner;

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
    return grabAndCompareTablesFromHtml();
  }

  public boolean grabAndCompareTablesFromHtml() throws ParserException {
    firstScanner = new HtmlTableScanner(firstFileContent);
    secondScanner = new HtmlTableScanner(secondFileContent);
    if (firstScanner.getTableCount() == 0 || secondScanner.getTableCount() == 0)
      return false;
    int numTables = firstScanner.getTableCount() >= secondScanner.getTableCount() ? secondScanner.getTableCount() : firstScanner.getTableCount();
    resultContent = new String[numTables];
    for (int i = 0; i < numTables; i++){
      resultContent[i] = "pass";
      compareTables(firstScanner.getTable(i), secondScanner.getTable(i),i);
    }
    return true;
  }

  private void initializeFileContents(String firstFilePath, String secondFilePath) {
    String content = getFileContent(firstFilePath);
    firstFileContent = content == null ? "" : content;
    content = getFileContent(secondFilePath);
    secondFileContent = content == null ? "" : content;
  }

  public String[] getResultContent() {
    return resultContent;
  }

  public static boolean compareTables(Table table1, Table table2, int tableNum) {
    if(table1.getRowCount() != table2.getRowCount())
      return tablesDiffer(tableNum);
    for (int i = 0; i < table1.getRowCount(); i++) {
      if(table1.getColumnCountInRow(i) != table2.getColumnCountInRow(i))
        return tablesDiffer(tableNum);
      for (int j = 0; j < table1.getColumnCountInRow(i); j++) {
        String content1 = table1.getCellResult(j, i);
        String content2 = table2.getCellResult(j, i);
        if (!content1.equals(content2))
          tablesDiffer(tableNum);
      }
    }
    return resultContent[tableNum] != "fail";
  }

  private static boolean tablesDiffer(int i) {
    resultContent[i] = "fail";
    return false;
  }
}
