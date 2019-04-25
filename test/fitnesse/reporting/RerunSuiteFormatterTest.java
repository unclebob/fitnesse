package fitnesse.reporting;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class RerunSuiteFormatterTest {
  private final WikiPage wikiPageRoot = InMemoryPage.makeRoot("root");
  private final WikiPage wikiSuitePage = WikiPageUtil.addPage(wikiPageRoot, PathParser.parse("Suite"), "suite content");

  private File targetFile;
  private RerunSuiteFormatter formatter;

  @Before
  public void setUp() throws IOException {
    targetFile = File.createTempFile("root", "rerun.wiki");
    formatter = new RerunSuiteFormatter(targetFile);
  }

  @After
  public void tearDown() {
    if (targetFile != null && targetFile.exists()) {
      targetFile.delete();
    }
  }

  @Test
  public void noErrorOrFailedNoFile() throws IOException {
    sendTestComplete(ExecutionResult.PASS, "PassedPage");
    formatter.close();

    assertEquals(0, formatter.getErrorCount());

    assertFalse(targetFile.exists());
  }

  @Test
  public void addRefOnError() throws IOException {
    sendTestComplete(ExecutionResult.ERROR, "ErrorPage");
    formatter.close();

    assertEquals(1, formatter.getErrorCount());

    List<String> xrefs = getReferencedPages();
    assertEquals(".Suite.ErrorPage", xrefs.get(0));
  }

  @Test
  public void addRefOnFailure() throws IOException {
    sendTestComplete(ExecutionResult.FAIL, "FailedPage");
    formatter.close();

    assertEquals(1, formatter.getErrorCount());

    List<String> xrefs = getReferencedPages();
    assertEquals(".Suite.FailedPage", xrefs.get(0));
  }

  @Test
  public void addRefsOnMultiple() throws IOException {
    sendTestComplete(ExecutionResult.FAIL, "FailedPage");
    sendTestComplete(ExecutionResult.PASS, "PassedPage");
    sendTestComplete(ExecutionResult.ERROR, "ErrorPage");
    sendTestComplete(ExecutionResult.FAIL, "FailedPage2");
    formatter.close();

    assertEquals(3, formatter.getErrorCount());

    List<String> xrefs = getReferencedPages();
    assertEquals(".Suite.FailedPage", xrefs.get(0));
    assertEquals(".Suite.ErrorPage", xrefs.get(1));
    assertEquals(".Suite.FailedPage2", xrefs.get(2));
  }

  @Test
  public void addRefPagenameNotWikiWord() throws IOException {
    sendTestComplete(ExecutionResult.FAIL, "Failedpage");
    formatter.close();

    assertEquals(1, formatter.getErrorCount());

    List<String> xrefs = getReferencedPages();
    assertEquals(".Suite.Failedpage", xrefs.get(0));
  }

  @Test
  public void suiteSetUpAndSuiteTearDownAreIgnored() throws IOException {
    sendTestComplete(ExecutionResult.ERROR, "SuiteSetUp");
    sendTestComplete(ExecutionResult.ERROR, "ErrorPage");
    sendTestComplete(ExecutionResult.ERROR, "SuiteTearDown");
    formatter.close();

    assertEquals(1, formatter.getErrorCount());

    List<String> xrefs = getReferencedPages();
    assertEquals(".Suite.ErrorPage", xrefs.get(0));
  }

  private List<String> getReferencedPages() throws IOException {
    String rerunPageContent = FileUtil.getFileContent(targetFile);
    assertTrue(rerunPageContent.startsWith("---\nHelp: "));

    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageName"), rerunPageContent);
    return WikiPageUtil.getXrefPages(page);
  }

  private void sendTestComplete(ExecutionResult result, String pageName) {
    WikiPage wikiTestPage = WikiPageUtil.addPage(wikiSuitePage, PathParser.parse(pageName), "content");
    formatter.testComplete(new WikiTestPage(wikiTestPage), createTestSummary(result));
  }

  private TestSummary createTestSummary(ExecutionResult fail) {
    TestSummary testSummary = new TestSummary();
    testSummary.add(fail);
    return testSummary;
  }
}
