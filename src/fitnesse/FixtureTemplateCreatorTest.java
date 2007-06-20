// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.fixtures.PrimeNumberRowFixture;
import junit.framework.TestCase;

public class FixtureTemplateCreatorTest extends TestCase
{
	private FixtureTemplateCreator templateCreator = null;

	public void setUp()
	{
		if(templateCreator == null)
			templateCreator = new FixtureTemplateCreator();
	}

	public void testGetShortClassName() throws Exception
	{
		assertEquals("Three", templateCreator.getShortClassName("one.two.Three"));
		assertEquals("ClassName", templateCreator.getShortClassName("ClassName"));
	}

	public void testFixClassName() throws Exception
	{
		assertEquals("Object[]", templateCreator.fixClassName("Object;"));
		assertEquals("Object", templateCreator.fixClassName("Object"));
	}

	public void testGetTargetClassFromRowFixture() throws Exception
	{
		Class targetClass = templateCreator.getTargetClassFromRowFixture(PrimeNumberRowFixture.class);
		assertNotNull(targetClass);
		assertEquals("fitnesse.fixtures.PrimeData", targetClass.getName());
	}
}
