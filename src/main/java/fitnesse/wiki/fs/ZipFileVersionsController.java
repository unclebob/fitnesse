package fitnesse.wiki.fs;

import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPageProperties;
import util.StreamReader;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static fitnesse.wiki.fs.SimpleFileVersionsController.contentFilename;
import static fitnesse.wiki.fs.SimpleFileVersionsController.propertiesFilename;

public class ZipFileVersionsController implements VersionsController {

  public static final Pattern ZIP_FILE_PATTERN = Pattern.compile("(\\S+)?\\d+(~\\d+)?\\.zip");

  private int daysTillVersionsExpire = 14;

  private VersionsController persistence;

  public ZipFileVersionsController() {
    // Fix on Disk file system, since that's what ZipFileVersionsController can deal with.
    persistence = new SimpleFileVersionsController(new DiskFileSystem());
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
    daysTillVersionsExpire = historyDepth;
  }

  @Override
  public PageData getRevisionData(final FileSystemPage page, final String label) {
    if (label == null) {
      return persistence.getRevisionData(page, null);
    }
    final String filename = page.getFileSystemPath() + "/" + label + ".zip";
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

  @Override
  public Collection<ZipFileVersionInfo> history(final FileSystemPage page) {
    final File dir = new File(page.getFileSystemPath());
    final File[] files = dir.listFiles();
    final Set<ZipFileVersionInfo> versions = new HashSet<ZipFileVersionInfo>();
    if (files != null) {
      for (final File file : files) {
        if (isVersionFile(file)) {
          versions.add(ZipFileVersionInfo.makeVersionInfo(file));
        }
      }
    }
    return versions;
  }

  @Override
  public VersionInfo makeVersion(final FileSystemPage page, final PageData data) {
    makeZipVersion(page, page.getData());
    return persistence.makeVersion(page, data);
  }

  @Override
  public VersionInfo getCurrentVersion(FileSystemPage page) {
    return persistence.getCurrentVersion(page);
  }

  @Override
  public void delete(FileSystemPage page) {
    persistence.delete(page);
  }

  protected VersionInfo makeZipVersion(FileSystemPage page, PageData data) {
    final String dirPath = page.getFileSystemPath();
    final File contentFile = new File(dirPath, contentFilename);
    final File propertiesFile = new File(dirPath, propertiesFilename);
    final VersionInfo version = VersionInfo.makeVersionInfo(data);

    if (!contentFile.exists() || !propertiesFile.exists()) {
      return version;
    }

    ZipOutputStream zos = null;
    try {
      final String filename = makeVersionFileName(page, version.getName());
      zos = new ZipOutputStream(new FileOutputStream(filename));
      addToZip(contentFile, zos);
      addToZip(propertiesFile, zos);
      return version;
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
      pruneVersions(page, history(page));
    }
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

  private boolean isVersionFile(final File file) {
    return ZIP_FILE_PATTERN.matcher(file.getName()).matches();
  }

  private void loadVersionAttributes(final ZipFile zipFile, final PageData data) {
    final ZipEntry attributes = zipFile.getEntry(propertiesFilename);
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
    final ZipEntry contentEntry = zipFile.getEntry(contentFilename);
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
    final File dir = new File(page.getFileSystemPath());
    final File[] files = dir.listFiles();
    final Set<VersionInfo> versions = new HashSet<VersionInfo>();
    if (files != null) {
      for (final File file : files) {
        if (isVersionFile(file)) {
          versions.add(ZipFileVersionInfo.makeVersionInfo(file));
        }
      }
    }
    return versions;
  }

  private String makeVersionFileName(final FileSystemPage page, final String name) {
    String filename = page.getFileSystemPath() + "/" + name + ".zip";
    int counter = 1;
    while (new File(filename).exists()) {
      filename = page.getFileSystemPath() + "/" + name + "~" + (counter++) + ".zip";
    }
    return filename;
  }

  public void pruneVersions(FileSystemPage page, Collection<ZipFileVersionInfo> versions) {
    List<ZipFileVersionInfo> versionsList = makeSortedVersionList(versions);
    if (versions.size() > 0) {
      VersionInfo lastVersion = versionsList.get(versionsList.size() - 1);
      Date expirationDate = makeVersionExpirationDate(lastVersion);
      for (ZipFileVersionInfo version : versionsList) {
        Date thisDate = version.getCreationTime();
        if (thisDate.before(expirationDate) || thisDate.equals(expirationDate))
          version.getFile().delete();
      }
    }
  }

  private List<ZipFileVersionInfo> makeSortedVersionList(Collection<ZipFileVersionInfo> versions) {
    List<ZipFileVersionInfo> versionsList = new ArrayList<ZipFileVersionInfo>(versions);
    Collections.sort(versionsList);
    return versionsList;
  }

  private Date makeVersionExpirationDate(VersionInfo lastVersion) {
    Date dateOfLastVersion = lastVersion.getCreationTime();
    GregorianCalendar expirationDate = new GregorianCalendar();
    expirationDate.setTime(dateOfLastVersion);
    expirationDate.add(Calendar.DAY_OF_MONTH, -(daysTillVersionsExpire));
    return expirationDate.getTime();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

}
