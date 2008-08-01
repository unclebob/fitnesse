// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

public class BlankParentWidget extends ParentWidget
{
	public BlankParentWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		addChildWidgets(text);
	}

	public String render() throws Exception
	{
		return "";
	}

	public String asWikiText() throws Exception
	{
		return "";
	}
}
