// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.*;

public class PageReferenceRenamingVisitor implements WidgetVisitor
{
	private WikiPage pageToRename;
	private String newName;

	public PageReferenceRenamingVisitor(WikiPage pageToRename, String newName)
	{
		this.pageToRename = pageToRename;
		this.newName = newName;
	}

	public void visit(WikiWidget widget) throws Exception
	{
	}

	public void visit(WikiWordWidget widget) throws Exception
	{
		widget.renamePageIfReferenced(pageToRename, newName);
	}

	public void visit(AliasLinkWidget widget) throws Exception
	{
		widget.renamePageIfReferenced(pageToRename, newName);
	}
}
