package fitnesse.wiki;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import fitnesse.wiki.storage.FileSystem;

public class SimpleFileVersionsController implements VersionsController {

  public static final String contentFilename = "/content.txt";
  public static final String propertiesFilename = "/properties.xml";

  private static VersionInfo versionInfo = new VersionInfo("current", "author", new Date());

  private final FileSystem fileSystem;

  public SimpleFileVersionsController(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
    // Just one file, no history
  }

  @Override
  public PageData getRevisionData(FileSystemPage page, String label) {
    final PageData pagedata = new PageData(page);
    loadContent(page, pagedata);
    loadAttributes(page, pagedata);
    return pagedata;
  }

  @Override
  public Collection<VersionInfo> history(FileSystemPage page) {
    return Arrays.asList(versionInfo);
  }

  @Override
  public VersionInfo makeVersion(FileSystemPage page, PageData data) {
    createDirectoryIfNewPage(page);
    saveContent(page, data.getContent());
    saveAttributes(page, data.getProperties());
    return null;
  }

  private void createDirectoryIfNewPage(final FileSystemPage page) {
    String pagePath = getFileSystemPath(page);
    if (!fileSystem.exists(pagePath)) {
      try {
        fileSystem.makeDirectory(pagePath);
      } catch (IOException e) {
        throw new RuntimeException("Unable to create directory for new page", e);
      }
    }
  }

  protected synchronized void saveContent(final FileSystemPage page, String content) {
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

    String contentPath = getFileSystemPath(page) + contentFilename;
    try {
      fileSystem.makeFile(contentPath, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected synchronized void saveAttributes(final FileSystemPage page, final WikiPageProperties attributes)
  {
    String propertiesFilePath = "<unknown>";
    try {
      propertiesFilePath = getFileSystemPath(page) + propertiesFilename;
      WikiPageProperties propertiesToSave = new WikiPageProperties(attributes);
      removeAlwaysChangingProperties(propertiesToSave);
      fileSystem.makeFile(propertiesFilePath, propertiesToSave.toXml());
    } catch (final Exception e) {
      throw new RuntimeException("Failed to save properties file: \""
              + propertiesFilePath + "\" (exception: " + e + ").", e);
    }
  }

  private void removeAlwaysChangingProperties(WikiPageProperties properties) {
    properties.remove(PageData.PropertyLAST_MODIFIED);
  }

  private void loadContent(final FileSystemPage page, final PageData data) {
    String content = "";
    final String name = getFileSystemPath(page) + contentFilename;
    try {
      if (fileSystem.exists(name)) {
        content = fileSystem.getContent(name);
      }
      data.setContent(content);
    } catch (IOException e) {
      throw new RuntimeException("Error while loading content", e);
    }
  }

  private void loadAttributes(final FileSystemPage page, final PageData data) {
    final String path = getFileSystemPath(page) + propertiesFilename;
    if (fileSystem.exists(path)) {
      try {
        long lastModifiedTime = getLastModifiedTime(page);
        attemptToReadPropertiesFile(path, data, lastModifiedTime);
      } catch (final Exception e) {
        System.err.println("Could not read properties file:" + path);
        e.printStackTrace();
      }
    }
  }

  private void attemptToReadPropertiesFile(String file, PageData data,
                                           long lastModifiedTime) throws IOException {
    String propertiesXml = fileSystem.getContent(file);
    final WikiPageProperties props = new WikiPageProperties();
    props.loadFromXml(propertiesXml);
    props.setLastModificationTime(new Date(lastModifiedTime));
    data.setProperties(props);
  }

  private long getLastModifiedTime(final FileSystemPage page) {
    final String path = getFileSystemPath(page) + contentFilename;
    return fileSystem.lastModified(path);
  }

  private VersionInfo makeVersionInfo(final PageData data) {
    try {
      Date time = new Date();
      String versionName = VersionInfo.nextId() + "-" + dateFormat().format(time);
      final String user = data.getAttribute(PageData.LAST_MODIFYING_USER);
      if (user != null && !"".equals(user)) {
        versionName = user + "-" + versionName;
      }

      return new VersionInfo(versionName, user, time);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileSystemPath(final FileSystemPage page) {
    try {
      return page.getFileSystemPath();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static SimpleDateFormat dateFormat() {
    return WikiPageProperty.getTimeFormat();
  }

}
