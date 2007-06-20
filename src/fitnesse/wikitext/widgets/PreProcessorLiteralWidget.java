// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

import java.util.regex.*;

public class PreProcessorLiteralWidget extends WikiWidget
{
	public static final String REGEXP = "!-.*?-!";
	public static final Pattern pattern = Pattern.compile("!-(.*?)-!", Pattern.MULTILINE + Pattern.DOTALL);
	private String literal = null;
	private int literalNumber;

	public PreProcessorLiteralWidget(ParentWidget parent, String text)
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			literal = match.group(1);
			literalNumber = this.parent.defineLiteral(literal);
		}
	}

	public String render() throws Exception
	{
		return "!lit(" + literalNumber + ")";
	}

	public String asWikiText() throws Exception
	{
		return "!-" + literal + "-!";
	}
}
