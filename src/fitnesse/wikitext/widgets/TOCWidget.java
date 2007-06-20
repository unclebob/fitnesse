// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.*;
import fitnesse.wiki.*;
import fitnesse.wikitext.WikiWidget;

import java.util.*;

public class TOCWidget extends WikiWidget
{
	public static final String REGEXP = "(?:^!contents[ \t]*$)|(?:^!contents -R[ \t]*$)";

	private boolean recursive;

	public TOCWidget(ParentWidget parent, String text)
	{
		super(parent);
		setRecursive(text);
	}

	private void setRecursive(String text)
	{
		recursive = (text.indexOf("-R") > -1);
	}

	public String render() throws Exception
	{
		return buildContentsDiv(getWikiPage(), 1).html();
	}

	private HtmlTag buildContentsDiv(WikiPage wikiPage, int currentDepth)
		throws Exception
	{
		HtmlTag div = makeDivTag(currentDepth);
		div.add(buildList(wikiPage, currentDepth));
		return div;
	}

	private HtmlTag buildList(WikiPage wikiPage, int currentDepth)
		throws Exception
	{
		HtmlTag list = new HtmlTag("ul");
		for(Iterator iterator = buildListOfChildPages(wikiPage).iterator(); iterator.hasNext();)
		{
			list.add(buildListItem((WikiPage) iterator.next(), currentDepth));
		}
		return list;
	}

	private HtmlTag buildListItem(WikiPage wikiPage, int currentDepth) throws Exception
	{
		HtmlTag listItem = new HtmlTag("li");
		listItem.add(HtmlUtil.makeLink(getHref(wikiPage), getLinkText(wikiPage)));
		if(isRecursive() && buildListOfChildPages(wikiPage).size() > 0)
		{
			listItem.add(buildContentsDiv(wikiPage, currentDepth + 1));
		}
		return listItem;
	}

	private String getHref(WikiPage wikiPage) throws Exception
	{
		String href = null;
		WikiPagePath wikiPagePath = wikiPage.getPageCrawler().getFullPath(wikiPage);
		href = PathParser.render(wikiPagePath);
		return href;
	}

	private HtmlElement getLinkText(WikiPage wikiPage) throws Exception
	{
		if(wikiPage instanceof ProxyPage)
			return new HtmlTag("i", wikiPage.getName());
		else
			return new RawHtml(wikiPage.getName());
	}

	private List buildListOfChildPages(WikiPage wikiPage) throws Exception
	{
		List childPageList = new ArrayList(wikiPage.getChildren());
		if(wikiPage.hasExtension(VirtualCouplingExtension.NAME))
		{
			VirtualCouplingExtension extension = (VirtualCouplingExtension) wikiPage.getExtension(VirtualCouplingExtension.NAME);
			WikiPage virtualCoupling = extension.getVirtualCoupling();
			childPageList.addAll(virtualCoupling.getChildren());
		}
		Collections.sort(childPageList);
		return childPageList;
	}

	private HtmlTag makeDivTag(int currentDepth)
	{
		return HtmlUtil.makeDivTag("toc" + currentDepth);
	}

	public boolean isRecursive()
	{
		return recursive;
	}
}
