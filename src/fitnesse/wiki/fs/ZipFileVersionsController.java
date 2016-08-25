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
import java.util.zip.*;

import static fitnesse.ConfigurationParameter.VERSIONS_CONTROLLER_DAYS;
import static java.lang.String.format;

public class ZipFileVersionsController implements VersionsController {
  private static final Logger LOG = Logger.getLogger(ZipFileVersionsController.class.getName());

  private static final Pattern ZIP_FILE_PATTERN = Pattern.compile("(\\S+)?\\d+(~\\d+)?\\.zip");
  public static final String ZIP_EXTENSION = ".zip";

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
  public FileVersion[] getRevisionData(final String label, final File... files) throws IOException {
    if (label == null) {
      return persistence.getRevisionData(null, files);
    }
    final File file = new File(files[0].getParentFile(), label + ZIP_EXTENSION);
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
    } finally {
        FileUtil.close(zipFile);
    }
  }

  @Override
  public Collection<VersionInfo> history(final File... pageFiles) {
    // Let's assume for a second all files live in the same folder
    File dir = commonBaseDir(pageFiles);
    final File[] files = dir.listFiles();
    final Set<VersionInfo> versions = new HashSet<>();
    if (files != null) {
      for (final File file : files) {
        if (isVersionFile(file) && isZipForFiles(file, pageFiles)) {
          versions.add(ZipFileVersionInfo.makeVersionInfo(file));
        }
      }
    }
    return versions;
  }

  // What about a page with just a content.txt file and no properties.xml?
  // Is okay to have one of the files present!
  private boolean isZipForFiles(File zipFile, File... containedFiles) {
    List<String> zipFileNames = getFileNamesInZipFile(zipFile);
    for (File f : containedFiles) {
      if (zipFileNames.contains(f.getName())) {
        return true;
      }
    }
    return false;
  }

  private List<String> getFileNamesInZipFile(File zipFile) {
    List<String> names = new ArrayList<>();
    try (final ZipInputStream zos = new ZipInputStream(new FileInputStream(zipFile))) {
      for (ZipEntry entry = zos.getNextEntry(); entry != null; entry = zos.getNextEntry()) {
        names.add(entry.getName());
      }
    } catch (IOException e) {
      LOG.log(Level.WARNING, format("Could not read zip file %s", zipFile), e);
    }
    return names;
  }

  @Override
  public VersionInfo makeVersion(final FileVersion... fileVersions) throws IOException {
    File commonBaseDir = commonBaseDir(fileVersions);
    String versionName = makeVersionName(commonBaseDir, fileVersions[0]);
    final File zipFile = new File(commonBaseDir, versionName + ZIP_EXTENSION);

    makeZipVersion(zipFile, fileVersions);
    pruneVersions(history(toFiles(fileVersions)));
    persistence.makeVersion(fileVersions);
    return new VersionInfo(versionName, fileVersions[0].getAuthor(), fileVersions[0].getLastModificationTime());
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
  public void delete(File... files) throws IOException {
    persistence.delete(files);
  }

  protected void makeZipVersion(File zipFile, FileVersion... fileVersions) throws IOException {
    if (!exists(fileVersions)) {
      return;
    }

    ZipOutputStream zos = null;
    try {
      zos = new ZipOutputStream(new FileOutputStream(zipFile));
      for (FileVersion fileVersion : fileVersions) {
        addToZip(fileVersion.getFile(), zos);
      }
    } finally {
      try {
        if (zos != null) {
          zos.finish();
          FileUtil.close(zos);
        }
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Unable to create zip file", e);
      }
    }
  }

  private boolean exists(FileVersion[] fileVersions) {
    for (FileVersion fileVersion : fileVersions) {
      if (fileVersion.getFile().exists())
        return true;
    }
    return false;
  }

  private File[] toFiles(FileVersion[] fileVersions) {
    File[] files = new File[fileVersions.length];
    for (int i = 0; i < fileVersions.length; i++) {
      files[i] = fileVersions[i].getFile();
    }
    return files;
  }

  private File commonBaseDir(FileVersion[] fileVersions) {
    return commonBaseDir(fileVersions[0].getFile());
  }

  private File commonBaseDir(File... files) {
    return files[0].getParentFile();
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
      FileUtil.close(is);
    }
  }

  private boolean isVersionFile(final File file) {
    return ZIP_FILE_PATTERN.matcher(file.getName()).matches();
  }

  private ZipFileVersion loadZipEntry(final ZipFile zipFile, final File file) throws IOException {
    final ZipEntry contentEntry = zipFile.getEntry(file.getName());
    if (contentEntry != null) {
      InputStream contentIS = null;
      try {
        contentIS = zipFile.getInputStream(contentEntry);
        return new ZipFileVersion(file, FileUtil.toString(contentIS), new Date(contentEntry.getTime()));
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

  private String makeVersionName(final File baseDir, final FileVersion fileVersion) {
    Date time = fileVersion.getLastModificationTime();
    String versionName = WikiImportProperty.getTimeFormat().format(time);
    final String user = fileVersion.getAuthor();
    if (user != null && !"".equals(user)) {
      versionName = user + "-" + versionName;
    }
    String name = versionName;
    int counter = 1;
    while (new File(baseDir, name + ZIP_EXTENSION).exists()) {
      name = versionName + "~" + (counter++);
    }
    return name;
  }

  private void pruneVersions(Collection<VersionInfo> versions) {
    List<VersionInfo> versionsList = makeSortedVersionList(versions);
    if (!versions.isEmpty()) {
      VersionInfo lastVersion = versionsList.get(versionsList.size() - 1);
      Date expirationDate = makeVersionExpirationDate(lastVersion);
      for (VersionInfo version : versionsList) {
        Date thisDate = version.getCreationTime();
        if (thisDate.before(expirationDate) || thisDate.equals(expirationDate))
          ((ZipFileVersionInfo) version).getFile().delete();
      }
    }
  }

  private List<VersionInfo> makeSortedVersionList(Collection<VersionInfo> versions) {
    List<VersionInfo> versionsList = new ArrayList<>(versions);
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
      return new ByteArrayInputStream(content.getBytes(FileUtil.CHARENCODING));
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
