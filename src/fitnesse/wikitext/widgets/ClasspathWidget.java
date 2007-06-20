// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.*;

public class ClasspathWidget extends ParentWidget implements WidgetWithTextArgument
{
	public static final String REGEXP = "^!path [^\r\n]*";
	private static final Pattern pattern = Pattern.compile("^!path (.*)");
	private String pathText;

	public ClasspathWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			pathText = match.group(1);
			addChildWidgets(pathText);
		}
	}

	public WidgetBuilder getBuilder()
	{
		return WidgetBuilder.variableWidgetBuilder;
	}

	public String render() throws Exception
	{
		return HtmlUtil.metaText("classpath: " + childHtml());
	}

	public String asWikiText() throws Exception
	{
		return "!path " + pathText;
	}

	public String getText() throws Exception
	{
		return childHtml();
	}
}