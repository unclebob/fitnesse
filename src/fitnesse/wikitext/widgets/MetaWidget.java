// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.*;

public class MetaWidget extends ParentWidget
{
	public static final String REGEXP = "^!meta [^\r\n]*";
	private static final Pattern pattern = Pattern.compile("^!meta (.*)");

	private String content;

	public MetaWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
			setContent(match.group(1));
	}

	private void setContent(String content) throws Exception
	{
		this.content = content;
		addChildWidgets(this.content);
	}

	public String render() throws Exception
	{
		return HtmlUtil.metaText(childHtml());
	}

	public String asWikiText() throws Exception
	{
		return "!meta " + content;
	}
}
