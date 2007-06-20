// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;

import java.util.regex.*;

public class FixtureWidget extends TextWidget
{
	public static final String REGEXP = "^!fixture [^\r\n]*";
	private static final Pattern pattern = Pattern.compile("^!fixture (.*)");

	public FixtureWidget(ParentWidget parent, String text)
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
			this.text = match.group(1);
	}

	public String render() throws Exception
	{
		return HtmlUtil.metaText("fixture: " + getText());
	}

	public String asWikiText() throws Exception
	{
		return "!fixture " + this.text;
	}
}