// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.TestSuiteMaker;
import junit.framework.Test;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("widgets", new Class[]{
			ParentWidgetTest.class,
			TextWidgetTest.class,
			WikiWordWidgetTest.class,
			ItalicWidgetTest.class,
			BoldWidgetTest.class,
			PreformattedWidgetTest.class,
			HruleWidgetTest.class,
			HeaderWidgetTest.class,
			CenterWidgetTest.class,
			TableWidgetTest.class,
			TableRowWidgetTest.class,
			TableCellWidgetTest.class,
			ListWidgetTest.class,
			ListItemWidgetTest.class,
			ClasspathWidgetTest.class,
			ImageWidgetTest.class,
			LinkWidgetTest.class,
			TOCWidgetTest.class,
			AliasLinkWidgetTest.class,
			LiteralWidgetTest.class,
			NoteWidgetTest.class,
			CommentWidgetTest.class,
			VariableDefinitionWidgetTest.class,
			VariableWidgetTest.class,
			PreProcessorLiteralWidgetTest.class,
			StrikeWidgetTest.class,
			IncludeWidgetTest.class,
			LastModifiedWidgetTest.class,
			FixtureWidgetTest.class,
			TextIgnoringWidgetRootTest.class,
			WidgetVisitorTest.class,
			VirtualWikiWidgetTest.class,
			WidgetRootTest.class,
			XRefWidgetTest.class,
			MetaWidgetTest.class,
			EmailWidgetTest.class,
			AnchorDeclarationWidgetTest.class,
			AnchorMarkerWidgetTest.class,
			CollapsableWidgetTest.class
		});
	}
}
