package fitnesse.wiki.zip;

import fitnesse.wiki.NullVersionsController;
import util.StreamReader;
import fitnesse.wiki.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipFileVersionsController extends NullVersionsController {
  public static SimpleDateFormat dateFormat() {
    return new SimpleDateFormat("yyyyMMddHHmmss");
  }

  public ZipFileVersionsController() {
    this(new Properties());
  }

  public ZipFileVersionsController(final Properties properties) {
  }

  public PageData getRevisionData(final FileSystemPage page, final String label) {
    final String filename = getFileSystemPath(page) + "/" + label + ".zip";
    final File file = new File(filename);
    if (!file.exists()) {
      throw new NoSuchVersionException("There is no version '" + label + "'");
    }

    ZipFile zipFile = null;
    try {
      final PageData data = new PageData(page);
      zipFile = new ZipFile(file);
      loadVersionContent(zipFile, data);
      loadVersionAttributes(zipFile, data);
      data.addVersions(loadVersions(page));
      return data;
    } catch (Throwable th) {
      throw new RuntimeException(th);
    } finally {
      try {
        if (zipFile != null) {
          zipFile.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private String getFileSystemPath(final FileSystemPage page) {
    try {
      return page.getFileSystemPath();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<VersionInfo> history(final FileSystemPage page) {
    final File dir = new File(getFileSystemPath(page));
    final File[] files = dir.listFiles();
    final Set<VersionInfo> versions = new HashSet<VersionInfo>();
    if (files != null) {
      for (final File file : files) {
        if (isVersionFile(file)) {
          versions.add(new VersionInfo(makeVersionName(file)));
        }
      }
    }
    return versions;
  }

  public boolean isRevisionControlEnabled() {
    return true;
  }

  public boolean isExternalRevisionControlEnabled() {
    return false;
  }

  public VersionInfo makeVersion(final FileSystemPage page, final PageData data) {
    final String dirPath = getFileSystemPath(page);
    final Set<File> filesToZip = getFilesToZip(dirPath);

    final VersionInfo version = makeVersionInfo(data);

    if (filesToZip.size() == 0) {
      return new VersionInfo("first_commit", "", new Date());
    }
    ZipOutputStream zos = null;
    try {
      final String filename = makeVersionFileName(page, version.getName());
      zos = new ZipOutputStream(new FileOutputStream(filename));
      for (File aFilesToZip : filesToZip) {
        addToZip(aFilesToZip, zos);
      }
      return new VersionInfo(version.getName());
    } catch (Throwable th) {
      throw new RuntimeException(th);
    } finally {
      try {
        if (zos != null) {
          zos.finish();
          zos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void prune(final FileSystemPage page) {
    try {
      PageVersionPruner.pruneVersions(page, history(page));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void removeVersion(final FileSystemPage page, final String versionName) {
    final String versionFileName = makeVersionFileName(page, versionName);
    final File versionFile = new File(versionFileName);
    versionFile.delete();
  }

  private void addToZip(final File file, final ZipOutputStream zos) throws IOException {
    final ZipEntry entry = new ZipEntry(file.getName());
    zos.putNextEntry(entry);
    final FileInputStream is = new FileInputStream(file);
    final int size = (int) file.length();
    final byte[] bytes = new byte[size];
    is.read(bytes);
    is.close();
    zos.write(bytes, 0, size);
  }

  private Set<File> getFilesToZip(final String dirPath) {
    final Set<File> filesToZip = new HashSet<File>();
    final File dir = new File(dirPath);
    final File[] files = dir.listFiles();
    if (files == null) {
      return filesToZip;
    }
    for (final File file : files) {
      if (!(isVersionFile(file) || file.isDirectory())) {
        filesToZip.add(file);
      }
    }
    return filesToZip;
  }

  private boolean isVersionFile(final File file) {
    return Pattern.matches("(\\S+)?\\d+\\.zip", file.getName());
  }

  private void loadVersionAttributes(final ZipFile zipFile, final PageData data) {
    final ZipEntry attributes = zipFile.getEntry("properties.xml");
    if (attributes != null) {
      InputStream attributeIS = null;
      try {
        attributeIS = zipFile.getInputStream(attributes);
        final WikiPageProperties props = new WikiPageProperties(attributeIS);
        data.setProperties(props);
      } catch (Throwable th) {
        throw new RuntimeException(th);
      } finally {
        try {
          if (attributeIS != null) {
            attributeIS.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void loadVersionContent(final ZipFile zipFile, final PageData data) {
    String content = "";
    final ZipEntry contentEntry = zipFile.getEntry("content.txt");
    if (contentEntry != null) {
      StreamReader reader = null;
      try {
        final InputStream contentIS = zipFile.getInputStream(contentEntry);
        reader = new StreamReader(contentIS);
        content = reader.read((int) contentEntry.getSize());
      } catch (Throwable th) {
        throw new RuntimeException(th);
      } finally {
        try {
          if (reader != null) {
            reader.close();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    data.setContent(content);
  }

  private Collection<VersionInfo> loadVersions(final FileSystemPage page) {
    final File dir = new File(getFileSystemPath(page));
    final File[] files = dir.listFiles();
    final Set<VersionInfo> versions = new HashSet<VersionInfo>();
    if (files != null) {
      for (final File file : files) {
        if (isVersionFile(file)) {
          versions.add(new VersionInfo(makeVersionName(file)));
        }
      }
    }
    return versions;
  }

  private String makeVersionFileName(final FileSystemPage page, final String name) {
    return getFileSystemPath(page) + "/" + name + ".zip";
  }

  private VersionInfo makeVersionInfo(final PageData data) {
    try {
      Date time;
      time = data.getProperties().getLastModificationTime();
      String versionName = VersionInfo.nextId() + "-" + dateFormat().format(time);
      final String user = data.getAttribute(WikiPage.LAST_MODIFYING_USER);
      if (user != null && !"".equals(user)) {
        versionName = user + "-" + versionName;
      }

      return new VersionInfo(versionName, user, time);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String makeVersionName(final File file) {
    final String name = file.getName();
    return name.substring(0, name.length() - 4);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public String getControllerName() {
    return "Zipped Version History";
  }
}
