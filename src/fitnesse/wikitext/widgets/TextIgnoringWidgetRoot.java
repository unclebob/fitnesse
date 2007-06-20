// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.Matcher;

public class TextIgnoringWidgetRoot extends WidgetRoot
{
	public TextIgnoringWidgetRoot(String value, WikiPage page, WidgetBuilder builder) throws Exception
	{
		super(value, page, builder);
	}

	public void addChildWidgets(String value) throws Exception
	{
		Matcher m = getBuilder().getWidgetPattern().matcher(value);
		if(m.find())
		{
			getBuilder().makeWidget(this, m);
			String postString = value.substring(m.end(), value.length());
			if(!postString.equals(""))
				addChildWidgets(postString);
		}
	}
}

