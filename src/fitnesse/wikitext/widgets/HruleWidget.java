// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

import java.util.regex.*;

public class HruleWidget extends WikiWidget
{
	public static final String REGEXP = "-{4,}";
	private static final Pattern pattern = Pattern.compile("-{4}(-*)");

	private int size = 0;

	public HruleWidget(ParentWidget parent, String text)
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
			size = match.group(1).length();
	}

	public int size()
	{
		return size;
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("<hr");
		if(size > 0)
			html.append(" size=\"").append(size + 1).append("\"");
		html.append(">");

		return html.toString();
	}
}

