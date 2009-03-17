// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol.zip;

import static fitnesse.revisioncontrol.NullState.VERSIONED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import util.StreamReader;
import fitnesse.revisioncontrol.RevisionControlException;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.revisioncontrol.State;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageVersionPruner;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class ZipFileRevisionController implements RevisionController {
  public static SimpleDateFormat dateFormat() {
    return new SimpleDateFormat("yyyyMMddHHmmss");
  }

  public ZipFileRevisionController() {
    this(new Properties());
  }

  public ZipFileRevisionController(final Properties properties) {
  }

  public void add(final String... filePaths) throws RevisionControlException {
  }

  public void checkin(final String... filePaths) throws RevisionControlException {
  }

  public void checkout(final String... filePaths) throws RevisionControlException {
  }

  public State checkState(final String... filePaths) throws RevisionControlException {
    return VERSIONED;
  }

  public void delete(final String... filePaths) throws RevisionControlException {
  }

  public void move(final File src, final File dest) throws RevisionControlException {
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
        zipFile.close();
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

  public boolean isReversionControlEnabled() {
    return true;
  }

  public boolean isExternalReversionControlEnabled() {
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
      for (final Iterator<File> iterator = filesToZip.iterator(); iterator.hasNext();) {
        addToZip(iterator.next(), zos);
      }
      return new VersionInfo(version.getName());
    } catch (Throwable th) {
      throw new RuntimeException(th);
    } finally {
      try {
        zos.finish();
        zos.close();
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

  public void revert(final String... filePaths) throws RevisionControlException {
  }

  public void update(final String... filePaths) throws RevisionControlException {
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
          attributeIS.close();
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
          reader.close();
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
