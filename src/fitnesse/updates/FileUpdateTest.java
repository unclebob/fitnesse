// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import java.io.File;

public class FileUpdateTest extends UpdateTest
{
	public final File testFile = new File("classes/testFile");

	protected Update makeUpdate() throws Exception
	{
		return new FileUpdate(updater, "testFile", "files/images");
	}

	public void setUp() throws Exception
	{
		super.setUp();
		testFile.createNewFile();
	}

	public void tearDown() throws Exception
	{
		super.tearDown();
		testFile.delete();
	}

	public void testSimpleFunctions() throws Exception
	{
		assertTrue("doesn't want to apply", update.shouldBeApplied());
		assertTrue("wrong starting of message", update.getMessage().startsWith("Installing file: "));
		assertTrue("wrong end of message", update.getMessage().endsWith("testFile"));
		assertEquals("FileUpdate(testFile)", update.getName());
	}

	public void testUpdateWithMissingDirectories() throws Exception
	{
		update.doUpdate();

		File file = new File(context.rootPagePath + "/files/images/testFile");
		assertTrue(file.exists());

		assertFalse(update.shouldBeApplied());
	}

	public void testFileMissing() throws Exception
	{
		update = new FileUpdate(updater, "images/missingFile", "files/images");

		try
		{
			update.doUpdate();
			fail();
		}
		catch(Exception e)
		{
		}
	}
}
