// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

public class HtmlComment extends HtmlTag
{
	public String comment;

	public HtmlComment(String comment)
	{
		super("commant");
		this.comment = comment;
	}

	public String html(int depth) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		addTabs(depth, buffer);
		buffer.append("<!--").append(comment).append("-->").append(endl);
		return buffer.toString();
	}
}
