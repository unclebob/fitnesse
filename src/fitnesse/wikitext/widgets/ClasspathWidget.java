// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.WidgetBuilder;

public class ClasspathWidget extends ParentWidget implements WidgetWithTextArgument
{
	public static final String REGEXP = "^!path [^\r\n]*";
	private static final Pattern pattern = Pattern.compile("^!path (.*)");
	private static WidgetBuilder variableWidgetBook = new WidgetBuilder(new Class[]{VariableWidget.class});
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
		return variableWidgetBook;
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