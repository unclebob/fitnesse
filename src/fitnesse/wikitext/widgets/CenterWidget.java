// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.util.regex.*;

public class CenterWidget extends ParentWidget
{
	public static final String REGEXP = "^![cC] [^\r\n]*" + LineBreakWidget.REGEXP + "?";
	private static final Pattern pattern = Pattern.compile("^![cC] (.*)");

	public CenterWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
			addChildWidgets(match.group(1));
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("<div class=\"centered\">");
		html.append(childHtml()).append("</div>");
		return html.toString();
	}
}
