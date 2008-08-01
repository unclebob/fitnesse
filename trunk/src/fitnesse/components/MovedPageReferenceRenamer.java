// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.*;

public class MovedPageReferenceRenamer extends ReferenceRenamer
{
	private WikiPage pageToBeMoved;
	private String newParentName;

	public MovedPageReferenceRenamer(WikiPage root)
	{
		super(root);
	}

	public void renameReferences(WikiPage pageToBeMoved, String newParentName) throws Exception
	{
		this.pageToBeMoved = pageToBeMoved;
		this.newParentName = newParentName;
		renameReferences();
	}

	protected WidgetVisitor getVisitor()
	{
		return new MovedPageReferenceRenamingVisitor(pageToBeMoved, newParentName);
	}

	public String getSearchPattern() throws Exception
	{
		return pageToBeMoved.getName();
	}
}
