// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License versionName 1.0.
package fitnesse.wiki.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.util.Clock;
import util.FileUtil;

import static fitnesse.wiki.PageType.STATIC;

public class FileSystemPage extends BaseWikiPage {

  static final String contentFilename = "content.txt";
  static final String propertiesFilename = "properties.xml";

  private final File path;
  private final transient VersionsController versionsController;
  private final transient SubWikiPageFactory subWikiPageFactory;
  private final String versionName;
  private transient PageData pageData;

  public FileSystemPage(final File path, final String name,
                        final VersionsController versionsController, final SubWikiPageFactory subWikiPageFactory,
                        final VariableSource variableSource) {
    super(name, variableSource);
    this.path = path;
    this.versionsController = versionsController;
    this.subWikiPageFactory = subWikiPageFactory;
    this.versionName = null;
  }

  public FileSystemPage(final File path, final String name, final FileSystemPage parent) {
    this(path, name, parent, null, parent.versionsController, parent.subWikiPageFactory, parent.getVariableSource());
  }

  public FileSystemPage(final File path, final String name, final FileSystemPage parent, final VersionsController versionsController) {
    this(path, name, parent, null, versionsController, parent.subWikiPageFactory, parent.getVariableSource());
  }

  private FileSystemPage(FileSystemPage page, String versionName) {
    this(page.getFileSystemPath(), page.getName(), (FileSystemPage) (page.isRoot() ? null : page.getParent()), versionName,
            page.versionsController, page.subWikiPageFactory, page.getVariableSource());
  }

  private FileSystemPage(final File path, final String name, final FileSystemPage parent, final String versionName,
                         final VersionsController versionsController, final SubWikiPageFactory subWikiPageFactory,
                         final VariableSource variableSource) {
    super(name, parent, variableSource);
    this.path = path;
    this.versionsController = versionsController;
    this.subWikiPageFactory = subWikiPageFactory;
    this.versionName = versionName;
  }

  @Override
  public boolean hasChildPage(final String pageName) {
    return subWikiPageFactory.getChildPage(this, pageName) != null;
  }

  @Override
  public void removeChildPage(final String name) {
    final WikiPage childPage = getChildPage(name);
    if (childPage instanceof FileSystemPage) {
      versionsController.delete(new FileVersion() {
        @Override
        public File getFile() {
          return ((FileSystemPage) childPage).getFileSystemPath();
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
  public WikiPage addChildPage(String pageName) {
    WikiPage page = getChildPage(pageName);
    if (page == null) {
      page = new FileSystemPage(new File(getFileSystemPath(), pageName), pageName, this);
    }
    return page;
  }

  @Override
  public List<WikiPage> getChildren() {
    return subWikiPageFactory.getChildren(this);
  }

  @Override
  public WikiPage getChildPage(String childName) {
    return subWikiPageFactory.getChildPage(this, childName);
  }

  @Override
  public PageData getData() {
    if (pageData == null) {
      pageData = getDataVersion();
    }
    return new PageData(pageData);
  }

  public File getFileSystemPath() {
    return this.path;
  }

  @Override
  public VersionInfo commit(final PageData data) {
    // Note: RecentChanges is not handled by the versionsController?
    resetCache();
    try {
      return versionsController.makeVersion(new ContentFileVersion(data), new PropertiesFileVersion(data));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void resetCache() {
    super.resetCache();
    pageData = null;
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return (Collection<VersionInfo>) versionsController.history(contentFile(), propertiesFile());
  }

  private PageData getDataVersion() {
    FileVersion[] versions = versionsController.getRevisionData(versionName, contentFile(), propertiesFile());
    String content = "";
    WikiPageProperties properties = null;
    try {
      for (FileVersion version : versions) {
        if (version == null) continue;
        if (contentFilename.equals(version.getFile().getName())) {
          content = loadContent(version);
        } else if (propertiesFilename.equals(version.getFile().getName())) {
          properties = loadAttributes(version);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (properties == null) {
      properties = defaultPageProperties();
    }
    return new PageData(content, properties);
  }

  public WikiPageProperties defaultPageProperties() {
    WikiPageProperties properties = new WikiPageProperties();
    properties.set(PageData.PropertyEDIT);
    properties.set(PageData.PropertyPROPERTIES);
    properties.set(PageData.PropertyREFACTOR);
    properties.set(PageData.PropertyWHERE_USED);
    properties.set(PageData.PropertyRECENT_CHANGES);
    properties.set(PageData.PropertyFILES);
    properties.set(PageData.PropertyVERSIONS);
    properties.set(PageData.PropertySEARCH);
    properties.setLastModificationTime(Clock.currentDate());

    PageType pageType = PageType.getPageTypeForPageName(getName());

    if (STATIC.equals(pageType))
      return properties;

    properties.set(pageType.toString());
    return properties;
  }

  @Override
  public WikiPage getVersion(String versionName) {
    // Just assert the version is valid
    versionsController.getRevisionData(versionName, contentFile(), propertiesFile());
    return new FileSystemPage(this, versionName);
  }

  @Override
  public String toString() {
    try {
      return getClass().getName() + " at " + this.getFileSystemPath() + "#" + (versionName != null ? versionName : "latest");
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FileSystemPage)) return false;

    FileSystemPage that = (FileSystemPage) o;

    if (versionName != null ? !versionName.equals(that.versionName) : that.versionName != null) return false;
    return super.equals(that);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (versionName != null ? versionName.hashCode() : 0);
    return result;
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
