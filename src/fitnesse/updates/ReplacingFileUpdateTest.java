// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.util.FileUtil;

import java.io.*;

public class ReplacingFileUpdateTest extends UpdateTest
{
	public final String sourceFilename = "classes/testFile";
	public final File sourceFile = new File(sourceFilename);

	public final String destDirName = "subDir";
	public final String destPath = UpdateTest.testDir + "/" + UpdateTest.rootName + "/" + destDirName + "/testFile";
	public final File destFile = new File(destPath);

	public void setUp() throws Exception
	{
		super.setUp();
		sourceFile.createNewFile();
	}

	public void tearDown() throws Exception
	{
		super.tearDown();
		sourceFile.delete();
	}

	protected Update makeUpdate() throws Exception
	{
		return new ReplacingFileUpdate(updater, "testFile", destDirName);
	}

	public void testNoDestination() throws Exception
	{
		assertTrue(update.shouldBeApplied());
		update.doUpdate();
		assertTrue(destFile.exists());
	}

	public void testFileMatch() throws Exception
	{
		update.doUpdate();
		assertFalse(update.shouldBeApplied());
	}

	public void testFileDiffer() throws Exception
	{
		update.doUpdate();

		FileOutputStream output = new FileOutputStream(sourceFile);
		output.write("hello".getBytes());
		output.close();

		assertTrue(update.shouldBeApplied());
		update.doUpdate();

		assertEquals("hello", FileUtil.getFileContent(destFile));
	}
}