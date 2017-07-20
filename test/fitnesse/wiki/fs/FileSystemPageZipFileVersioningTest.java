// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class FileSystemPageZipFileVersioningTest {
  public static final int MAX_HISTORY_DEPTH = 3;
  public FileBasedWikiPage page;
  private VersionInfo firstVersion;
  private VersionInfo secondVersion;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    File rootPath = FitNesseUtil.createTemporaryFolder();
    ZipFileVersionsController versionsController = new ZipFileVersionsController(MAX_HISTORY_DEPTH);
    FileSystemPageFactory fileSystemPageFactory = new FileSystemPageFactory(new DiskFileSystem(), versionsController);
    root = fileSystemPageFactory.makePage(rootPath, "RooT", null, new SystemVariableSource());
    page = (FileBasedWikiPage) WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "original content");

    PageData data = page.getData();
    firstVersion = VersionInfo.makeVersionInfo(data);
    secondVersion = page.commit(data);
  }

  @Test
  public void aZipFileIsCreatedAfterUpdatingPageContent() throws Exception {
    File dir = page.getFileSystemPath();
    String[] filenames = dir.getParentFile().list();

    List<String> list = Arrays.asList(filenames);
    assertTrue(list.contains(firstVersion + ".zip"));
  }

  @Test
  public void originalContentCanBeRetrievedViaVersionInfo() throws Exception {
    PageData data = page.getData();
    data.setContent("new content");
    VersionInfo version = page.commit(data);

    PageData loadedData = page.getVersion(version.getName()).getData();
    assertEquals("original content", loadedData.getContent());
  }

  @Test
  public void testSubWikisDontInterfere() throws Exception {
    WikiPageUtil.addPage(page, PathParser.parse("SubPage"), "sub page content");
    try {
      page.commit(page.getData());
    } catch (Exception e) {
      fail("this exception should not have been thrown: " + e.getMessage());
    }
  }

  @Test
  public void canRetrieveVersions() throws Exception {
    PageData data = page.getData();
    data.setContent("new content");
    page.commit(data);
    Collection<VersionInfo> versionNames = page.getVersions();
    assertEquals(versionNames.toString(), 2, versionNames.size());
    assertTrue(versionNames.contains(firstVersion));
    assertTrue(versionNames.contains(secondVersion));
  }

  public DateFormat dateFormat() {
    return WikiImportProperty.getTimeFormat();
  }

  @Test
  public void oldVersionsAreRemovedOnCommit() throws Exception {
    PageData data = page.getData();

    Calendar modificationTime = Calendar.getInstance();
    modificationTime.add(Calendar.DATE, -1);
    String timeIndex1 = format(modificationTime);
    WikiPageProperties properties = new WikiPageProperties(data.getProperties());
    data.setProperties(properties);
    properties.setLastModificationTime(dateFormat().parse(timeIndex1));
    page.commit(data);
    modificationTime.add(Calendar.DATE, -1);
    String timeIndex2 = format(modificationTime);
    properties.setLastModificationTime(dateFormat().parse(timeIndex2));
    page.commit(data);
    modificationTime.add(Calendar.DATE, -1);
    properties.setLastModificationTime(dateFormat().parse(format(modificationTime)));
    page.commit(data);
    modificationTime.add(Calendar.DATE, -1);
    properties.setLastModificationTime(dateFormat().parse(format(modificationTime)));
    page.commit(data);

    Collection<VersionInfo> versions = page.getVersions();
    assertEquals(MAX_HISTORY_DEPTH, versions.size());

    List<VersionInfo> versionsList = new LinkedList<>(versions);
    Collections.sort(versionsList);
    assertTrue(versionsList.toString(), versionsList.get(0).toString().endsWith(timeIndex2));
    assertTrue(versionsList.toString(), versionsList.get(1).toString().endsWith(timeIndex1));
    assertEquals(versionsList.get(2), firstVersion);
  }

  private String format(Calendar modificationTime) {
    return WikiPageProperty.getTimeFormat().format(modificationTime.getTime());
  }

  @Test
  public void testGetContent() throws Exception {
    WikiPagePath alpha = PathParser.parse("AlphaAlpha");
    WikiPage a = WikiPageUtil.addPage(root, alpha, "a");

    PageData data = a.getData();
    assertEquals("a", data.getContent());
  }

  @Test
  public void testReplaceContent() throws Exception {
    WikiPagePath alpha = PathParser.parse("AlphaAlpha");
    WikiPage page = WikiPageUtil.addPage(root, alpha, "a");

    PageData data = page.getData();
    data.setContent("b");
    page.commit(data);
    assertEquals("b", page.getData().getContent());
  }

  @Test
  public void testSimpleVersionTasks() throws Exception {
    WikiPagePath path = PathParser.parse("MyPageOne");
    WikiPage page = WikiPageUtil.addPage(root, path, "old content");
    PageData data = page.getData();
    data.setContent("new content");
    VersionInfo previousVersion = page.commit(data);

    Collection<VersionInfo> versions = page.getVersions();
    assertEquals(1, versions.size());
    assertEquals(true, versions.contains(previousVersion));

    WikiPage loadedPage = page.getVersion(previousVersion.getName());
    assertEquals("old content", loadedPage.getData().getContent());
  }

  @Test
  public void testUserNameIsInVersionName() throws Exception {
    WikiPagePath testPagePath = PathParser.parse("TestPage");
    WikiPage testPage = WikiPageUtil.addPage(root, testPagePath, "version1");

    PageData data = testPage.getData();
    data.setAttribute(PageData.LAST_MODIFYING_USER, "Aladdin");
    VersionInfo record = testPage.commit(data);

    assertTrue(record.getName().startsWith("Aladdin"));
  }

  @Test
  public void testNoVersionException() throws Exception {
    WikiPagePath pageOnePath = PathParser.parse("PageOne");
    WikiPage page = WikiPageUtil.addPage(root, pageOnePath, "old content");
    try {
      page.getVersion("abc");
      fail("a NoSuchVersionException should have been thrown");
    } catch (NoSuchVersionException e) {
      assertEquals("There is no version 'abc'", e.getMessage());
    }
  }

  @Test
  public void testUnicodeInVersions() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "\uba80\uba81\uba82\uba83");
    PageData data = page.getData();
    data.setContent("blah");
    VersionInfo info = page.commit(data);

    data = page.getVersion(info.getName()).getData();
    String expected = "\uba80\uba81\uba82\uba83";
    String actual = data.getContent();

    assertEquals(expected, actual);
  }

  @Test
  public void testVersionedPropertiedLoadedProperly() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("TestPage"));
    PageData data = page.getData();
    WikiPageProperty oldProps = data.getProperties();
    WikiPageProperties props = new WikiPageProperties();
    props.set("MyProp", "my value");
    data.setProperties(props);
    page.commit(data);

    data.setProperties(oldProps);
    VersionInfo version = page.commit(data);

    PageData versionedData = page.getVersion(version.getName()).getData();
    WikiPageProperty versionedProps = versionedData.getProperties();

    assertTrue(versionedProps.has("MyProp"));
    assertEquals("my value", versionedProps.get("MyProp"));
  }

}
