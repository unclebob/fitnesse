// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.*;

public class VariableDefinitionWidget extends ParentWidget
{
	public static final String REGEXP = "^!define \\w+ (?:(?:\\{[^}]*\\})|(?:\\([^)]*\\)))";
	private static final Pattern pattern =
		Pattern.compile("^!define (\\w+) ([\\{\\(])(.*)[\\}\\)]",
		                Pattern.DOTALL + Pattern.MULTILINE);
	public String name;
	public String value;

	public VariableDefinitionWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			name = match.group(1);
			value = match.group(3);
		}
	}

	public String render() throws Exception
	{
		this.parent.addVariable(name, value);
		return HtmlUtil.metaText("variable defined: " + name + "=" + value);
	}

	public String asWikiText() throws Exception
	{
		String text = "!define " + name + " ";
		if(value.indexOf("{") == -1)
			text += "{" + value + "}";
		else
			text += "(" + value + ")";
		return text;
	}
}
