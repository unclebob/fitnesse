// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.html.*;
import fitnesse.wiki.*;
import fitnesse.components.*;
import fit.*;
import java.util.*;

public class SuiteResponder extends TestResponder implements FitClientListener
{
	public static final String SUITE_SETUP_NAME = "SuiteSetUp";
	public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

	private LinkedList processingQueue = new LinkedList();
	private WikiPage currentTest = null;
	private SuiteHtmlFormatter suiteFormatter;

	protected HtmlTag addSummaryPlaceHolder()
	{
		HtmlTag testSummaryDiv = new HtmlTag("div", "Running Tests ...");
		testSummaryDiv.addAttribute("id", "test-summary");

		return testSummaryDiv;
	}

	protected void doSending() throws Exception
	{
		data = page.getData();
		buildHtml();
		addToResponse(formatter.head());

		List testPages = makePageList();

		String classPath = buildClassPath(testPages, page);
		command = buildCommand(data, getClassName(data, request), classPath);
		client = new CommandRunningFitClient(this, command, context.port, context.socketDealer);
		log = new ExecutionLog(page, client.commandRunner);
		client.start();
		if(client.isSuccessfullyStarted())
		{
			processTestPages(testPages);

			client.done();
			client.join();

			completeResponse();
		}
	}

	private void processTestPages(List testPages) throws Exception
	{
		for(Iterator iterator = testPages.iterator(); iterator.hasNext();)
		{
			WikiPage testPage = (WikiPage) iterator.next();
			processingQueue.addLast(testPage);
			String testableHtml = HtmlUtil.testableHtml(testPage.getData());
			if(testableHtml.length() > 0)
				client.send(testableHtml);
			else
				client.send(emptyPageContent);
		}
	}

	protected void close() throws Exception
	{
		response.add(suiteFormatter.testOutput());
		response.add(suiteFormatter.tail());
		response.closeChunks();
		response.addTrailingHeader("Exit-Code", String.valueOf(exitCode()));
		response.closeTrailer();
		response.close();
	}

	public void acceptOutput(String output) throws Exception
	{
		WikiPage firstInLine = processingQueue.isEmpty() ? null : (WikiPage) processingQueue.getFirst();
		if(firstInLine != null && firstInLine != currentTest)
		{
			PageCrawler pageCrawler = page.getPageCrawler();
			String relativeName = pageCrawler.getRelativeName(page, firstInLine);
			WikiPagePath fullPath = pageCrawler.getFullPath(firstInLine);
			String fullPathName = PathParser.render(fullPath);
			suiteFormatter.startOutputForNewTest(relativeName, fullPathName);
			currentTest = firstInLine;
		}
		suiteFormatter.acceptOutput(output);
	}

	public void acceptResults(Counts counts) throws Exception
	{
		super.acceptResults(counts);

		PageCrawler pageCrawler = page.getPageCrawler();
		WikiPage testPage = (WikiPage) processingQueue.removeFirst();
		String relativeName = pageCrawler.getRelativeName(page, testPage);
		addToResponse(suiteFormatter.acceptResults(relativeName, counts));
	}

	protected void makeFormatter() throws Exception
	{
		suiteFormatter = new SuiteHtmlFormatter(html);
		formatter = suiteFormatter;
	}

	protected String pageType()
	{
		return "Suite Results";
	}

	public static String buildClassPath(List testPages, WikiPage page) throws Exception
	{
		final ClassPathBuilder classPathBuilder = new ClassPathBuilder();
		final String pathSeparator = classPathBuilder.getPathSeparator(page);
		List classPathElements = new ArrayList();
		Set visitedPages = new HashSet();

		for(Iterator iterator = testPages.iterator(); iterator.hasNext();)
		{
			WikiPage testPage = (WikiPage) iterator.next();
			addClassPathElements(testPage, classPathElements, visitedPages);
		}
		final String classPathString = classPathBuilder.createClassPathString(classPathElements, pathSeparator);
		return classPathString;
	}

	private static void addClassPathElements(WikiPage page, List classPathElements, Set visitedPages) throws Exception
	{
		List pathElements = new ClassPathBuilder().getInheritedPathElements(page, visitedPages);
		classPathElements.addAll(pathElements);
	}

	public List makePageList() throws Exception
	{
		return makePageList(page, root);
	}

	public static List makePageList(WikiPage page, WikiPage root) throws Exception
	{
		LinkedList pages = getAllTestPagesUnder(page);
		List referencedPages = gatherCrossReferencedTestPages(page, root);
		pages.addAll(referencedPages);

		WikiPage suiteSetUp = PageCrawlerImpl.getInheritedPage(SUITE_SETUP_NAME, page);
		if(suiteSetUp != null)
		{
			if(pages.contains(suiteSetUp))
				pages.remove(suiteSetUp);
			pages.addFirst(suiteSetUp);
		}
		WikiPage suiteTearDown = PageCrawlerImpl.getInheritedPage(SUITE_TEARDOWN_NAME, page);
		if(suiteTearDown != null)
		{
			if(pages.contains(suiteTearDown))
				pages.remove(suiteTearDown);
			pages.addLast(suiteTearDown);
		}

		return pages;
	}

	public static LinkedList getAllTestPagesUnder(WikiPage suiteRoot) throws Exception
	{
		LinkedList testPages = new LinkedList();
		addTestPagesToList(testPages, suiteRoot);

		Collections.sort(testPages, new Comparator()
		{
			public int compare(Object o, Object o1)
			{
				try
				{
					PageCrawler crawler = ((WikiPage) o).getPageCrawler();
					WikiPagePath path1 = crawler.getFullPath((WikiPage) o);
					WikiPagePath path2 = crawler.getFullPath((WikiPage) o1);

					return path1.compareTo(path2);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return 0;
				}
			}
		});

		return testPages;
	}

	private static void addTestPagesToList(List testPages, WikiPage context) throws Exception
	{
		if(context.getData().hasAttribute("Test"))
			testPages.add(context);

		ArrayList children = new ArrayList();
		children.addAll(context.getChildren());
		if(context.hasExtension(VirtualCouplingExtension.NAME))
		{
			VirtualCouplingExtension extension = (VirtualCouplingExtension) context.getExtension(VirtualCouplingExtension.NAME);
			children.addAll(extension.getVirtualCoupling().getChildren());
		}
		for(Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			WikiPage page = (WikiPage) iterator.next();
			addTestPagesToList(testPages, page);
		}
	}

	public static List gatherCrossReferencedTestPages(WikiPage testPage, WikiPage root) throws Exception
	{
		LinkedList pages = new LinkedList();
		PageData data = testPage.getData();
		List pageReferences = data.getXrefPages();
		PageCrawler crawler = testPage.getPageCrawler();
		WikiPagePath testPagePath = crawler.getFullPath(testPage);
		WikiPage parent = crawler.getPage(root, testPagePath.parentPath());
		for(Iterator i = pageReferences.iterator(); i.hasNext();)
		{
			WikiPagePath path = PathParser.parse((String) i.next());
			WikiPage referencedPage = crawler.getPage(parent, path);
			if(referencedPage != null)
				pages.add(referencedPage);
		}
		return pages;
	}
}
