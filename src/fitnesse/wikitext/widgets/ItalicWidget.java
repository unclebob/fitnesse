// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.util.regex.*;

public class ItalicWidget extends ParentWidget
{
	public static final String REGEXP = "''.+?''";
	private static final Pattern pattern = Pattern.compile("''(.+?)''", Pattern.MULTILINE + Pattern.DOTALL);

	public ItalicWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
			addChildWidgets(match.group(1));
		else
			System.err.println("ItalicWidget: match was not found");
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("<i>");
		html.append(childHtml()).append("</i>");

		return html.toString();
	}

}

