// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.components.PageReferencer;
import fitnesse.wiki.*;
import java.util.regex.*;

public class IncludeWidget extends ParentWidget implements PageReferencer
{
	public static final String REGEXP = "^!include(?: +-setup| +-teardown| +-seamless)? " + WikiWordWidget.REGEXP + LineBreakWidget.REGEXP + "?";
	static final Pattern pattern = Pattern.compile("^!include *(-setup|-teardown|-seamless)? (.*)");

	protected String pageName;
	protected WikiPage includingPage;
	protected WikiPage parentPage;

	public IncludeWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher matcher = pattern.matcher(text);
		if(matcher.find())
		{
			pageName = parsePageName(matcher);
			includingPage = parent.getWikiPage();
			parentPage = includingPage.getParent();
			handleDisplayForOption(parseOption(matcher));
		}
	}

	private String parseOption(Matcher match)
	{
		return match.group(1);
	}

	private String parsePageName(Matcher match)
	{
		return match.group(2);
	}

	//TODO MDM I know this is bad...  But it seems better then creatting two new widgets.
	private void handleDisplayForOption(String option) throws Exception
	{
		String widgetText = processLiterals(getIncludedPageContent());
		if("-seamless".equals(option))
		{
			addChildWidgets(widgetText + "\n");
		}
		else if("-setup".equals(option))
		{
			new CollapsableWidget(this, "Set Up: " + pageName, widgetText, "setup");
		}
		else if("-teardown".equals(option))
		{
			new CollapsableWidget(this, "Tear Down: " + pageName, widgetText, "teardown");
		}
		else
		{
			new CollapsableWidget(this, "Included page: " + pageName, widgetText, "included");
		}
	}

	public String render() throws Exception
	{
		return childHtml();
	}

	public WikiPage getReferencedPage() throws Exception
	{
		return getParentPage().getPageCrawler().getPage(getParentPage(), PathParser.parse(pageName));
	}

	protected String getIncludedPageContent() throws Exception
	{
		PageCrawler crawler = parentPage.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		WikiPagePath pagePath = PathParser.parse(pageName);
		if(crawler.pageExists(parentPage, pagePath))
		{
			WikiPage page = getIncludedPage();
			return page.getData().getContent();
		}
		else if(includingPage instanceof ProxyPage)
		{
			ProxyPage proxy = (ProxyPage) includingPage;
			String host = proxy.getHost();
			int port = proxy.getHostPort();
			try
			{
				ProxyPage remoteIncludedPage = new ProxyPage("RemoteIncludedPage", null, host, port, pagePath);
				return remoteIncludedPage.getData().getContent();
			}
			catch(Exception e)
			{
				return "!meta '''Remote page " + host + ":" + port + "/" + pageName + " does not exist.'''";
			}
		}
		else
		{
			return "!meta '''Page include failed because the page " + pageName + " does not exist.'''";
		}
	}

	protected WikiPage getIncludedPage() throws Exception
	{
		PageCrawler crawler = parentPage.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		return crawler.getPage(parentPage, PathParser.parse(pageName));
	}

	protected WikiPage getParentPage() throws Exception
	{
		return parent.getWikiPage().getParent();
	}
}
