// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.FitNesseContext;
import fitnesse.wiki.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class RecentChanges
{
	private static final String RECENT_CHANGES = "RecentChanges";

	private static SimpleDateFormat makeDateFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		return new SimpleDateFormat(FitNesseContext.recentChangesDateFormat);
	}

	public static void updateRecentChanges(PageData pageData) throws Exception
	{
		createRecentChangesIfNecessary(pageData);
		addCurrentPageToRecentChanges(pageData);
	}

	public static List getRecentChangesLines(PageData recentChangesdata) throws Exception
	{
		String content = recentChangesdata.getContent();
		BufferedReader reader = new BufferedReader(new StringReader(content));
		List lines = new ArrayList();
		String line = null;
		while((line = reader.readLine()) != null)
			lines.add(line);
		return lines;
	}

	private static void addCurrentPageToRecentChanges(PageData data) throws Exception
	{
		WikiPage recentChanges = data.getWikiPage().getPageCrawler().getRoot(data.getWikiPage()).getChildPage(RECENT_CHANGES);
		String resource = resource(data);
		PageData recentChangesdata = recentChanges.getData();
		List lines = getRecentChangesLines(recentChangesdata);
		removeDuplicate(lines, resource);
		lines.add(0, makeRecentChangesLine(data));
		trimExtraLines(lines);
		String content = convertLinesToWikiText(lines);
		recentChangesdata.setContent(content);
		recentChanges.commit(recentChangesdata);
	}

	private static String resource(PageData data) throws Exception
	{
		WikiPagePath fullPath = data.getWikiPage().getPageCrawler().getFullPath(data.getWikiPage());
		String resource = PathParser.render(fullPath);
		return resource;
	}

	private static void createRecentChangesIfNecessary(PageData data) throws Exception
	{
		PageCrawler crawler = data.getWikiPage().getPageCrawler();
		WikiPage root = crawler.getRoot(data.getWikiPage());
		if(!root.hasChildPage(RECENT_CHANGES))
			crawler.addPage(root, PathParser.parse(RECENT_CHANGES), "");
	}

	private static String makeRecentChangesLine(PageData data) throws Exception
	{
		String user = data.getAttribute(WikiPage.LAST_MODIFYING_USER);
		if(user == null)
			user = "";
		return "|" + resource(data) + "|" + user + "|" + makeDateFormat().format(new Date()) + "|";
	}

	private static void removeDuplicate(List lines, String resource)
	{
		for(ListIterator iterator = lines.listIterator(); iterator.hasNext();)
		{
			String s = (String) iterator.next();
			if(s.startsWith("|" + resource + "|"))
				iterator.remove();
		}
	}

	private static String convertLinesToWikiText(List lines)
	{
		StringBuffer buffer = new StringBuffer();
		for(Iterator iterator = lines.iterator(); iterator.hasNext();)
		{
			String s = (String) iterator.next();
			buffer.append(s).append("\n");
		}
		return buffer.toString();
	}

	private static void trimExtraLines(List lines)
	{
		while(lines.size() > 100)
			lines.remove(100);
	}
}
