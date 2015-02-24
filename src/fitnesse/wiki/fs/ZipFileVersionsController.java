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

import static fitnesse.ConfigurationParameter.VERSIONS_CONTROLLER_DAYS;

public class ZipFileVersionsController implements VersionsController {
  private static final Logger LOG = Logger.getLogger(ZipFileVersionsController.class.getName());

  public static final Pattern ZIP_FILE_PATTERN = Pattern.compile("(\\S+)?\\d+(~\\d+)?\\.zip");

  private final int daysTillVersionsExpire;

  private VersionsController persistence;

  public ZipFileVersionsController(Properties properties) {
    this(getVersionDays(properties));
  }
  public ZipFileVersionsController() {
    this(14);
  }

  public ZipFileVersionsController(int versionDays) {
    this.daysTillVersionsExpire = versionDays;
    // Fix on Disk file system, since that's what ZipFileVersionsController can deal with.
    persistence = new SimpleFileVersionsController(new DiskFileSystem());
  }

  private static int getVersionDays(Properties properties) {
    String days = properties.getProperty(VERSIONS_CONTROLLER_DAYS.getKey());
    return days == null ? 14 : Integer.parseInt(days);
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
    } catch (Exception e) {
      throw new RuntimeException(e);
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
    } catch (Exception e) {
      throw new RuntimeException("Unable to make zip of current version", e);
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
    final FileInputStream is;
    try {
      is = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      LOG.warning("File " + file.getAbsolutePath() + " not found. It can not be saved in this version.");
      return;
    }
    try {
      final ZipEntry entry = new ZipEntry(file.getName());
      zos.putNextEntry(entry);
      final int size = (int) file.length();
      final byte[] bytes = new byte[size];
      is.read(bytes);
      zos.write(bytes, 0, size);
    } finally {
      is.close();
    }
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
      } catch (Exception e) {
        throw new RuntimeException("Unable to load content for file " + file + " from " + zipFile, e);
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
    if (!versions.isEmpty()) {
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
