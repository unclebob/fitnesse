// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

public class LineBreakWidget extends WikiWidget
{
	public static final String REGEXP = "(?:(?:\r\n)|\n|\r)";

	public LineBreakWidget(ParentWidget parent, String text)
	{
		super(parent);
	}

	public String render() throws Exception
	{
		return "<br>";
	}

	public String asWikiText() throws Exception
	{
		return "\n";
	}

}

