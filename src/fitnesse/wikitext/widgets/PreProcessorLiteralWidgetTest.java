// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

public class PreProcessorLiteralWidgetTest extends WidgetTest
{
	private WidgetRoot root;

	public void setUp() throws Exception
	{
		root = new MockWidgetRoot();
	}

	protected String getRegexp()
	{
		return PreProcessorLiteralWidget.REGEXP;
	}

	public void testMatches() throws Exception
	{
		assertMatches("!-literal-!");
		assertMatches("!-this is a literal-!");
		assertMatches("!-this is\n a literal-!");
		assertMatches("!- !- !-this is a literal-!");
		assertMatches("!-!literal-!");
		assertMatches("!--!");
		assertNoMatch("!-no");
		assertNoMatch("! -no-!");
		assertMatchEquals("!-no-!-!", "!-no-!");
	}

	public void testRender() throws Exception
	{
		PreProcessorLiteralWidget widget = new PreProcessorLiteralWidget(root, "!-abc-!");
		assertEquals("!lit(0)", widget.render());
		assertEquals("abc", root.getLiteral(0));
	}

	public void testAsWikiText() throws Exception
	{
		PreProcessorLiteralWidget widget = new PreProcessorLiteralWidget(root, "!-abc-!");
		assertEquals("!-abc-!", widget.asWikiText());
	}
}
