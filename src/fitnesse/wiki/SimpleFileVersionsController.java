package fitnesse.wiki;

import static fitnesse.wiki.VersionInfo.makeVersionInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import fitnesse.wiki.fs.FileSystem;

public class SimpleFileVersionsController implements VersionsController {

  public static final String contentFilename = "content.txt";
  public static final String propertiesFilename = "properties.xml";

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
    return Arrays.asList(makeVersion(page, getRevisionData(page, "")));
  }

  @Override
  public VersionInfo makeVersion(FileSystemPage page, PageData data) {
    createDirectoryIfNewPage(page);
    saveContent(page, data.getContent());
    saveAttributes(page, data.getProperties());
    return makeVersionInfo(data);
  }

  @Override
  public VersionInfo getCurrentVersion(FileSystemPage page) {
    return makeVersionInfo(getRevisionData(page, null));
  }

  private void createDirectoryIfNewPage(final FileSystemPage page) {
    String pagePath = page.getFileSystemPath();
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

    String contentPath = page.getFileSystemPath() + "/" + contentFilename;
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
      propertiesFilePath = page.getFileSystemPath() + "/" + propertiesFilename;
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
    final String name = page.getFileSystemPath() + "/" + contentFilename;
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
    final String path = page.getFileSystemPath() + "/" + propertiesFilename;
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
    final String path = page.getFileSystemPath() + "/" + contentFilename;
    return fileSystem.lastModified(path);
  }

}
