// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

import java.util.regex.*;

public class VirtualWikiWidget extends WikiWidget
{
	public static final String REGEXP = "^!virtualwiki http://[^\r\n]*";
	private static final Pattern pattern = Pattern.compile("!virtualwiki (http://.*)");

	public String url;

	public VirtualWikiWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
			url = match.group(1);
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("");
		html.append("<span class=\"meta\">");
		html.append("!virtualwiki has been deprecated.  Use the Properties button instead.");
		html.append("</span>");
		return html.toString();
	}

	public String getRemoteUrl()
	{
		return url;
	}
}
