package fitnesse.junit;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.reporting.BaseFormatter;
import fitnesse.reporting.FormatterException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import util.FileUtil;

/**
 * Used to run tests from a JUnit test suite.
 *
 * @see fitnesse.junit.FitNesseRunner
 */
public class JavaFormatter extends BaseFormatter implements Closeable {

  private String mainPageName;
  private boolean isSuite = true;


  public interface ResultsRepository extends Closeable {
    void open(String string) throws IOException;

    void write(String content) throws IOException;
  }

  public static class FileCopier {
    public static void copy(String src, File dst) throws IOException {
      InputStream in = null;
      OutputStream out = null;
      try {
        in = FileCopier.class.getResourceAsStream(src);
        out = new FileOutputStream(dst);
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } finally {
        FileUtil.close(in);
        FileUtil.close(out);
      }
    }
  }

  public static class TestResultPage {
    private OutputStreamWriter currentWriter;

    public TestResultPage(String outputPath, String testName) throws IOException {
      File outputFile = new File(outputPath, testName + ".html");
      currentWriter = new OutputStreamWriter(new FileOutputStream(outputFile), FileUtil.CHARENCODING);
      writeHeaderFor(testName);
    }

    public void appendResultChunk(String content) throws IOException {
      currentWriter.write(content.replace("src=\"/files/images/", "src=\"images/"));
    }

    private void writeHeaderFor(String testName) throws IOException {
      currentWriter.write("<html><head><title>");
      currentWriter.write(testName);
      currentWriter
        .write("</title><meta http-equiv='Content-Type' content='text/html;charset=" + FileUtil.CHARENCODING + "'/>"
          + "<link rel='stylesheet' type='text/css' href='css/fitnesse.css'/>"
          + "<script src='javascript/jquery-1.11.3.min.js' type='text/javascript'></script>"
          + "<script src='javascript/fitnesse.js' type='text/javascript'></script>" + "</head><body><header><h2>");
      currentWriter.write(testName);
      currentWriter.write("</h2></header><article>");
    }

    public void finish() throws IOException {
      if (currentWriter != null) {
        currentWriter.write("</article></body></html>");
        currentWriter.close();
      }
    }
  }

  public static class FolderResultsRepository implements ResultsRepository {
    private String outputPath;
    private TestResultPage testResultPage;

    public FolderResultsRepository(String outputPath) throws IOException {
      this.outputPath = outputPath;
      copyAssets();
    }

    @Override
    public void close() throws IOException {
      testResultPage.finish();
    }

    @Override
    public void open(String testName) throws IOException {
      testResultPage = new TestResultPage(outputPath, testName);
    }

    @Override
    public void write(String content) throws IOException {
      testResultPage.appendResultChunk(content);
    }

    public void addFile(String resource, String relativeFilePath) throws IOException {
      File dst = new File(outputPath, relativeFilePath);
      dst.getParentFile().mkdirs();
      FileCopier.copy(resource, dst);
    }

    private void copyAssets() throws IOException {
      String base = "/fitnesse/resources/";
      String cssDir = base + "css/";
      addFile(cssDir + "fitnesse.css", "css/fitnesse.css");
      addFile(cssDir + "fitnesse_wiki.css", "css/fitnesse_wiki.css");
      addFile(cssDir + "fitnesse_pages.css", "css/fitnesse_pages.css");
      addFile(cssDir + "fitnesse_straight.css", "css/fitnesse_straight.css");
      String javascriptDir = base + "javascript/";
      addFile(javascriptDir + "jquery-1.11.3.min.js", "javascript/jquery-1.11.3.min.js");
      addFile(javascriptDir + "fitnesse.js", "javascript/fitnesse.js");
      String imagesDir = base + "images/";
      addFile(imagesDir + "collapsibleOpen.png", "images/collapsibleOpen.png");
      addFile(imagesDir + "collapsibleClosed.png", "images/collapsibleClosed.png");
    }
  }

  private TestSummary totalSummary = new TestSummary();

  private List<String> visitedTestPages = new ArrayList<>();
  private Map<String, TestSummary> testSummaries = new HashMap<>();

  @Override
  public void testStarted(TestPage test) {
    try {
      resultsRepository.open(test.getFullPath());
    } catch (IOException e) {
      throw new FormatterException("Could not open new report file for " + test.getFullPath(), e);
    }
  }

  @Override
  public void testComplete(TestPage test, TestSummary testSummary) {
    String fullPath = test.getFullPath();
    visitedTestPages.add(fullPath);
    totalSummary.add(testSummary);
    testSummaries.put(fullPath, new TestSummary(testSummary));
    FileUtil.close(resultsRepository);
    isSuite = isSuite && (!mainPageName.equals(fullPath));
  }

  TestSummary getTestSummary(String testPath) {
    return testSummaries.get(testPath);
  }

  @Override
  public void testOutputChunk(String output) {
    try {
      resultsRepository.write(output);
    } catch (IOException e) {
      throw new FormatterException("could not write output chunk", e);
    }
  }

  private ResultsRepository resultsRepository;

  public TestSummary getTotalSummary() {
    return totalSummary;
  }

  public void setTotalSummary(TestSummary testSummary) {
    totalSummary = testSummary;
  }

  public void setResultsRepository(ResultsRepository mockResultsRepository) {
    this.resultsRepository = mockResultsRepository;
  }

  /**
   * package-private to prevent instantiation apart from getInstance and tests
   */
  JavaFormatter(String suiteName) {
    this.mainPageName = suiteName;
  }

  @Override
  public void close() throws IOException {
    if (isSuite)
      writeSummary(mainPageName);
  }

  public void writeSummary(String suiteName) throws IOException {
    resultsRepository.open(suiteName);
    resultsRepository.write(new TestResultsSummaryTable(visitedTestPages, testSummaries).toString());
    resultsRepository.close();
  }

  public static class TestResultsSummaryTableRow {
    private String testName;
    private TestSummary testSummary;

    public TestResultsSummaryTableRow(String testName, TestSummary testSummary) {
      this.testName = testName;
      this.testSummary = testSummary;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("<tr class=\"").append(getCssClass(testSummary)).append("\"><td>").append(
              "<a href=\"").append(testName).append(".html\">").append(testName).append("</a>").append(
              "</td><td>").append(testSummary.getRight()).append("</td><td>").append(testSummary.getWrong())
              .append("</td><td>").append(testSummary.getExceptions()).append("</td></tr>");
      return sb.toString();
    }

    private String getCssClass(TestSummary ts) {
      if (ts.getExceptions() > 0)
        return "error";
      if (ts.getWrong() > 0)
        return "fail";
      if (ts.getRight() > 0)
        return "pass";
      return "plain";
    }
  }

  public static class TestResultsSummaryTable {
    public static final String SUMMARY_FOOTER = "</table>";
    public static final String SUMMARY_HEADER = "<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td></tr>";
    private List<String> visitedTestPages;
    private Map<String, TestSummary> testSummaries;

    public TestResultsSummaryTable(List<String> visitedTestPages, Map<String, TestSummary> testSummaries) {
      this.visitedTestPages = visitedTestPages;
      this.testSummaries = testSummaries;
    }

    public String summaryRow(String testName, TestSummary testSummary) {
      return new TestResultsSummaryTableRow(testName, testSummary).toString();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(SUMMARY_HEADER);
      for (String s : visitedTestPages) {
        sb.append(summaryRow(s, testSummaries.get(s)));
      }
      sb.append(SUMMARY_FOOTER);
      return sb.toString();
    }
  }
}
