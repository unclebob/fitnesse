package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetBuilder;

public class MockWidgetRoot extends WidgetRoot
{
	public MockWidgetRoot() throws Exception
	{
		super(null, new PagePointer(new MockWikiPage("RooT"), new WikiPagePath()), WidgetBuilder.htmlWidgetBuilder);
	}

	protected void buildWidgets(String value) throws Exception
	{
	}
}
