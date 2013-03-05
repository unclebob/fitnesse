package fitnesse.wiki.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.VersionsController;
import fitnesse.wiki.WikiPageProperties;
import util.Clock;
import util.StreamReader;

public class ZipFileVersionsController implements VersionsController {
  public static SimpleDateFormat dateFormat() {
    return new SimpleDateFormat("yyyyMMddHHmmss");
  }

  private int daysTillVersionsExpire = 14;

  public ZipFileVersionsController() {
  }

  @Override
  public void setHistoryDepth(int historyDepth) {
    daysTillVersionsExpire = historyDepth;
  }

  @Override
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

  @Override
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

  @Override
  public VersionInfo makeVersion(final FileSystemPage page, final PageData data) {
    final String dirPath = getFileSystemPath(page);
    final Set<File> filesToZip = getFilesToZip(dirPath);

    final VersionInfo version = makeVersionInfo(data);

    if (filesToZip.size() == 0) {
      return new VersionInfo("first_commit", "", Clock.currentDate());
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
      pruneVersions(page, history(page));
    }
  }

  @Override
  public void prune(final FileSystemPage page) {
    try {
      pruneVersions(page, history(page));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
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
      final String user = data.getAttribute(PageData.LAST_MODIFYING_USER);
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


  public void pruneVersions(FileSystemPage page, Collection<VersionInfo> versions) {
    List<VersionInfo> versionsList = makeSortedVersionList(versions);
    if (versions.size() > 0) {
      VersionInfo lastVersion = versionsList.get(versionsList.size() - 1);
      GregorianCalendar expirationDate = makeVersionExpirationDate(lastVersion);
      for (Iterator<VersionInfo> iterator = versionsList.iterator(); iterator.hasNext();) {
        VersionInfo version = iterator.next();
        removeVersionIfExpired(page, version, expirationDate);
      }
    }
  }

  private List<VersionInfo> makeSortedVersionList(Collection<VersionInfo> versions) {
    List<VersionInfo> versionsList = new ArrayList<VersionInfo>(versions);
    Collections.sort(versionsList);
    return versionsList;
  }

  private GregorianCalendar makeVersionExpirationDate(VersionInfo lastVersion) {
    Date dateOfLastVersion = lastVersion.getCreationTime();
    GregorianCalendar expirationDate = new GregorianCalendar();
    expirationDate.setTime(dateOfLastVersion);
    expirationDate.add(Calendar.DAY_OF_MONTH, -(daysTillVersionsExpire));
    return expirationDate;
  }

  private void removeVersionIfExpired(FileSystemPage page, VersionInfo version, GregorianCalendar expirationDate) {
    Calendar thisDate = new GregorianCalendar();
    thisDate.setTime(version.getCreationTime());
    if (thisDate.before(expirationDate) || thisDate.equals(expirationDate))
      removeVersion(page, version.getName());
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public String getControllerName() {
    return "Zipped Version History";
  }
}
