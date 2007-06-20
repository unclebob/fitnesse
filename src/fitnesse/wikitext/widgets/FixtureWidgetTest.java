// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;

public class FixtureWidgetTest extends WidgetTest
{
	public void testFixtureWidgetRendersProperly() throws Exception
	{
		assertWidgetRendersToContain("!fixture some.FixtureName", "fixture: some.FixtureName");
	}

	private void assertWidgetRendersToContain(final String text, final String substring)
		throws Exception
	{
		WikiWidget widget = makeWidget(text);
		String html = widget.render();
		assertSubString(substring, html);
	}

	private FixtureWidget makeWidget(final String text)
		throws Exception
	{
		return new FixtureWidget(new MockWidgetRoot(), text);
	}

	public void testAsWikiText() throws Exception
	{
		final String FIXTURE_WIDGET = "!fixture myFixture";
		FixtureWidget w = new FixtureWidget(new MockWidgetRoot(), FIXTURE_WIDGET);
		assertEquals(FIXTURE_WIDGET, w.asWikiText());
	}

	protected String getRegexp()
	{
		return FixtureWidget.REGEXP;
	}
}
