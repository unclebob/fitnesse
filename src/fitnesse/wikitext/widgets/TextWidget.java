// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.*;

public class TextWidget extends WikiWidget implements WidgetWithTextArgument
{
	protected String text;

	public TextWidget(ParentWidget parent)
	{
		super(parent);
	}

	public TextWidget(ParentWidget parent, String text)
	{
		super(parent);
		this.text = text;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String newText)
	{
		text = newText;
	}

	public String render() throws Exception
	{
		String html = getText();
		if(parent.doEscaping())
			html = Utils.escapeText(html);

		return html;
	}

	public String toString()
	{
		return super.toString() + " : " + getText();
	}

	public String asWikiText() throws Exception
	{
		return getText();
	}
}
