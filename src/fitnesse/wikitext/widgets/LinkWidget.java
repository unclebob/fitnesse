// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.*;

public class LinkWidget extends ParentWidget
{
	public static final String REGEXP = "https?://[^\\s]+[^\\s.)]+";
	private static final Pattern pattern = Pattern.compile("https?://([^/\\s]*)(\\S*)?");

	public LinkWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		addChildWidgets(text);
	}

	public String render() throws Exception
	{
		String linkText = childHtml();
		String usableURL = makeUrlUsable(linkText);
		StringBuffer html = new StringBuffer("<a href=\"");
		html.append(usableURL);
		html.append("\">");
		html.append(linkText);
		html.append("</a>");

		return html.toString();
	}

	public static String makeUrlUsable(String url)
	{
		String usableUrl = url;
		Matcher match = pattern.matcher(url);
		if(match.find())
		{
			String host = match.group(1);
			String resource = match.group(2);
			if("files".equals(host))
				usableUrl = "/files" + resource;
		}

		return usableUrl;
	}

	public WidgetBuilder getBuilder()
	{
		return WidgetBuilder.variableWidgetBuilder;
	}

	public String asWikiText() throws Exception
	{
		return childWikiText();
	}
}


