// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;
import fitnesse.responders.run.*;
import fitnesse.testutil.*;
import fitnesse.util.FileUtil;
import fitnesse.wiki.*;

import java.io.*;
import java.net.*;
import java.util.List;

public class TestRunnerTest extends RegexTest
{
	private int port;
	private WikiPage root;
	private TestRunner runner;
	private MockResultFormatter mockHandler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		FitClientResponderTest.buildSuite(root);

		FitNesseUtil.startFitnesse(root);
		port = FitNesseUtil.port;

		runner = new TestRunner(new PrintStream(new ByteArrayOutputStream()));
		mockHandler = new MockResultFormatter();
	}

	public void tearDown() throws Exception
	{
		FitNesseUtil.stopFitnesse();
		FileUtil.deleteFile("testFile.txt");
	}

	public void testOnePassingTest() throws Exception
	{
		testPageResults("SuitePage.TestPassing", new Counts(1, 0, 0, 0), 0);
	}

	public void testOneFailingTest() throws Exception
	{
		testPageResults("SuitePage.TestFailing", new Counts(0, 1, 0, 0), 1);
	}

	public void testOneErrorTest() throws Exception
	{
		testPageResults("SuitePage.TestError", new Counts(0, 0, 0, 1), 1);
	}

	public void testOneIgnoreTest() throws Exception
	{
		testPageResults("SuitePage.TestIgnore", new Counts(0, 0, 1, 0), 0);
	}

	public void testSuite() throws Exception
	{
		testPageResults("SuitePage", new Counts(1, 1, 1, 1), 2);
	}

	public void testResultContentWithOneTest() throws Exception
	{
		runner.handler.addHandler(mockHandler);
		runPage("SuitePage.TestPassing");
		List results = mockHandler.results;
		assertEquals(1, results.size());
		Object result1 = results.get(0);
		assertTrue(result1 instanceof PageResult);
		PageResult pageResult = (PageResult) result1;
		assertSubString("PassFixture", pageResult.content());
		assertEquals("", pageResult.title());
		assertEquals(new Counts(1, 0, 0, 0), pageResult.counts());
	}

	public void testResultContentWithSuite() throws Exception
	{
		runner.handler.addHandler(mockHandler);
		runPage("SuitePage");
		List results = mockHandler.results;
		assertEquals(4, results.size());

		checkResult(results, 0, "TestError", new Counts(0, 0, 0, 1), "ErrorFixture");
		checkResult(results, 1, "TestFailing", new Counts(0, 1, 0, 0), "FailFixture");
		checkResult(results, 2, "TestIgnore", new Counts(0, 0, 1, 0), "IgnoreFixture");
		checkResult(results, 3, "TestPassing", new Counts(1, 0, 0, 0), "PassFixture");
	}

	public void testKeepResultFile() throws Exception
	{
		runPage("-results testFile.txt", "SuitePage");
		assertTrue(new File("testFile.txt").exists());
		String content = FileUtil.getFileContent("testFile.txt");
		assertSubString("TestError", content);
		assertSubString("TestPassing", content);
		assertSubString("TestFailing", content);
		assertSubString("TestIgnore", content);
	}

	public void testSuiteSetUpAndTearDownIsCalledIfSingleTestIsRun() throws Exception
	{
		addSuiteSetUpTearDown();
		runner.handler.addHandler(mockHandler);

		runPage("SuitePage.TestPassing");

		List results = mockHandler.results;
		assertEquals(1, results.size());
		checkResult(results, 0, "", new Counts(3, 0, 0, 0), "PassFixture");
		PageResult result = (PageResult) results.get(0);
		String content = result.content();
		assertSubString("SuiteSetUp", content);
		assertSubString("SuiteTearDown", content);
	}

	private void addSuiteSetUpTearDown() throws Exception
	{
		PageCrawler crawler = root.getPageCrawler();
		WikiPage suitePage = crawler.getPage(root, PathParser.parse("SuitePage"));
		crawler.addPage(suitePage, PathParser.parse(SuiteResponder.SUITE_SETUP_NAME), "!|fitnesse.testutil.PassFixture|\n");
		crawler.addPage(suitePage, PathParser.parse(SuiteResponder.SUITE_TEARDOWN_NAME), "!|fitnesse.testutil.PassFixture|\n");
	}

	private void checkResult(List results, int i, String s, Counts counts, String content)
	{
		PageResult result = (PageResult) results.get(i);
		assertEquals(s, result.title());
		assertEquals(counts, result.counts());
		assertSubString(content, result.content());
	}

	private void testPageResults(String pageName, Counts expectedCounts, int exitCode) throws Exception
	{
		runPage(pageName);
		Counts counts = runner.getCounts();
		assertEquals(expectedCounts, counts);
		assertEquals(exitCode, runner.exitCode());
	}

	private void runPage(String pageName) throws Exception
	{
		runPage("", pageName);
	}

	private void runPage(String options, String pageName) throws Exception
	{
		options += " localhost " + port + " " + pageName;
		String[] args = options.trim().split(" ");
		runner.run(args);
	}

	public void testAcceptResults() throws Exception
	{
		PageResult result = new PageResult("SomePage");
		result.setCounts(new Counts(5, 0, 0, 0));
	}

	public void testHtmlOption() throws Exception
	{
		runner.args(new String[]
			{"-html", "stdout", "blah", "80", "blah"});
		assertEquals(1, runner.formatters.size());
		FormattingOption option = (FormattingOption) runner.formatters.get(0);
		assertEquals("html", option.format);
	}

	public void testVerboseOption() throws Exception
	{
		runner.args(new String[]
			{"-v", "blah", "80", "blah"});
		assertEquals(1, runner.handler.subHandlers.size());
		Object o = runner.handler.subHandlers.get(0);
		assertTrue(o instanceof StandardResultHandler);
		assertTrue(runner.verbose);
	}

	public void testNoPathOption() throws Exception
	{
		assertTrue(runner.usingDownloadedPaths);
		String request = runner.makeHttpRequest();
		assertSubString("includePaths", request);

		runner.args(new String[]
			{"-nopath", "blah", "80", "blah"});
		assertFalse(runner.usingDownloadedPaths);
		request = runner.makeHttpRequest();
		assertNotSubString("includePaths", request);
	}

	public void testAddUrlToClasspath() throws Exception
	{
		ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		assertTrue(systemClassLoader instanceof URLClassLoader);
		URLClassLoader classLoader = (URLClassLoader) systemClassLoader;

		URL sampleUrl = new File("src").toURL();

		String classpath = classpathAsString(classLoader);
		assertNotSubString(sampleUrl.toString(), classpath);

		TestRunner.addUrlToClasspath(sampleUrl);
		classpath = classpathAsString(classLoader);
		assertSubString(sampleUrl.toString(), classpath);
	}

	public void testAddMultipleUrlsToClasspath() throws Exception
	{
		String separator = System.getProperty("path.separator");
		String paths = "/blah/blah" + separator + "C" + otherSeperator(separator) + "\\foo\\bar";
		TestRunner.addItemsToClasspath(paths);
		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		String classpath = classpathAsString(classLoader);
		assertSubString("/blah/blah", classpath);
		assertMatches("[C" + otherSeperator(separator) + "?foo?bar]", classpath);
	}

	private String otherSeperator(String separator)
	{
		return separator.equals(";") ? ":" : ";";
	}

	private String classpathAsString(URLClassLoader classLoader)
	{
		URL[] urls = classLoader.getURLs();
		StringBuffer urlString = new StringBuffer();
		for(int i = 0; i < urls.length; i++)
			urlString.append(urls[i].toString()).append(":");
		return urlString.toString();
	}

	public void testNonMatchingSuiteFilter() throws Exception
	{
		runPage("-results testFile.txt -suiteFilter xxx", "SuitePage");
		assertTrue(new File("testFile.txt").exists());
		String content = FileUtil.getFileContent("testFile.txt");
		assertDoesntHaveRegexp(".*TestPassing.*", content);
		assertDoesntHaveRegexp(".*TestFailing.*", content);
		assertDoesntHaveRegexp(".*TestError.*", content);
		assertDoesntHaveRegexp(".*TestIgnore.*", content);
	}

	public void testSimpleMatchingSuiteFilter() throws Exception
	{
		runPage("-results testFile.txt -suiteFilter foo", "SuitePage");
		assertTrue(new File("testFile.txt").exists());
		String content = FileUtil.getFileContent("testFile.txt");
		assertHasRegexp(".*TestPassing.*", content);
		assertDoesntHaveRegexp(".*TestError.*", content);
		assertDoesntHaveRegexp(".*TestError.*", content);
		assertDoesntHaveRegexp(".*TestIgnore.*", content);
	}

	public void testSecondMatchingSuiteFilter() throws Exception
	{
		runPage("-results testFile.txt -suiteFilter smoke", "SuitePage");
		assertTrue(new File("testFile.txt").exists());
		String content = FileUtil.getFileContent("testFile.txt");
		assertDoesntHaveRegexp(".*TestPassing.*", content);
		assertHasRegexp(".*TestFailing.*", content);
		assertDoesntHaveRegexp(".*TestError.*", content);
		assertDoesntHaveRegexp(".*TestIgnore.*", content);
	}
}
