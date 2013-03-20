// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Collection;

import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileSystem;
import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.wikitext.parser.WikiWordPath;

public class FileSystemPage extends CachingPage {
  private static final long serialVersionUID = 1L;

  private final String path;
  private final WikiPageFactory wikiPageFactory;
  private final FileSystem fileSystem;
  private final VersionsController versionsController;

  public FileSystemPage(final String path, final String name,
                        final WikiPageFactory wikiPageFactory, final FileSystem fileSystem, final VersionsController versionsController) {
    super(name, null);
    this.path = path;
    this.wikiPageFactory = wikiPageFactory;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  @Deprecated
  // TODO: not to be used in production code
  public FileSystemPage(final String path, final String name) {
    this(path, name, new FileSystemPageFactory(), new DiskFileSystem(), new ZipFileVersionsController());
  }

  public FileSystemPage(final String name, final FileSystemPage parent) {
    super(name, parent);
    path = parent.getFileSystemPath();
    wikiPageFactory = parent.wikiPageFactory;
    fileSystem = parent.fileSystem;
    versionsController = parent.versionsController;
  }

  @Override
  public void removeChildPage(final String name) {
    super.removeChildPage(name);
    String pathToDelete = getFileSystemPath() + "/" + name;
    fileSystem.delete(pathToDelete);
  }

  @Override
  public boolean hasChildPage(final String pageName) {
    final String file = getFileSystemPath() + "/" + pageName;
    if (fileSystem.exists(file)) {
      addChildPage(pageName);
      return true;
    }
    return false;
  }

  @Override
  protected WikiPage createChildPage(final String name) {
    return wikiPageFactory.makeChildPage(name, this);
  }

  @Override
  protected void loadChildren() {
    final String thisDir = getFileSystemPath();
    if (fileSystem.exists(thisDir)) {
      final String[] subFiles = fileSystem.list(thisDir);
      for (final String subFile : subFiles) {
        if (fileIsValid(subFile, thisDir) && !this.children.containsKey(subFile)) {
          this.children.put(subFile, getChildPage(subFile));
        }
      }
    }
  }

  private boolean fileIsValid(final String filename, final String dir) {
    if (WikiWordPath.isWikiWord(filename)) {
      if (fileSystem.exists(dir + "/" + filename)) {
        return true;
      }
    }
    return false;
  }

  private String getParentFileSystemPath() {
    return this.parent != null ? ((FileSystemPage) this.parent).getFileSystemPath() : this.path;
  }

  public String getFileSystemPath() {
    return getParentFileSystemPath() + "/" + getName();
  }

  @Override
  public VersionInfo commit(final PageData data) {
    VersionInfo versionInfo = versionsController.makeVersion(this, data);
    super.commit(data);
    return versionInfo;
  }

  @Override
  protected PageData makePageData() {
    return versionsController.getRevisionData(this, null);
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return (Collection<VersionInfo>) versionsController.history(this);
  }

  @Override
  public PageData getDataVersion(final String versionName) {
    return versionsController.getRevisionData(this, versionName);
  }

  @Override
  public String toString() {
    try {
      return getClass().getName() + " at " + this.getFileSystemPath();
    } catch (final Exception e) {
      return super.toString();
    }
  }
}
