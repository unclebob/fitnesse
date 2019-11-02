package fitnesse.reporting;

import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Creates a wiki page with all pages that either failed or threw exception
 */
public class RerunSuiteFormatter extends BaseFormatter implements Closeable {
  private static final Logger LOG = Logger.getLogger(RerunSuiteFormatter.class.getName());

  private final File wikiFile;
  private final PrintWriter pw;
  private int errorCount = 0;

  public RerunSuiteFormatter(File targetFile) throws IOException {
    wikiFile = targetFile;
    if (!wikiFile.getParentFile().exists()) {
      wikiFile.getParentFile().mkdirs();
    } else if (wikiFile.exists()) {
      wikiFile.delete();
    }
    LOG.fine("Rerun suite will be made in: " + wikiFile.getAbsolutePath());
    pw = new PrintWriter(wikiFile, "utf-8");
  }

  @Override
  public void testComplete(TestPage testPage, TestSummary testSummary) {
    if (testSummary.getExceptions() > 0) {
      recordFailure(testPage);
    } else if (testSummary.getWrong() > 0) {
      recordFailure(testPage);
    }
  }

  @Override
  public int getErrorCount() {
    return errorCount;
  }

  @Override
  public void close() {
    pw.close();
    // no content -> remove file
    if (getErrorCount() == 0 && wikiFile.exists()) {
      wikiFile.delete();
    }
  }

  protected void recordFailure(TestPage testPage) {
    String testPageName = testPage.getName();
    if (!"SuiteSetUp".equals(testPageName)
      && !"SuiteTearDown".equals(testPageName)) {
      errorCount++;
      if (errorCount == 1) {
        appendHeader(pw);
      }
      appendPageFailure(pw, testPage);
      pw.flush();
    }
  }

  protected void appendHeader(PrintWriter writer) {
    writer.append("---\n" +
      "Help: Lists tests failed during last run, so they can be run again (without running all tests that passed).\n" +
      "Suite\n" +
      "---\n" +
      "\n");
    writer.append("!note This page is automatically generated when running tests. ");
    writer.append("It will be overwritten by the next Suite or Test execution.\n\n");
    writer.append("Tests failed (first failure was at ");
    writer.append(LocalDateTime.now().toString());
    writer.append("):\n\n");
  }

  protected void appendPageFailure(PrintWriter writer, TestPage testPage) {
    String pagePath = testPage.getFullPath();
    writer.append("!see [[");
    writer.append(pagePath);
    writer.append("][.");
    writer.append(pagePath);
    writer.append("]]\n");
  }
}
