// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.util.regex.*;

public class HeaderWidget extends ParentWidget
{
	public static final String REGEXP = "^![123] [^\r\n]*(?:(?:\r\n)|\n|\r)?";
	private static final Pattern pattern = Pattern.compile("!([123]) (.*)");

	private int size = 3;

	public HeaderWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			size = Integer.valueOf(match.group(1)).intValue();
			addChildWidgets(match.group(2));
		}
	}

	public int size()
	{
		return size;
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("<h");
		html.append(size).append(">").append(childHtml());
		html.append("</h").append(size).append(">");

		return html.toString();
	}
}
