// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.*;

public class XRefWidget extends ParentWidget implements WidgetWithTextArgument
{
	public static final String REGEXP = "^!see " + WikiWordWidget.REGEXP;
	private static final Pattern pattern = Pattern.compile("^!see (.*)");
	private String pageName;

	public XRefWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			pageName = match.group(1);
			addChildWidgets(pageName);
		}
	}

	public String render() throws Exception
	{
		return HtmlUtil.metaText("<b>See: " + childHtml() + "</b>");
	}

	public String asWikiText() throws Exception
	{
		return "!see " + pageName;
	}

	public String getText()
	{
		return pageName;
	}
}