// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.WikiWordPath;

import java.util.Collection;

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

  public FileSystemPage(final String name, final FileSystemPage parent, final FileSystem fileSystem, final VersionsController versionsController) {
    super(name, parent, new SymbolicPageFactory());
    path = null;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  public FileSystemPage(final String name, final FileSystemPage parent) {
    this(name, parent, parent.fileSystem, parent.versionsController);
  }

  @Override
  public void removeChildPage(final String name) {
    WikiPage childPage = getChildPage(name);
    if (childPage instanceof FileSystemPage) {
      versionsController.delete((FileSystemPage) childPage);
      super.removeChildPage(name);
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

  private boolean hasContentChild(String path) {
    for (String child : fileSystem.list(path)) {
      if (child.equals("content.txt")) return true;
    }
    return false;
  }

  private boolean hasHtmlChild(String path) {
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
