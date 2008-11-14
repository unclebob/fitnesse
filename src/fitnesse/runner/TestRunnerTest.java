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
import org.junit.BeforeClass;
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
  private ByteArrayOutputStream outputBytes;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    FitClientResponderTest.buildSuite(root);

    FitNesseUtil.startFitnesse(root);
    port = FitNesseUtil.port;

    outputBytes = new ByteArrayOutputStream();
    runner = new TestRunner(new PrintStream(outputBytes));
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
    FileUtil.deleteFile("testFile.txt");
  }

  @Test
  public void testOnePassingTest() throws Exception {
    testPageResults("SuitePage.TestPassing", new TestSummary(1, 0, 0, 0), 0);
  }

  @Test
  public void testOneFailingTest() throws Exception {
    testPageResults("SuitePage.TestFailing", new TestSummary(0, 1, 0, 0), 1);
  }

  @Test
  public void testOneErrorTest() throws Exception {
    testPageResults("SuitePage.TestError", new TestSummary(0, 0, 0, 1), 1);
  }

  @Test
  public void testOneIgnoreTest() throws Exception {
    testPageResults("SuitePage.TestIgnore", new TestSummary(0, 0, 1, 0), 0);
  }

  @Test
  public void testSuite() throws Exception {
    testPageResults("SuitePage", new TestSummary(1, 1, 1, 1), 2);
  }

  @Test
  public void verbose() throws Exception {
    runPage("-v", "SuitePage.TestPassing");
    assertEquals("Test Runner for Root Path: TestPassing\n" +
      "Page:(TestPassing) right:1, wrong:0, ignored:0, exceptions:0\n", outputBytes.toString());
  }

  @Test
  public void testXMLFile() throws Exception {
    runPage("-xml testFile.txt", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String xmlContent = FileUtil.getFileContent("testFile.txt");
    Document testResultsDocument = XmlUtil.newDocument(xmlContent);
    Element testResultsElement = testResultsDocument.getDocumentElement();
    assertEquals("testResults", testResultsElement.getNodeName());
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

  private void testPageResults(String pageName, TestSummary expectedCounts, int exitCode) throws Exception {
    runPage(pageName);
    TestSummary counts = runner.getCounts();
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
    runPage("-xml testFile.txt -suiteFilter xxx", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertDoesntHaveRegexp(".*TestPassing.*", content);
    assertDoesntHaveRegexp(".*TestFailing.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestIgnore.*", content);
  }

  @Test
  public void testSimpleMatchingSuiteFilter() throws Exception {
    runPage("-xml testFile.txt -suiteFilter foo", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertHasRegexp(".*TestPassing.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestIgnore.*", content);
  }

  @Test
  public void testSecondMatchingSuiteFilter() throws Exception {
    runPage("-xml testFile.txt -suiteFilter smoke", "SuitePage");
    assertTrue(new File("testFile.txt").exists());
    String content = FileUtil.getFileContent("testFile.txt");
    assertDoesntHaveRegexp(".*TestPassing.*", content);
    assertHasRegexp(".*TestFailing.*", content);
    assertDoesntHaveRegexp(".*TestError.*", content);
    assertDoesntHaveRegexp(".*TestIgnore.*", content);
  }
}
