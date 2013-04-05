// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import java.util.Collection;

import fitnesse.wiki.CachingPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.SymbolicPageFactory;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.WikiWordPath;

public class FileSystemPage extends CachingPage {
  private static final long serialVersionUID = 1L;

  // Only used for root page:
  private final String path;

  private final FileSystem fileSystem;
  private final VersionsController versionsController;

  public FileSystemPage(final String path, final String name, final FileSystem fileSystem, final VersionsController versionsController) {
    super(name, null, new SymbolicPageFactory());
    this.path = path;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  @Deprecated
  // TODO: not to be used in production code
  public FileSystemPage(final String path, final String name) {
    this(path, name, new DiskFileSystem(), new ZipFileVersionsController());
  }

  public FileSystemPage(final String name, final FileSystemPage parent) {
    super(name, parent, new SymbolicPageFactory());
    path = null;
    fileSystem = parent.fileSystem;
    versionsController = parent.versionsController;
  }

  @Override
  public void removeChildPage(final String name) {
    super.removeChildPage(name);
    WikiPage childPage = getChildPage(name);
    if (childPage instanceof FileSystemPage) {
      versionsController.delete((FileSystemPage) childPage);
    }
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
    String path = getFileSystemPath() + "/" + name;
    if (hasContentChild(path)) {
      return new FileSystemPage(name, this);
    } else if (hasHtmlChild(path)) {
      return new ExternalSuitePage(path, name, this, fileSystem);
    } else {
      return new FileSystemPage(name, this);
    }
  }

  private Boolean hasContentChild(String path) {
    for (String child : fileSystem.list(path)) {
      if (child.equals("content.txt")) return true;
    }
    return false;
  }

  private Boolean hasHtmlChild(String path) {
    if (path.endsWith(".html")) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(path + "/" + child)) return true;
    }
    return false;
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
