// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.ParentWidget;

public abstract class WikiWidget
{
	protected ParentWidget parent = null;

	protected WikiWidget(ParentWidget parent)
	{
		this.parent = parent;
		addToParent();
	}

	protected void addToParent()
	{
		if(this.parent != null)
			this.parent.addChild(this);
	}

	//TODO-DaC what's a better name for this?
	public abstract String render() throws Exception;

	public void acceptVisitor(WidgetVisitor visitor) throws Exception
	{
		visitor.visit(this);
	}

	public WikiPage getWikiPage()
	{
		return parent.getWikiPage();
	}

	public String asWikiText() throws Exception
	{
		return getClass().toString() + ".asWikiText()";
	}
}

