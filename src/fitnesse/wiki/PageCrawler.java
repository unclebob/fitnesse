// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.components.FitNesseTraversalListener;

//TODO after extracting the WikiPageModel... rethink this class.  Lots of these methods might be able to go back into WikiPAge.
public interface PageCrawler
{
	public WikiPage getPage(WikiPage context, WikiPagePath path) throws Exception;
	public void setDeadEndStrategy(PageCrawlerDeadEndStrategy strategy);
	public boolean pageExists(WikiPage context, WikiPagePath path) throws Exception;

	public WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath) throws Exception;
	public WikiPagePath getFullPath(WikiPage page) throws Exception;
	public WikiPage addPage(WikiPage context, WikiPagePath path, String content) throws Exception;
	public WikiPage addPage(WikiPage context, WikiPagePath path) throws Exception;  

	public String getRelativeName(WikiPage base, WikiPage page) throws Exception;
	public boolean isRoot(WikiPage page) throws Exception;
	public WikiPage getRoot(WikiPage page) throws Exception;

	public void traverse(WikiPage root, FitNesseTraversalListener pageCrawlerTest) throws Exception;
}