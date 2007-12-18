// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fit.Counts;
import fitnesse.components.ClassPathBuilder;
import fitnesse.components.FitClientListener;
import fitnesse.html.HtmlTag;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.wiki.*;

import java.util.*;

public class SuiteResponder extends TestResponder implements FitClientListener
{
	public static final String SUITE_SETUP_NAME = "SuiteSetUp";
	public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

	private LinkedList<WikiPage> processingQueue = new LinkedList<WikiPage>();
	private WikiPage currentTest = null;
	private SuiteHtmlFormatter suiteFormatter;
	private List<WikiPage> testPages;

	protected HtmlTag addSummaryPlaceHolder()
	{
		HtmlTag testSummaryDiv = new HtmlTag("div", "Running Tests ...");
		testSummaryDiv.addAttribute("id", "test-summary");

		return testSummaryDiv;
	}

	protected void finishSending() throws Exception
	{
	}

	protected void performExecution() throws Exception
	{
		processTestPages(testPages);

		client.done();
		client.join();

		completeResponse();
	}

	protected void prepareForExecution() throws Exception
	{
		testPages = makePageList();

		classPath = buildClassPath(testPages, page);
	}

	private void processTestPages(List<WikiPage> testPages) throws Exception
	{
    for (WikiPage testPage : testPages) {
      processingQueue.addLast(testPage);
      String testableHtml = SetupTeardownIncluder.render(testPage.getData());
      if (testableHtml.length() > 0)
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
		WikiPage firstInLine = processingQueue.isEmpty() ? null : processingQueue.getFirst();
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
		WikiPage testPage = processingQueue.removeFirst();
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

	public static String buildClassPath(List<WikiPage> testPages, WikiPage page) throws Exception
	{
		final ClassPathBuilder classPathBuilder = new ClassPathBuilder();
		final String pathSeparator = classPathBuilder.getPathSeparator(page);
		List<Object> classPathElements = new ArrayList<Object>();
		Set visitedPages = new HashSet();

    for (WikiPage testPage : testPages) {
      addClassPathElements(testPage, classPathElements, visitedPages);
    }
    return classPathBuilder.createClassPathString(classPathElements, pathSeparator);
	}

	private static void addClassPathElements(WikiPage page, List<Object> classPathElements, Set visitedPages) throws Exception
	{
		List pathElements = new ClassPathBuilder().getInheritedPathElements(page, visitedPages);
		classPathElements.addAll(pathElements);
	}

	public List<WikiPage> makePageList() throws Exception
	{
		String suite = request != null ? (String) request.getInput("suiteFilter") : null;
		return makePageList(page, root, suite);
	}

	public static List<WikiPage> makePageList(WikiPage page, WikiPage root, String suite) throws Exception
	{
		LinkedList<WikiPage> pages = getAllTestPagesUnder(page, suite);
		List<WikiPage> referencedPages = gatherCrossReferencedTestPages(page, root);
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

		if(pages.isEmpty())
		{
			String name = new WikiPagePath(page).toString();
			WikiPageDummy dummy = new WikiPageDummy("", "|Comment|\n|No test found with suite filter '" + suite + "' in subwiki !-" + name + "-!!|\n");
			dummy.setParent(root);
			pages.add(dummy);
		}
		return pages;
	}

	public static LinkedList getAllTestPagesUnder(WikiPage suiteRoot) throws Exception
	{
		return getAllTestPagesUnder(suiteRoot, null);
	}

	public static LinkedList getAllTestPagesUnder(WikiPage suiteRoot, String suite) throws Exception
	{
		LinkedList<WikiPage> testPages = new LinkedList<WikiPage>();
		addTestPagesToList(testPages, suiteRoot, suite);

		Collections.sort(testPages, new Comparator<WikiPage>()
		{
			public int compare(WikiPage p1, WikiPage p2)
			{
				try
				{
					PageCrawler crawler = p1.getPageCrawler();
					WikiPagePath path1 = crawler.getFullPath(p1);
					WikiPagePath path2 = crawler.getFullPath(p2);

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

	private static void addTestPagesToList(List<WikiPage> testPages, WikiPage context, String suite) throws Exception
	{
		PageData data = context.getData();
		if (! data.hasAttribute(PageData.PropertyPRUNE))
		{
			if (data.hasAttribute("Test"))
			{
				if(belongsToSuite(context, suite))
				{
					testPages.add(context);
				}
			}

			ArrayList<WikiPage> children = new ArrayList<WikiPage>();
			children.addAll(context.getChildren());
			if(context.hasExtension(VirtualCouplingExtension.NAME))
			{
				VirtualCouplingExtension extension = (VirtualCouplingExtension) context.getExtension(VirtualCouplingExtension.NAME);
				children.addAll(extension.getVirtualCoupling().getChildren());
			}
			for (WikiPage page : children) {
				addTestPagesToList(testPages, page, suite);
			}
		}
  }

	private static boolean belongsToSuite(WikiPage context, String suite)
	{
		if((suite == null) || (suite.trim().length() == 0))
		{
			return true;
		}
		try
		{
			String suitesStr = context.getData().getAttribute(PageData.PropertySUITES);
			if(suitesStr != null)
			{
				StringTokenizer t = new StringTokenizer(suitesStr, ",");
				while(t.hasMoreTokens())
				{
					if(t.nextToken().trim().equalsIgnoreCase(suite))
					{
						return true;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static List<WikiPage> gatherCrossReferencedTestPages(WikiPage testPage, WikiPage root) throws Exception
	{
		LinkedList<WikiPage> pages = new LinkedList<WikiPage>();
		PageData data = testPage.getData();
		List<String> pageReferences = data.getXrefPages();
		PageCrawler crawler = testPage.getPageCrawler();
		WikiPagePath testPagePath = crawler.getFullPath(testPage);
		WikiPage parent = crawler.getPage(root, testPagePath.parentPath());
    for (String pageReference : pageReferences) {
      WikiPagePath path = PathParser.parse(pageReference);
      WikiPage referencedPage = crawler.getPage(parent, path);
      if (referencedPage != null)
        pages.add(referencedPage);
    }
    return pages;
	}
}
