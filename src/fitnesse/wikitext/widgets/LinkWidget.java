// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkWidget extends WikiWidget
{
	public static final String REGEXP = "https?://[^\\s]+[^\\s.)]+";
	private static final Pattern pattern = Pattern.compile("https?://([^/\\s]*)(\\S*)?");

	private String linkText;
	private String usableURL;

	public LinkWidget(ParentWidget parent, String text)
	{
		super(parent);
		linkText = text;
		usableURL = makeUrlUsable(linkText);
	}

	public String render() throws Exception
	{
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

	public String asWikiText() throws Exception
	{
		return linkText;
	}
}


