// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.*;

public class MovedPageReferenceRenamingVisitor implements WidgetVisitor
{
	private WikiPage pageToBeMoved;
	private String newParentName;

	public MovedPageReferenceRenamingVisitor(WikiPage pageToBeMoved, String newParentName)
	{
		this.pageToBeMoved = pageToBeMoved;
		this.newParentName = newParentName;
	}

	public void visit(AliasLinkWidget widget) throws Exception
	{
	}

	public void visit(WikiWidget widget) throws Exception
	{
	}

	public void visit(WikiWordWidget widget) throws Exception
	{
		widget.renameMovedPageIfReferenced(pageToBeMoved, newParentName);
	}
}
