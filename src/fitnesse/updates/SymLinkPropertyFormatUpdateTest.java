// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.ByteArrayInputStream;
import java.io.File;

import util.FileUtil;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPageProperties;

public class SymLinkPropertyFormatUpdateTest extends UpdateTestCase {
  private static String badPropertiesFormat =
    "<?xml version=\"1.0\"?>\n" +
      "<properties>\n" +
      "        <Edit/>\n" +
      "        <symbolicLink>\n" +
      "                <name>SomePage</name>\n" +
      "                <path>RecentChanges</path>\n" +
      "        </symbolicLink>\n" +
      "        <symbolicLink>\n" +
      "                <name>PageTwo</name>\n" +
      "                <path>PageHeader</path>\n" +
      "        </symbolicLink>\n" +
      "</properties>";

  private void setupPropertiesFilesThatNeedsFixing() throws Exception {
    PageData data = pageOne.getData();
    WikiPageProperties properties = new WikiPageProperties();
    properties.loadFromXmlStream(new ByteArrayInputStream(badPropertiesFormat.getBytes()));
    data.setProperties(properties);
    pageOne.commit(data);

    File propertiesFile = getSamplePropertiesFile();
    FileUtil.createFile(propertiesFile, badPropertiesFormat);
  }

  private File getSamplePropertiesFile() throws Exception {
    FileSystemPage page = (FileSystemPage) pageOne;
    return new File(page.getFileSystemPath() + FileSystemPage.propertiesFilename);
  }

  public void testPropertiesFixed() throws Exception {
    setupPropertiesFilesThatNeedsFixing();
    update.doUpdate();

    String newContent = FileUtil.getFileContent(getSamplePropertiesFile());
    newContent = newContent.replace("\r", "");

    WikiPageProperties properties = pageOne.getData().getProperties();
    assertFalse(properties.has("symbolicLink"));
    assertTrue(properties.has(SymbolicPage.PROPERTY_NAME));
    assertEquals("RecentChanges", properties.getProperty(SymbolicPage.PROPERTY_NAME).get("SomePage"));
    assertEquals("PageHeader", properties.getProperty(SymbolicPage.PROPERTY_NAME).get("PageTwo"));
    assertTrue(properties.has("Edit"));
  }

  protected Update makeUpdate() throws Exception {
    return new SymLinkPropertyFormatUpdate(updater);
  }
}
