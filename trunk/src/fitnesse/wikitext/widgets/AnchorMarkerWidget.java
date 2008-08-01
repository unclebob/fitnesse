// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.WikiWidget;

import java.util.regex.*;

public class AnchorMarkerWidget extends WikiWidget
{
	public static final String REGEXP = ".#\\w+";
	private static final Pattern pattern = Pattern.compile(".#(\\w*)");

	private String text, anchorName;

	public AnchorMarkerWidget(ParentWidget parent, String text)
	{
		super(parent);
		this.text = text;
		Matcher match = pattern.matcher(this.text);
		if(match.find())
			anchorName = match.group(1);
	}

	public String render() throws Exception
	{
		return HtmlUtil.makeLink("#" + anchorName, ".#" + anchorName).html();
	}
}
