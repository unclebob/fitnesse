package fitnesse.wiki.fs;

import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPageProperties;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.util.Cache;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPageProperties;
import util.Clock;
import static fitnesse.wiki.VersionInfo.makeVersionInfo;

public class SimpleFileVersionsController implements VersionsController {
  private static final Logger LOG = Logger.getLogger("Wiki");

  public static final int CACHE_TIMEOUT = 300000; // ms

  public static final String contentFilename = "content.txt";
  public static final String propertiesFilename = "properties.xml";

  private final FileSystem fileSystem;

  private final Cache<String, String> fileCache = new Cache.Builder<String, String>()
          .withLoader(new Cache.Loader<String, String>() {
            @Override
            public String fetch(String fileName) throws Exception {
              return fileSystem.exists(fileName) ? fileSystem.getContent(fileName) : null;
            }
          })
          .withExpirationPolicy(new Cache.ExpirationPolicy<String, String>() {
            @Override
            public boolean isExpired(String key, String value, long lastModified) {
              return !fileSystem.exists(key)
                      || (lastModified - Clock.currentTimeInMillis() > CACHE_TIMEOUT);
            }
          })
          .build();

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

  @Override
  public void delete(FileSystemPage page) {
    fileSystem.delete(page.getFileSystemPath());
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
    fileCache.evict(contentPath);
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
      fileCache.evict(propertiesFilePath);
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
    final String name = page.getFileSystemPath() + "/" + contentFilename;
    try {
      String content = fileCache.get(name);
      data.setContent(content != null ? content : "");
    } catch (Exception e) {
      throw new RuntimeException("Error while loading content", e);
    }
  }

  private void loadAttributes(final FileSystemPage page, final PageData data) {
    final String path = page.getFileSystemPath() + "/" + propertiesFilename;
    try {
      String propertiesXml = fileCache.get(path);
      if (propertiesXml != null) {
        long lastModifiedTime = getLastModifiedTime(page);
        final WikiPageProperties props = parsePropertiesXml(propertiesXml, lastModifiedTime);
        data.setProperties(props);
      }
    } catch (final Exception e) {
      LOG.log(Level.WARNING, "Could not read properties file: " + path, e);
    }
  }

  private long getLastModifiedTime(final FileSystemPage page) {
    final String path = page.getFileSystemPath() + "/" + contentFilename;
    return fileSystem.lastModified(path);
  }

  public static WikiPageProperties parsePropertiesXml(String propertiesXml, long lastModifiedTime) {
    final WikiPageProperties props = new WikiPageProperties();
    props.loadFromXml(propertiesXml);
    props.setLastModificationTime(new Date(lastModifiedTime));
    return props;
  }

}
