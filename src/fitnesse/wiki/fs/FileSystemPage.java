// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.WikiWordPath;
import util.FileUtil;

public class FileSystemPage extends BaseWikiPage {
  private static final long serialVersionUID = 1L;

  static final String contentFilename = "content.txt";
  static final String propertiesFilename = "properties.xml";

  // Only used for root page:
  private final String path;

  private final transient FileSystem fileSystem;
  private final transient VersionsController versionsController;
  private boolean autoCommit;

  public FileSystemPage(final String path, final String name, final FileSystem fileSystem,
                        final VersionsController versionsController, final SymbolicPageFactory symbolicPageFactory,
                        final VariableSource variableSource) {
    super(name, symbolicPageFactory, variableSource);
    this.path = path;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  public FileSystemPage(final String name, final FileSystemPage parent) {
    this(name, parent, parent.fileSystem, parent.versionsController);
    autoCommit = parent.autoCommit;
  }

  public FileSystemPage(final String name, final FileSystemPage parent,
                        final FileSystem fileSystem, final VersionsController versionsController) {
    super(name, parent);
    path = null;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  @Override
  public void removeChildPage(final String name) {
    final WikiPage childPage = getChildPage(name);
    if (childPage instanceof FileSystemPage) {
      versionsController.delete(new FileVersion() {
        @Override
        public File getFile() {
          return new File(((FileSystemPage) childPage).getFileSystemPath());
        }

        @Override
        public InputStream getContent() throws IOException {
          return null;
        }

        @Override
        public String getAuthor() {
          // Who is deleting this page??
          return "";
        }

        @Override
        public Date getLastModificationTime() {
          return new Date();
        }
      });
    }
  }

  @Override
  public boolean hasChildPage(final String pageName) {
    final File file = new File(getFileSystemPath(), pageName);
    if (fileSystem.exists(file)) {
      addChildPage(pageName);
      return true;
    }
    return false;
  }

  @Override
  public WikiPage addChildPage(String name) {
    File path = new File(getFileSystemPath(), name);
    if (hasContentChild(path)) {
      return new FileSystemPage(name, this);
    } else if (hasHtmlChild(path)) {
      return new ExternalSuitePage(path, name, this, fileSystem);
    } else {
      FileSystemPage page = new FileSystemPage(name, this);
      if (autoCommit) page.commit(page.getData());
      return page;
    }
  }

  private boolean hasContentChild(File path) {
    for (String child : fileSystem.list(path)) {
      if (child.equals("content.txt")) return true;
    }
    return false;
  }

  private boolean hasHtmlChild(File path) {
    if (path.getName().endsWith(".html")) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(new File(path, child))) return true;
    }
    return false;
  }


  @Override
  public List<WikiPage> getNormalChildren() {
    final File thisDir = new File(getFileSystemPath());
    final List<WikiPage> children = new ArrayList<WikiPage>();
    if (fileSystem.exists(thisDir)) {
      final String[] subFiles = fileSystem.list(thisDir);
      for (final String subFile : subFiles) {
        if (fileIsValid(subFile, thisDir)) {
          children.add(getChildPage(subFile));
        }
      }
    }
    return children;
  }

  @Override
  protected WikiPage getNormalChildPage(String pageName) {
    final File file = new File(getFileSystemPath(), pageName);
    if (fileSystem.exists(file)) {
      return addChildPage(pageName);
    }
    return null;
  }

  @Override
  public PageData getData() {
    return getDataVersion(null);
  }

  @Override
  public ReadOnlyPageData readOnlyData() {
    return getData();
  }

  private boolean fileIsValid(final String filename, final File dir) {
    if (WikiWordPath.isWikiWord(filename)) {
      if (fileSystem.exists(new File(dir, filename))) {
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

  @Override
  public VersionInfo commit(final PageData data) {
    // Note: RecentChanges is not handled by the versionsController?
    try {
      return versionsController.makeVersion(new ContentFileVersion(data), new PropertiesFileVersion(data));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return (Collection<VersionInfo>) versionsController.history(contentFile(), propertiesFile());
  }

  @Override
  public PageData getDataVersion(final String versionName) {
    FileVersion[] versions = versionsController.getRevisionData(versionName, contentFile(), propertiesFile());
    PageData data = new PageData(this);
    try {
      for (FileVersion version : versions) {
        if (version == null) continue;
        if (contentFilename.equals(version.getFile().getName())) {
          data.setContent(loadContent(version));
        } else if (propertiesFilename.equals(version.getFile().getName())) {
          data.setProperties(loadAttributes(version));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new PageData(data, getVariableSource());
  }

  public void autoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  @Override
  public String toString() {
    try {
      return getClass().getName() + " at " + this.getFileSystemPath();
    } catch (final Exception e) {
      return super.toString();
    }
  }

  private File contentFile() {
    return new File(getFileSystemPath(), contentFilename);
  }

  private File propertiesFile() {
    return new File(getFileSystemPath(), propertiesFilename);
  }

  private String loadContent(final FileVersion fileVersion) throws IOException {
    InputStream content = fileVersion.getContent();
    try {
      return FileUtil.toString(content);
    } finally {
      content.close();
    }
  }

  private WikiPageProperties loadAttributes(final FileVersion fileVersion) throws IOException {
    final WikiPageProperties props = new WikiPageProperties();
    InputStream content = fileVersion.getContent();
    try {
      props.loadFromXmlStream(content);
    } finally {
      content.close();
    }
    props.setLastModificationTime(fileVersion.getLastModificationTime());
    return props;
  }

  class ContentFileVersion implements FileVersion {

    final PageData data;

    ContentFileVersion(PageData pageData) {
      this.data = new PageData(pageData);
    }

    @Override
    public File getFile() {
      return contentFile();
    }

    @Override
    public InputStream getContent() throws UnsupportedEncodingException {
      String content = data.getContent();

      if (content == null) {
        return new ByteArrayInputStream("".getBytes());
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

      return new ByteArrayInputStream(content.getBytes("UTF-8"));
    }

    @Override
    public String getAuthor() {
      return data.getAttribute(PageData.LAST_MODIFYING_USER);

    }

    @Override
    public Date getLastModificationTime() {
      return data.getProperties().getLastModificationTime();
    }

  }

  class PropertiesFileVersion implements FileVersion {

    final PageData data;

    PropertiesFileVersion(PageData pageData) {
      this.data = new PageData(pageData);
    }

    @Override
    public File getFile() {
      return propertiesFile();
    }

    @Override
    public InputStream getContent() throws IOException {
      WikiPageProperties propertiesToSave = new WikiPageProperties(data.getProperties());
      removeAlwaysChangingProperties(propertiesToSave);
      return new ByteArrayInputStream(propertiesToSave.toXml().getBytes("UTF-8"));
    }

    @Override
    public String getAuthor() {
      return data.getAttribute(PageData.LAST_MODIFYING_USER);
    }

    @Override
    public Date getLastModificationTime() {
      return data.getProperties().getLastModificationTime();
    }

    private void removeAlwaysChangingProperties(WikiPageProperties properties) {
      properties.remove(PageData.PropertyLAST_MODIFIED);
    }
  }
}
