// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

public class ListItemWidget extends ParentWidget
{
	private int level;

	public ListItemWidget(ParentWidget parent, String text, int level) throws Exception
	{
		super(parent);
		this.level = level;
		addChildWidgets(text);
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer();
		for(int i = 0; i < level; i++)
			html.append("\t");
		html.append("<li>").append(childHtml()).append("</li>\n");

		return html.toString();
	}
}
