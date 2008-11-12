// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;
import fitnesse.responders.run.FitClientResponderTest;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.responders.run.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import static fitnesse.testutil.RegexTestCase.*;
import fitnesse.util.FileUtil;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class TestRunnerTest {
  private int port;
  private WikiPage root;
  private TestRunner runner;
  private MockResultFormatter mockHandler;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    FitClientResponderTest.buildSuite(root);

    FitNesseUtil.startFitnesse(root);
    port = FitNesseUtil.port;

    runner = new TestRunner(new PrintStream(new ByteArrayOutputStream()));
    mockHandler = new MockResultFormatter();
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
    FileUtil.deleteFile("testFile.txt");
  }

  @Test
  public void testOnePassingTest() throws Exception {
    testPageResults("SuitePage.TestPassing", new Counts(1, 0, 0, 0), 0);
  }

  @Test
  public void testOneFailingTest() throws Exception {
    testPageResults("SuitePage.TestFailing", new Counts(0, 1, 0, 0), 1);
  }

  @Test
  public void testOneErrorTest() throws Exception {
    testPageResults("SuitePage.TestError", new Counts(0, 0, 0, 1), 1);
  }

  @Test
  public void testOneIgnoreTest() throws Exception {
    testPageResults("SuitePage.TestIgnore", new Counts(0, 0, 1, 0), 0);
  }

  @Test
  public void testSuite() throws Exception {
    testPageResults("SuitePage", new Counts(1, 1, 1, 1), 2);
  }

  @Test
  public void testResultContentWithOneTest() throws Exception {
    runner.handler.addHandler(mockHandler);
    runPage("SuitePage.TestPassing");
    List<PageResult> results = mockHandler.results;
    assertEquals(1, results.size());
    Object result1 = results.get(0);
    assertTrue(result1 instanceof PageResult);
    PageResult pageResult = (PageResult) result1;
    assertSubString("PassFixture", pageResult.content());
    assertEquals("", pageResult.title());
    assertEquals(new TestSummary(1, 0, 0, 0), pageResult.testSummary());
  }

  @Test
  public void testResultContentWithSuite() throws Exception {
    runner.handler.addHandler(mockHandler);
    runPage("SuitePage");
    List<PageResult> results = mockHandler.results;
    assertEquals(4, results.size());

    checkResult(results, 0, "TestError", new TestSummary(0, 0, 0, 1), "ErrorFixture");
    checkResult(results, 1, "TestFailing", new TestSummary(0, 1, 0, 0), "FailFixture");
    checkResult(results, 2, "TestIgnore", new TestSummary(0, 0, 1, 0), "IgnoreFixture");
    checkResult(results, 3, "TestPassing", new TestSummary(1, 0, 0, 0), "PassFixture");
  }

  @Test
  public void testKeepResultFile() throws Exception {
    runPage("-results testFile.txt", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertSubString("TestError", content);
    assertSubString("TestPassing", content);
    assertSubString("TestFailing", content);
    assertSubString("TestIgnore", content);
  }

  @Test
  public void testHtmlFile() throws Exception {
    runPage("-html testFile.txt", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertSubString("<span class=\"test_summary_results pass\">1 right, 0 wrong, 0 ignored, 0 exceptions</span>", content);
    assertSubString("<td class=\"ignore\">fitnesse.testutil.IgnoreFixture</td>", content);
    assertSubString("<td>fitnesse.testutil.ErrorFixture</td>", content);
    assertSubString("<td>fitnesse.testutil.FailFixture</td>", content);
    assertSubString("<tr><td>fitnesse.testutil.PassFixture</td>", content);
  }

  @Test
  public void testXMLFile() throws Exception {
    runPage("-xml testFile.txt", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String xmlContent = FileUtil.getFileContent("testFile.txt");
    Document testResultsDocument = XmlUtil.newDocument(xmlContent);
    Element testResultsElement = testResultsDocument.getDocumentElement();
    assertEquals("testResults", testResultsElement.getNodeName());
    assertEquals("localhost:1999", XmlUtil.getTextValue(testResultsElement, "host"));
    assertEquals("SuitePage", XmlUtil.getTextValue(testResultsElement, "rootPath"));

    Element finalCounts = XmlUtil.getElementByTagName(testResultsElement, "finalCounts");
    assertCounts(finalCounts, "1", "1", "1", "1");

    NodeList results = testResultsElement.getElementsByTagName("result");
    assertEquals(4, results.getLength());

    assertResult(results.item(0), "TestError", "fitnesse.testutil.ErrorFixture", "0", "0", "0", "1");
    assertResult(results.item(1), "TestFailing", "fitnesse.testutil.FailFixture", "0", "1", "0", "0");
    assertResult(results.item(2), "TestIgnore", "fitnesse.testutil.IgnoreFixture", "0", "0", "1", "0");
    assertResult(results.item(3), "TestPassing", "fitnesse.testutil.PassFixture", "1", "0", "0", "0");
  }

  private void assertResult(
    Node result, String pageName, String fixtureName,
    String right, String wrong, String ignores, String exceptions
  ) throws Exception {
    Element resultElement = (Element) result;
    assertEquals(pageName, XmlUtil.getTextValue(resultElement, "relativePageName"));
    String content = XmlUtil.getTextValue(resultElement, "content");
    assertSubString(fixtureName, content);
    Element counts = XmlUtil.getElementByTagName(resultElement, "counts");
    assertCounts(counts, right, wrong, ignores, exceptions);
  }

  private void assertCounts(Element counts, String right, String wrong, String ignores, String exceptions)
    throws Exception {
    assertEquals(right, XmlUtil.getTextValue(counts, "right"));
    assertEquals(wrong, XmlUtil.getTextValue(counts, "wrong"));
    assertEquals(ignores, XmlUtil.getTextValue(counts, "ignores"));
    assertEquals(exceptions, XmlUtil.getTextValue(counts, "exceptions"));
  }

  @Test
  public void testSuiteSetUpAndTearDownIsCalledIfSingleTestIsRun() throws Exception {
    addSuiteSetUpTearDown();
    runner.handler.addHandler(mockHandler);

    runPage("SuitePage.TestPassing");

    List<PageResult> results = mockHandler.results;
    assertEquals(1, results.size());
    checkResult(results, 0, "", new TestSummary(3, 0, 0, 0), "PassFixture");
    PageResult result = results.get(0);
    String content = result.content();
    assertSubString("SuiteSetUp", content);
    assertSubString("SuiteTearDown", content);
  }

  private void addSuiteSetUpTearDown() throws Exception {
    PageCrawler crawler = root.getPageCrawler();
    WikiPage suitePage = crawler.getPage(root, PathParser.parse("SuitePage"));
    crawler.addPage(suitePage, PathParser.parse(SuiteResponder.SUITE_SETUP_NAME), "!|fitnesse.testutil.PassFixture|\n");
    crawler.addPage(suitePage, PathParser.parse(SuiteResponder.SUITE_TEARDOWN_NAME),
      "!|fitnesse.testutil.PassFixture|\n"
    );
  }

  private void checkResult(List<PageResult> results, int i, String s, TestSummary expectedSummary, String content) {
    PageResult result = results.get(i);
    assertEquals(s, result.title());
    TestSummary resultSummary = result.testSummary();
    assertEquals(expectedSummary, resultSummary);
    assertSubString(content, result.content());
  }

  private void testPageResults(String pageName, Counts expectedCounts, int exitCode) throws Exception {
    runPage(pageName);
    Counts counts = runner.getCounts();
    assertEquals(expectedCounts, counts);
    assertEquals(exitCode, runner.exitCode());
  }

  private void runPage(String pageName) throws Exception {
    runPage("", pageName);
  }

  private void runPage(String options, String pageName) throws Exception {
    options += " localhost " + port + " " + pageName;
    String[] args = options.trim().split(" ");
    runner.run(args);
  }

  @Test
  public void testAcceptResults() throws Exception {
    PageResult result = new PageResult("SomePage");
    result.setTestSummary(new TestSummary(5, 0, 0, 0));
  }

  @Test
  public void testHtmlOption() throws Exception {
    runner.args(new String[]
      {"-html", "stdout", "blah", "80", "blah"}
    );
    assertEquals(1, runner.formatters.size());
    FormattingOption option = runner.formatters.get(0);
    assertEquals("html", option.format);
  }

  @Test
  public void testVerboseOption() throws Exception {
    runner.args(new String[]
      {"-v", "blah", "80", "blah"}
    );
    assertEquals(1, runner.handler.subHandlers.size());
    Object o = runner.handler.subHandlers.get(0);
    assertTrue(o instanceof StandardResultHandler);
    assertTrue(runner.verbose);
  }

  @Test
  public void testNoPathOption() throws Exception {
    assertTrue(runner.usingDownloadedPaths);
    String request = runner.makeHttpRequest();
    assertSubString("includePaths", request);

    runner.args(new String[]
      {"-nopath", "blah", "80", "blah"}
    );
    assertFalse(runner.usingDownloadedPaths);
    request = runner.makeHttpRequest();
    assertNotSubString("includePaths", request);
  }

  @Test
  public void testAddUrlToClasspath() throws Exception {
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    assertTrue(systemClassLoader instanceof URLClassLoader);
    URLClassLoader classLoader = (URLClassLoader) systemClassLoader;

    URL sampleUrl = new File("src").toURI().toURL();

    String classpath = classpathAsString(classLoader);
    assertNotSubString(sampleUrl.toString(), classpath);

    TestRunner.addUrlToClasspath(sampleUrl);
    classpath = classpathAsString(classLoader);
    assertSubString(sampleUrl.toString(), classpath);
  }

  @Test
  public void testAddMultipleUrlsToClasspath() throws Exception {
    String separator = System.getProperty("path.separator");
    String paths = "/blah/blah" + separator + "C" + otherSeperator(separator) + "\\foo\\bar";
    TestRunner.addItemsToClasspath(paths);
    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    String classpath = classpathAsString(classLoader);
    assertSubString("/blah/blah", classpath);
    assertMatches("[C" + otherSeperator(separator) + "?foo?bar]", classpath);
  }

  private String otherSeperator(String separator) {
    return separator.equals(";") ? ":" : ";";
  }

  private String classpathAsString(URLClassLoader classLoader) {
    URL[] urls = classLoader.getURLs();
    StringBuffer urlString = new StringBuffer();
    for (int i = 0; i < urls.length; i++)
      urlString.append(urls[i].toString()).append(":");
    return urlString.toString();
  }

  @Test
  public void testNonMatchingSuiteFilter() throws Exception {
    runPage("-results testFile.txt -suiteFilter xxx", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertDoesntHaveRegexp(".*TestPassing.*", content);
    assertDoesntHaveRegexp(".*TestFailing.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestIgnore.*", content);
  }

  @Test
  public void testSimpleMatchingSuiteFilter() throws Exception {
    runPage("-results testFile.txt -suiteFilter foo", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertHasRegexp(".*TestPassing.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestIgnore.*", content);
  }

  @Test
  public void testSecondMatchingSuiteFilter() throws Exception {
    runPage("-results testFile.txt -suiteFilter smoke", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertDoesntHaveRegexp(".*TestPassing.*", content);
    assertHasRegexp(".*TestFailing.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestIgnore.*", content);
  }
}
