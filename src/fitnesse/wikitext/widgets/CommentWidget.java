// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

public class CommentWidget extends TextWidget
{
	public static final String REGEXP = "^#[^\r\n]*(?:(?:\r\n)|\n|\r)?";

	public CommentWidget(ParentWidget parent, String text)
	{
		super(parent, text);
	}

	public String render() throws Exception
	{
		return "";
	}
}

