// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

import java.util.regex.*;

public class LiteralWidget extends WikiWidget
{
	public static final String REGEXP = "!lit\\(\\d+\\)";
	public static final Pattern pattern = Pattern.compile("!lit\\((\\d+)\\)", Pattern.MULTILINE + Pattern.DOTALL);
	private int literalNumber;

	public LiteralWidget(ParentWidget parent, String text)
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			literalNumber = Integer.parseInt(match.group(1));
		}
	}

	public String render() throws Exception
	{
		return parent.getLiteral(literalNumber);
	}

	public String asWikiText() throws Exception
	{
		return "!-" + parent.getLiteral(literalNumber) + "-!";
	}
}

