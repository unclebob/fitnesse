// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.ColumnFixture;
import fitnesse.wikitext.widgets.WidgetRoot;

public class RenderFixture extends ColumnFixture
{
	public String text;

	public String rendered() throws Exception
	{
		WidgetRoot root = new WidgetRoot(text, FitnesseFixtureContext.root);
		return root.render();
	}

}
