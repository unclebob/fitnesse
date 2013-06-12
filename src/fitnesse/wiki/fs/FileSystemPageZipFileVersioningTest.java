// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import fitnesse.wiki.*;
import junit.framework.TestCase;
import util.FileUtil;

import java.io.File;
import java.text.DateFormat;
import java.util.*;

public class FileSystemPageZipFileVersioningTest extends TestCase {
  public FileSystemPage page;
  private VersionInfo firstVersion;
  private VersionInfo secondVersion;
  private PageCrawler crawler;
  private WikiPage root;
  private ZipFileVersionsController versionsController;

  @Override
  public void setUp() throws Exception {
    versionsController = new ZipFileVersionsController();
    root = new FileSystemPage("TestDir", "RooT", new DiskFileSystem(), versionsController);
    crawler = root.getPageCrawler();
    page = (FileSystemPage) crawler.addPage(root, PathParser.parse("PageOne"), "original content");

    PageData data = page.getData();
    firstVersion = VersionInfo.makeVersionInfo(data);
    secondVersion = page.commit(data);
  }

  @Override
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("TestDir");
  }

  public void testSave() throws Exception {
    String dirPath = page.getFileSystemPath();
    File dir = new File(dirPath);
    String[] filenames = dir.list();

    List<String> list = Arrays.asList(filenames);
    assertTrue(list.contains(firstVersion + ".zip"));
  }

  public void testLoad() throws Exception {
    PageData data = page.getData();
    data.setContent("new content");
    VersionInfo version = page.commit(data);

    PageData loadedData = page.getDataVersion(version.getName());
    assertEquals("original content", loadedData.getContent());
  }

  public void testGetVersions() throws Exception {
    Collection<VersionInfo> versionNames = page.getVersions();
    assertEquals(1, versionNames.size());
    assertTrue(versionNames.contains(firstVersion));
  }

  public void testSubWikisDontInterfere() throws Exception {
    crawler.addPage(page, PathParser.parse("SubPage"), "sub page content");
    try {
      page.commit(page.getData());
    } catch (Exception e) {
      fail("this exception should not have been thrown: " + e.getMessage());
    }
  }

  public void testTwoVersions() throws Exception {
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

  public void testVersionsExpire() throws Exception {
    versionsController.setHistoryDepth(3);
    PageData data = page.getData();

    Calendar modificationTime = Calendar.getInstance();
    modificationTime.add(Calendar.DATE, -1);
    String timeIndex1 = format(modificationTime);
    data.getProperties().setLastModificationTime(dateFormat().parse(timeIndex1));
    versionsController.makeZipVersion(page, data);
    modificationTime.add(Calendar.DATE, -1);
    String timeIndex2 = format(modificationTime);
    data.getProperties().setLastModificationTime(dateFormat().parse(timeIndex2));
    versionsController.makeZipVersion(page, data);
    modificationTime.add(Calendar.DATE, -1);
    data.getProperties().setLastModificationTime(dateFormat().parse(format(modificationTime)));
    versionsController.makeZipVersion(page, data);
    modificationTime.add(Calendar.DATE, -1);
    data.getProperties().setLastModificationTime(dateFormat().parse(format(modificationTime)));
    versionsController.makeZipVersion(page, data);

    Collection<VersionInfo> versions = page.getVersions();
    assertEquals(3, versions.size());

    List<VersionInfo> versionsList = new LinkedList<VersionInfo>(versions);
    Collections.sort(versionsList);
    assertTrue(versionsList.toString(), versionsList.get(0).toString().endsWith(timeIndex2));
    assertTrue(versionsList.toString(), versionsList.get(1).toString().endsWith(timeIndex1));
    assertEquals(versionsList.get(2), firstVersion);
  }

  private String format(Calendar modificationTime) {
    return WikiPageProperty.getTimeFormat().format(modificationTime.getTime());
  }

  public void testGetContent() throws Exception {
    WikiPagePath alpha = PathParser.parse("AlphaAlpha");
    WikiPage a = crawler.addPage(root, alpha, "a");

    PageData data = a.getData();
    assertEquals("a", data.getContent());
  }

  public void testReplaceContent() throws Exception {
    WikiPagePath alpha = PathParser.parse("AlphaAlpha");
    WikiPage page = crawler.addPage(root, alpha, "a");

    PageData data = page.getData();
    data.setContent("b");
    page.commit(data);
    assertEquals("b", page.getData().getContent());
  }

  public void testSetAttributes() throws Exception {
    PageData data = root.getData();
    data.setAttribute("Test", "true");
    data.setAttribute("Search", "true");
    root.commit(data);
    assertTrue(root.getData().hasAttribute("Test"));
    assertTrue(root.getData().hasAttribute("Search"));

    assertEquals("true", root.getData().getAttribute("Test"));
  }

  public void testSimpleVersionTasks() throws Exception {
    WikiPagePath path = PathParser.parse("MyPageOne");
    WikiPage page = crawler.addPage(root, path, "old content");
    PageData data = page.getData();
    data.setContent("new content");
    VersionInfo previousVersion = page.commit(data);

    Collection<VersionInfo> versions = page.getVersions();
    assertEquals(1, versions.size());
    assertEquals(true, versions.contains(previousVersion));

    PageData loadedData = page.getDataVersion(previousVersion.getName());
    assertSame(page, loadedData.getWikiPage());
    assertEquals("old content", loadedData.getContent());
  }

  public void testUserNameIsInVersionName() throws Exception {
    WikiPagePath testPagePath = PathParser.parse("TestPage");
    WikiPage testPage = crawler.addPage(root, testPagePath, "version1");

    PageData data = testPage.getData();
    data.setAttribute(PageData.LAST_MODIFYING_USER, "Aladdin");
    VersionInfo record = testPage.commit(data);

    assertTrue(record.getName().startsWith("Aladdin"));
  }

  public void testNoVersionException() throws Exception {
    WikiPagePath pageOnePath = PathParser.parse("PageOne");
    WikiPage page = crawler.addPage(root, pageOnePath, "old content");
    try {
      page.getDataVersion("abc");
      fail("a NoSuchVersionException should have been thrown");
    } catch (NoSuchVersionException e) {
      assertEquals("There is no version 'abc'", e.getMessage());
    }
  }

  public void testUnicodeInVersions() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "\uba80\uba81\uba82\uba83");
    PageData data = page.getData();
    data.setContent("blah");
    VersionInfo info = page.commit(data);

    data = page.getDataVersion(info.getName());
    String expected = "\uba80\uba81\uba82\uba83";
    String actual = data.getContent();

    assertEquals(expected, actual);
  }

  public void testVersionedPropertiedLoadedProperly() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
    PageData data = page.getData();
    WikiPageProperties oldProps = data.getProperties();
    WikiPageProperties props = new WikiPageProperties();
    props.set("MyProp", "my value");
    data.setProperties(props);
    page.commit(data);

    data.setProperties(oldProps);
    VersionInfo version = page.commit(data);

    PageData versionedData = page.getDataVersion(version.getName());
    WikiPageProperties versionedProps = versionedData.getProperties();

    assertTrue(versionedProps.has("MyProp"));
    assertEquals("my value", versionedProps.get("MyProp"));
  }

}
