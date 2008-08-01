// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetBuilder;

public class MockWidgetRoot extends WidgetRoot
{
	public MockWidgetRoot() throws Exception
	{
		super(null, new PagePointer(new WikiPageDummy("RooT"), new WikiPagePath()), WidgetBuilder.htmlWidgetBuilder);
	}

	protected void buildWidgets(String value) throws Exception
	{
	}
}
