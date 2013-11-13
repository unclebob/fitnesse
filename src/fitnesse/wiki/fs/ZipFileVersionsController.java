package fitnesse.wiki.fs;

import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiImportProperty;
import util.FileUtil;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipFileVersionsController implements VersionsController {
  private static final Logger LOG = Logger.getLogger(ZipFileVersionsController.class.getName());

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
  public FileVersion[] getRevisionData(final String label, final File... files) {
    if (label == null) {
      return persistence.getRevisionData(null, files);
    }
    final File file = new File(files[0].getParentFile(), label + ".zip");
    if (!file.exists()) {
      throw new NoSuchVersionException("There is no version '" + label + "'");
    }

    ZipFile zipFile = null;
    FileVersion[] versions = new FileVersion[files.length];
    int counter = 0;
    try {
      zipFile = new ZipFile(file);
      for (File f : files) {
        ZipFileVersion version = loadZipEntry(zipFile, f);
        if (version != null)
          versions[counter++] = version;
      }
      return versions;
    } catch (Throwable th) {
      throw new RuntimeException(th);
    } finally {
      try {
        if (zipFile != null) {
          zipFile.close();
        }
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Unable to read zip file contents", e);
      }
    }
  }

  @Override
  public Collection<ZipFileVersionInfo> history(final File... files) {
    return history(files[0].getParentFile());
  }

  public Collection<ZipFileVersionInfo> history(final File dir) {
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
  public VersionInfo makeVersion(final FileVersion... fileVersions) throws IOException {
    makeZipVersion(fileVersions);
    return persistence.makeVersion(fileVersions);
  }

  @Override
  public VersionInfo addDirectory(FileVersion filePath) throws IOException {
    return persistence.addDirectory(filePath);
  }

  @Override
  public void rename(FileVersion fileVersion, File originalFile) throws IOException {
    persistence.rename(fileVersion, originalFile);
  }

  @Override
  public void delete(FileVersion... files) {
    persistence.delete(files);
  }

  protected void makeZipVersion(FileVersion... fileVersions) {
    if (!exists(fileVersions)) {
      return;
    }

    // if (isFileInFilesSection()) return version;
    ZipOutputStream zos = null;
    File commonBaseDir = commonBaseDir(fileVersions);
    try {
      final File zipFile = makeVersionFileName(commonBaseDir, makeVersionName(fileVersions[0]));
      zos = new ZipOutputStream(new FileOutputStream(zipFile));
      for (FileVersion fileVersion : fileVersions) {
        addToZip(fileVersion.getFile(), zos);
      }
    } catch (Throwable th) {
      throw new RuntimeException(th);
    } finally {
      try {
        if (zos != null) {
          zos.finish();
          zos.close();
        }
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Unable to create zip file", e);
      }
      pruneVersions(history(commonBaseDir));
    }
  }

  private String makeVersionName(FileVersion fileVersion) {
    Date time = fileVersion.getLastModificationTime();
    String versionName = WikiImportProperty.getTimeFormat().format(time);
    final String user = fileVersion.getAuthor();
    if (user != null && !"".equals(user)) {
      versionName = user + "-" + versionName;
    }
    return versionName;
  }

  private boolean exists(FileVersion[] fileVersions) {
    for (FileVersion fileVersion : fileVersions) {
      if (fileVersion.getFile().exists())
        return true;
    }
    return false;
  }

  private File commonBaseDir(FileVersion[] fileVersions) {
    return fileVersions[0].getFile().getParentFile();
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

  private ZipFileVersion loadZipEntry(final ZipFile zipFile, final File file) {
    final ZipEntry contentEntry = zipFile.getEntry(file.getName());
    if (contentEntry != null) {
      InputStream contentIS = null;
      try {
        contentIS = zipFile.getInputStream(contentEntry);
        return new ZipFileVersion(file, FileUtil.toString(contentIS), new Date(contentEntry.getTime()));
      } catch (Throwable th) {
        throw new RuntimeException(th);
      } finally {
        try {
          if (contentIS != null) {
            contentIS.close();
          }
        } catch (Exception e) {
          LOG.log(Level.WARNING, "Unable to read zip file contents", e);
        }
      }
    }
    return null;
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

  private File makeVersionFileName(final File file, final String name) {
    File zipFile = new File(file, name + ".zip");
    int counter = 1;
    while (zipFile.exists()) {
      zipFile = new File(file, name + "~" + (counter++) + ".zip");
    }
    return zipFile;
  }

  private void pruneVersions(Collection<ZipFileVersionInfo> versions) {
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

  private static class ZipFileVersion implements FileVersion {
    private final File file;
    private final String content;
    private final Date lastModified;

    public ZipFileVersion(File file, String content, Date lastModified) {
      this.file = file;
      this.content = content;
      this.lastModified = lastModified;
    }

    @Override
    public File getFile() {
      return file;
    }

    @Override
    public InputStream getContent() throws IOException {
      return new ByteArrayInputStream(content.getBytes("UTF-8"));
    }

    @Override
    public String getAuthor() {
      return null;
    }

    @Override
    public Date getLastModificationTime() {
      return lastModified;
    }
  }
}
