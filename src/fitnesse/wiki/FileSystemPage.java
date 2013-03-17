// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.wikitext.parser.WikiWordPath;
import util.Clock;
import util.DiskFileSystem;
import util.FileSystem;
import util.FileUtil;

public class FileSystemPage extends CachingPage {
  private static final long serialVersionUID = 1L;

  public static final String contentFilename = "/content.txt";
  public static final String propertiesFilename = "/properties.xml";

  private final String path;
  private final WikiPageFactory wikiPageFactory;
  private final FileSystem fileSystem;
  private final VersionsController versionsController;

  public FileSystemPage(final String path, final String name,
                        final WikiPageFactory wikiPageFactory, final FileSystem fileSystem, final VersionsController versionsController) {
    super(name, null);
    this.path = path;
    this.wikiPageFactory = wikiPageFactory;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  @Deprecated
  // TODO: not to be used in production code
  public FileSystemPage(final String path, final String name) {
    this(path, name, new FileSystemPageFactory(), new DiskFileSystem(), new ZipFileVersionsController());
  }

  public FileSystemPage(final String name, final FileSystemPage parent) {
    super(name, parent);
    path = parent.getFileSystemPath();
    wikiPageFactory = parent.wikiPageFactory;
    fileSystem = parent.fileSystem;
    versionsController = parent.versionsController;
  }

  @Override
  public void removeChildPage(final String name) {
    super.removeChildPage(name);
    String pathToDelete = getFileSystemPath() + "/" + name;
    final File fileToBeDeleted = new File(pathToDelete);
    FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
  }

  @Override
  public boolean hasChildPage(final String pageName) {
    final File f = new File(getFileSystemPath() + "/" + pageName);
    if (f.exists()) {
      addChildPage(pageName);
      return true;
    }
    return false;
  }

  // TODO: Do this in the VersionsController
  @Deprecated
  protected synchronized void saveContent(String content) {
    if (content == null) {
      return;
    }

    final String separator = System.getProperty("line.separator");

    if (content.endsWith("|")) {
      content += separator;
    }
    
    //First replace every windows style to unix
    content = content.replaceAll("\r\n", "\n");
    //Then do the replace to match the OS.  This works around
    //a strange behavior on windows.
    content = content.replaceAll("\n", separator);

    // Should use FileSystem instance instead
    String contentPath = getFileSystemPath() + contentFilename;
    try {
      fileSystem.makeFile(contentPath, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO: Do this in the VersionsController
  @Deprecated
  protected synchronized void saveAttributes(final WikiPageProperties attributes)
    {
    String propertiesFilePath = "<unknown>";
    try {
      propertiesFilePath = getFileSystemPath() + propertiesFilename;
      WikiPageProperties propertiesToSave = new WikiPageProperties(attributes);
      removeAlwaysChangingProperties(propertiesToSave);
      propertiesToSave.toXml();
      fileSystem.makeFile(propertiesFilePath, propertiesToSave.toXml());
    } catch (final Exception e) {
      throw new RuntimeException("Failed to save properties file: \""
        + propertiesFilePath + "\" (exception: " + e + ").", e);
    }
  }

  private void removeAlwaysChangingProperties(WikiPageProperties properties) {
    properties.remove(PageData.PropertyLAST_MODIFIED);
  }

  @Override
  protected WikiPage createChildPage(final String name) {
    return wikiPageFactory.makeChildPage(name, this);
  }

  private void loadContent(final PageData data) {
    String content = "";
    final String name = getFileSystemPath() + contentFilename;
    final File input = new File(name);
    try {
      if (input.exists()) {
        final byte[] bytes = readContentBytes(input);
        content = new String(bytes, "UTF-8");
      }
      data.setContent(content);
    } catch (IOException e) {
      throw new RuntimeException("Error while loading content", e);
    }
  }

  @Override
  protected void loadChildren() {
    final File thisDir = new File(getFileSystemPath());
    if (thisDir.exists()) {
      final String[] subFiles = thisDir.list();
      for (final String subFile : subFiles) {
        if (fileIsValid(subFile, thisDir) && !this.children.containsKey(subFile)) {
          this.children.put(subFile, getChildPage(subFile));
        }
      }
    }
  }

  private byte[] readContentBytes(final File input) throws IOException {
    // TODO: Read through FileSystem
    FileInputStream inputStream = null;
    try {
      final byte[] bytes = new byte[(int) input.length()];
      inputStream = new FileInputStream(input);
      inputStream.read(bytes);
      return bytes;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  private boolean fileIsValid(final String filename, final File dir) {
    if (WikiWordPath.isWikiWord(filename)) {
      final File f = new File(dir, filename);
      if (f.isDirectory()) {
        return true;
      }
    }
    return false;
  }

  private String getParentFileSystemPath() {
    return this.parent != null ? ((FileSystemPage) this.parent).getFileSystemPath() : this.path;
  }

  public String getFileSystemPath() {
    return getParentFileSystemPath() + "/" + getName();
  }

  public String getAbsoluteFileSystemPath() {
    return new File(getFileSystemPath()).getAbsolutePath();
  }

  private void loadAttributes(final PageData data) {
    final File file = new File(getFileSystemPath() + propertiesFilename);
    if (file.exists()) {
      try {
        long lastModifiedTime = getLastModifiedTime();
        attemptToReadPropertiesFile(file, data, lastModifiedTime);
      } catch (final Exception e) {
        System.err.println("Could not read properties file:" + file.getPath());
        e.printStackTrace();
      }
    }
  }

  private long getLastModifiedTime() {
    long lastModifiedTime = 0;

    final File file = new File(getFileSystemPath() + contentFilename);
    if (file.exists()) {
      lastModifiedTime = file.lastModified();
    } else {
      lastModifiedTime = Clock.currentTimeInMillis();
    }
    return lastModifiedTime;
  }

  private void attemptToReadPropertiesFile(File file, PageData data,
                                           long lastModifiedTime) throws FileNotFoundException {
    // TODO: read through FileSystem
    InputStream input = null;
    try {
      final WikiPageProperties props = new WikiPageProperties();
      input = new FileInputStream(file);
      props.loadFromXmlStream(input);
      props.setLastModificationTime(new Date(lastModifiedTime));
      data.setProperties(props);
    } finally {
      if (input != null)
        FileUtil.close(input);
    }
  }

  @Override
  public VersionInfo commit(final PageData data) {
    VersionInfo previousVersion = makeVersion();
    createDirectoryIfNewPage();
    saveContent(data.getContent());
    saveAttributes(data.getProperties());
    super.commit(data);
    return previousVersion;
  }

  @Override
  protected PageData makePageData() {
    final PageData pagedata = new PageData(this);
    loadContent(pagedata);
    loadAttributes(pagedata);
    return pagedata;
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return versionsController.history(this);
  }

  @Override
  public PageData getDataVersion(final String versionName) {
    return this.versionsController.getRevisionData(this, versionName);
  }

  @Deprecated
  // Move to persistence engine
  private void createDirectoryIfNewPage() {
    String pagePath = getFileSystemPath();
    if (!fileSystem.exists(pagePath)) {
      try {
        fileSystem.makeDirectory(pagePath);
      } catch (IOException e) {
        throw new RuntimeException("Unable to create directory for new page", e);
      }
    }
  }

  private VersionInfo makeVersion() {
    final PageData data = getData();
    return makeVersion(data);
  }

  protected VersionInfo makeVersion(final PageData data) {
    return this.versionsController.makeVersion(this, data);
  }

  @Override
  public String toString() {
    try {
      return getClass().getName() + " at " + this.getFileSystemPath();
    } catch (final Exception e) {
      return super.toString();
    }
  }
}
