// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.http.*;
import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.testutil.*;
import fitnesse.wiki.*;

import java.util.List;

public class SuiteResponderTest extends RegexTest
{
	private MockRequest request;
	private SuiteResponder responder;
	private WikiPage root;
	private WikiPage suite;
	private int port = 9123;
	private FitNesseContext context;
	private FitSocketReceiver receiver;
	private WikiPage testPage;
	private PageCrawler crawler;
	private WikiPage testPage2;
	private WikiPage testChildPage;
	private String suitePageName;

	public void setUp() throws Exception
	{
		suitePageName = "SuitePage";
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		PageData data = root.getData();
		data.setContent(classpathWidgets());
		root.commit(data);
		suite = crawler.addPage(root, PathParser.parse(suitePageName), "This is the test suite\n");
		testPage = addTestToSuite("TestOne", "|!-fitnesse.testutil.PassFixture-!|\n");

		request = new MockRequest();
		request.setResource(suitePageName);
		responder = new SuiteResponder();
		responder.page = suite;
		context = new FitNesseContext(root);
		context.port = port;

		receiver = new FitSocketReceiver(port, context.socketDealer);
	}

	private WikiPage addTestToSuite(String name, String content) throws Exception
	{
		return addTestPage(suite, name, content);
	}

	private WikiPage addTestPage(WikiPage page, String name, String content) throws Exception
	{
		WikiPage testPage = crawler.addPage(page, PathParser.parse(name), content);
		PageData data = testPage.getData();
		data.setAttribute("Test");
		testPage.commit(data);
		return testPage;
	}

	public void tearDown() throws Exception
	{
		receiver.close();
	}

	private String runSuite() throws Exception
	{
		receiver.receiveSocket();
		Response response = responder.makeResponse(context, request);
		MockResponseSender sender = new MockResponseSender(response);
		String results = sender.sentData();
		return results;
	}

	public void testGatherXRefTestPages() throws Exception
	{
		WikiPage testPage = crawler.addPage(root, PathParser.parse("SomePage"), "!see PageA\n!see PageB");
		WikiPage pageA = crawler.addPage(root, PathParser.parse("PageA"));
		WikiPage pageB = crawler.addPage(root, PathParser.parse("PageB"));
		List xrefTestPages = SuiteResponder.gatherCrossReferencedTestPages(testPage, root);
		assertEquals(2, xrefTestPages.size());
		assertTrue(xrefTestPages.contains(pageA));
		assertTrue(xrefTestPages.contains(pageB));
	}

	public void testBuildClassPath() throws Exception
	{
		responder.page = suite;
		List testPages = SuiteResponder.getAllTestPagesUnder(suite);
		String classpath = SuiteResponder.buildClassPath(testPages, responder.page);
		assertSubString("classes", classpath);
		assertSubString("dummy.jar", classpath);
	}

	public void testWithOneTest() throws Exception
	{
		String results = runSuite();
		assertSubString("href=\"#TestOne\"", results);
		assertSubString("1 right", results);
		assertSubString("id=\"TestOne\"", results);
		assertSubString(" href=\"SuitePage.TestOne\"", results);
		assertSubString("PassFixture", results);
	}

	public void testPageWithXref() throws Exception
	{
		PageData data = suite.getData();
		data.setContent("!see XrefOne\r\n!see XrefTwo\n!see XrefThree\n");
		suite.commit(data);
		addTestPage(root, "XrefOne", "|!-fitnesse.testutil.PassFixture-!|\n");
		addTestPage(root, "XrefTwo", "|!-fitnesse.testutil.PassFixture-!|\n");

		String results = runSuite();
		assertSubString("href=\"#XrefOne\"", results);
		assertSubString("href=\"#XrefTwo\"", results);
	}

	public void testWithTwoTests() throws Exception
	{
		addTestToSuite("TestTwo", "|!-fitnesse.testutil.FailFixture-!|\n\n|!-fitnesse.testutil.FailFixture-!|\n");
		String results = runSuite();

		assertSubString("href=\"#TestOne\"", results);
		assertSubString("href=\"#TestTwo\"", results);
		assertSubString("1 right", results);
		assertSubString("2 wrong", results);
		assertSubString("id=\"TestOne\"", results);
		assertSubString("id=\"TestTwo\"", results);
		assertSubString("PassFixture", results);
		assertSubString("FailFixture", results);
	}

	public void testSuiteWithEmptyPage() throws Exception
	{
		suite = crawler.addPage(root, PathParser.parse("SuiteWithEmptyPage"), "This is the empty page test suite\n");
		addTestPage(suite, "TestThatIsEmpty", "");
		request.setResource("SuiteWithEmptyPage");
		runSuite();

		WikiPagePath errorLogPath = PathParser.parse("ErrorLogs.SuiteWithEmptyPage");
		WikiPage errorLog = crawler.getPage(root, errorLogPath);
		PageData data = errorLog.getData();
		String errorLogContent = data.getContent();
		assertNotSubString("Exception", errorLogContent);
	}

	public void testSuiteWithOneTestWithoutTable() throws Exception
	{
		addTestToSuite("TestWithoutTable", "This test has not table");
		addTestToSuite("TestTwo", "|!-fitnesse.testutil.PassFixture-!|\n");
		addTestToSuite("TestThree", "|!-fitnesse.testutil.PassFixture-!|\n");
		String results = runSuite();

		assertSubString("TestOne", results);
		assertSubString("TestTwo", results);
		assertSubString("TestThree", results);
		assertSubString("TestWithoutTable", results);
	}

	public void testExitCodeHeader() throws Exception
	{
		String results = runSuite();
		assertSubString("Exit-Code: 0", results);
	}

	public void testGetAllTestPages() throws Exception
	{
		setUpForGetAllTestPages();

		List testPages = SuiteResponder.getAllTestPagesUnder(suite);
		assertEquals(3, testPages.size());
		assertEquals(true, testPages.contains(testPage));
		assertEquals(true, testPages.contains(testPage2));
		assertEquals(true, testPages.contains(testChildPage));
	}

	private void setUpForGetAllTestPages() throws Exception
	{
		testPage2 = addTestToSuite("TestPageTwo", "test page two");
		testChildPage = testPage2.addChildPage("TestChildPage");
		PageData data = testChildPage.getData();
		data.setAttribute("Test");
		testChildPage.commit(data);
	}

	public void testGetAllTestPagesSortsByQulifiedNames() throws Exception
	{
		setUpForGetAllTestPages();
		List testPages = SuiteResponder.getAllTestPagesUnder(suite);
		assertEquals(3, testPages.size());
		assertEquals(testPage, testPages.get(0));
		assertEquals(testPage2, testPages.get(1));
		assertEquals(testChildPage, testPages.get(2));
	}

	public void testSetUpAndTearDown() throws Exception
	{
		WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
		WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

		List testPages = responder.makePageList();
		assertEquals(3, testPages.size());
		assertSame(setUp, testPages.get(0));
		assertSame(tearDown, testPages.get(2));
	}

	public void testExecutionStatusAppears() throws Exception
	{
		String results = runSuite();
		assertHasRegexp(divWithIdAndContent("execution-status", ".*?"), results);
	}

	public void testTestSummaryInformationIncludesPageSummary() throws Exception
	{
		String results = runSuite();
		assertHasRegexp(divWithIdAndContent("test-summary", ".*?Test Pages:.*?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.*?Assertions:.*?"), results);
	}

	public void testFormatTestSummaryInformation() throws Exception
	{
		String results = runSuite();
		assertHasRegexp(divWithIdAndContent("test-summary", ".*?<strong>Test Pages:</strong>.*?<strong>Assertions:</strong>.*?"), results);
	}

	private String classpathWidgets()
	{
		return "!path classes\n" +
			"!path lib/dummy.jar\n";
	}

	public void testNonMatchingSuiteFilter() throws Exception
	{
		addTestPagesWithSuiteProperty();
		request.setQueryString("suiteFilter=xxx");
		String results = runSuite();
		assertDoesntHaveRegexp(".*href=\"#TestOne\".*", results);
		assertDoesntHaveRegexp(".*href=\"#TestTwo\".*", results);
		assertDoesntHaveRegexp(".*href=\"#TestThree\".*", results);
	}

	public void testSimpleMatchingSuiteFilter() throws Exception
	{
		addTestPagesWithSuiteProperty();
		request.setQueryString("suiteFilter=foo");
		String results = runSuite();
		assertDoesntHaveRegexp(".*href=\"#TestOne\".*", results);
		assertHasRegexp(".*href=\"#TestTwo\".*", results);
		assertDoesntHaveRegexp(".*href=\"#TestThree\".*", results);
	}

	public void testSecondMatchingSuiteFilter() throws Exception
	{
		addTestPagesWithSuiteProperty();
		request.setQueryString("suiteFilter=smoke");
		String results = runSuite();
		assertDoesntHaveRegexp(".*href=\"#TestOne\".*", results);
		assertDoesntHaveRegexp(".*href=\"#TestTwo\".*", results);
		assertHasRegexp(".*href=\"#TestThree\".*", results);
	}

	private void addTestPagesWithSuiteProperty() throws Exception
	{
		WikiPage test2 = addTestToSuite("TestTwo", "|!-fitnesse.testutil.PassFixture-!|\n");
		WikiPage test3 = addTestToSuite("TestThree", "|!-fitnesse.testutil.PassFixture-!|\n");
		PageData data2 = test2.getData();
		PageData data3 = test3.getData();
		data2.setAttribute(PropertiesResponder.SUITES, "foo");
		data3.setAttribute(PropertiesResponder.SUITES, "bar, smoke");
		test2.commit(data2);
		test3.commit(data3);
	}
}
