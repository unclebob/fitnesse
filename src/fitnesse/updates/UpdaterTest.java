// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.wiki.PathParser;

import java.io.File;

public class UpdaterTest extends UpdateTest
{

	public void setUp() throws Exception
	{
		super.setUp();
		Updater.testing = true;
		crawler.addPage(root, PathParser.parse("PageOne"));
	}

	public void testProperties() throws Exception
	{
		File file = new File("testDir/RooT/properties");
		assertFalse(file.exists());
		Updater updater = new Updater(context);
		updater.updates = new Update[]{};
		updater.update();
		assertTrue(file.exists());
	}
}
