// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.wiki.FileSystemPage;

import java.io.*;
import java.util.Properties;

public class PropertiesToXmlUpdateTest extends UpdateTest
{
	private String pageOneOldFilename = "testDir/RooT/PageOne" + PropertiesToXmlUpdate.old_propertiesFilename;
	private String pageTwoOldFilename = "testDir/RooT/PageOne/PageTwo" + PropertiesToXmlUpdate.old_propertiesFilename;
	private String pageOneXmlFilename = "testDir/RooT/PageOne" + FileSystemPage.propertiesFilename;
	private String pageTwoXmlFilename = "testDir/RooT/PageOne/PageTwo" + FileSystemPage.propertiesFilename;

	public void setUp() throws Exception
	{
		super.setUp();
		deleteXmlPropertiesFiles();
		Properties props = makeSampleProperties();
		writeOldPropertiesFiles(props);
	}

	public void testThatItWorks() throws Exception
	{
		update.doUpdate();
		assertTrue(new File(pageOneXmlFilename).exists());
		assertFalse(new File(pageOneOldFilename).exists());
		assertTrue(new File(pageTwoXmlFilename).exists());
		assertFalse(new File(pageTwoOldFilename).exists());
	}

	protected Update makeUpdate() throws Exception
	{
		return new PropertiesToXmlUpdate(updater);
	}

	private void writeOldPropertiesFiles(Properties props) throws IOException
	{
		OutputStream os1 = new FileOutputStream(pageOneOldFilename);
		props.store(os1, "test");
		os1.close();
		OutputStream os2 = new FileOutputStream(pageTwoOldFilename);
		props.store(os2, "test");
		os2.close();
	}

	private Properties makeSampleProperties()
	{
		Properties props = new Properties();
		props.put("Key1", "value1");
		props.put("Key2", "value2");
		props.put("Key3", "false");
		return props;
	}

	private void deleteXmlPropertiesFiles()
	{
		new File(pageOneXmlFilename).delete();
		new File(pageTwoXmlFilename).delete();
	}

}
