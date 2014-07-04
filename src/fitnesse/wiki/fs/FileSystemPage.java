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
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.ParsedPage;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.WikiSourcePage;
import util.FileUtil;

public class FileSystemPage extends BaseWikiPage {
  private static final long serialVersionUID = 1L;

  static final String contentFilename = "content.txt";
  static final String propertiesFilename = "properties.xml";

  // Only used for root page:
  private final File path;

  private final transient VersionsController versionsController;
  private final transient SubWikiPageFactory subWikiPageFactory;
  private final String versionName;
  private transient PageData pageData;
  private transient ParsedPage parsedPage;

  public FileSystemPage(final File path, final String name,
                        final VersionsController versionsController, final SubWikiPageFactory subWikiPageFactory,
                        final VariableSource variableSource) {
    super(name, variableSource);
    this.path = path;
    this.versionsController = versionsController;
    this.subWikiPageFactory = subWikiPageFactory;
    this.versionName = null;
  }

  public FileSystemPage(final String name, final FileSystemPage parent) {
    this(name, parent, parent.versionsController);
  }

  public FileSystemPage(final String name, final FileSystemPage parent, final VersionsController versionsController) {
    this(name, parent, null, versionsController);
  }

  private FileSystemPage(FileSystemPage page, String versionName) {
    this(page.getName(), (FileSystemPage) page.getParent(), versionName, page.versionsController);
  }

  private FileSystemPage(final String name, final FileSystemPage parent, final String versionName, final VersionsController versionsController) {
    super(name, parent);
    path = null;
    this.versionsController = versionsController;
    this.subWikiPageFactory = parent.subWikiPageFactory;
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
      page = new FileSystemPage(pageName, this);
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
      pageData = getDataVersion(versionName);
    }
    return new PageData(pageData, getVariableSource());
  }

  @Override
  public ReadOnlyPageData readOnlyData() {
    return getData();
  }

  private File getParentFileSystemPath() {
    return isRoot() ?  this.path : ((FileSystemPage) this.getParent()).getFileSystemPath();
  }

  public File getFileSystemPath() {
    return new File(getParentFileSystemPath(), getName());
  }

  @Override
  public VersionInfo commit(final PageData data) {
    // Note: RecentChanges is not handled by the versionsController?
    pageData = null;
    parsedPage = null;
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

    // Set data here, so we can render older versions of the data
//    pageData = data;
    return new PageData(data, getVariableSource());
  }

  @Override
  public WikiPage getVersion(String versionName) {
    return new FileSystemPage(this, versionName);
  }

  @Override
  public String getHtml() {
    return getParsedPage().toHtml();
  }

  public ParsedPage getParsedPage() {
    if (parsedPage == null) {
      parsedPage = new ParsedPage(new ParsingPage(new WikiSourcePage(this), getVariableSource()), getData().getContent());
    }
    return parsedPage;
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
