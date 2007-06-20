// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.*;

public class VariableWidget extends ParentWidget
{
	public static final String REGEXP = "\\$\\{\\w+\\}";
	public static final Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}", Pattern.MULTILINE + Pattern.DOTALL);
	private String name = null;
	private String renderedText;
	private boolean rendered;

	public VariableWidget(ParentWidget parent, String text)
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			name = match.group(1);
		}
	}

	public String render() throws Exception
	{
		if(!rendered)
			doRender();
		return renderedText;
	}

	private void doRender() throws Exception
	{
		String value = parent.getVariable(name);
		if(value != null)
		{
			addChildWidgets(value);
			renderedText = childHtml();
		}
		else
			renderedText = makeUndefinedVariableExpression(name);
		rendered = true;
	}

	private String makeUndefinedVariableExpression(String name) throws Exception
	{
		return HtmlUtil.metaText("undefined variable: " + name);
	}

	public String asWikiText() throws Exception
	{
		return "${" + name + "}";
	}
}


