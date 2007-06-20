// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.*;

public class PreformattedWidget extends ParentWidget
{
	public static final String REGEXP = "\\{\\{\\{.+?\\}\\}\\}";
	private static final Pattern pattern = Pattern.compile("\\{{3}(.+?)\\}{3}", Pattern.DOTALL);

	public PreformattedWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
			addChildWidgets(match.group(1));
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("<pre>");
		html.append(childHtml()).append("</pre>");

		return html.toString();
	}

	public String asWikiText() throws Exception
	{
		return "{{{" + childWikiText() + "}}}";
	}

	public WidgetBuilder getBuilder()
	{
		return WidgetBuilder.literalAndVariableWidgetBuilder;
	}
}

