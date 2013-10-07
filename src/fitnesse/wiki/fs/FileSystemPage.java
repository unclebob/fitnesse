// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.WikiWordPath;

public class FileSystemPage extends BaseWikiPage {
  private static final long serialVersionUID = 1L;

  // Only used for root page:
  private final String path;

  private final transient FileSystem fileSystem;
  private final transient VersionsController versionsController;
  private boolean autoCommit;

  public FileSystemPage(final String path, final String name, final FileSystem fileSystem,
                        final VersionsController versionsController, final SymbolicPageFactory symbolicPageFactory,
                        final VariableSource variableSource) {
    super(name, symbolicPageFactory, variableSource);
    this.path = path;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  public FileSystemPage(final String name, final FileSystemPage parent) {
    super(name, parent);
    path = null;
    fileSystem = parent.fileSystem;
    versionsController = parent.versionsController;
    autoCommit = parent.autoCommit;
  }

  public FileSystemPage(final String name, final FileSystemPage parent,
                        final FileSystem fileSystem, final VersionsController versionsController) {
    super(name, parent);
    path = null;
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  @Override
  public void removeChildPage(final String name) {
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
  public WikiPage addChildPage(String name) {
    String path = getFileSystemPath() + "/" + name;
    if (hasContentChild(path)) {
      return new FileSystemPage(name, this);
    } else if (hasHtmlChild(path)) {
      return new ExternalSuitePage(path, name, this, fileSystem);
    } else {
      FileSystemPage page = new FileSystemPage(name, this);
      if (autoCommit) page.commit(page.getData());
      return page;
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
  public List<WikiPage> getNormalChildren() {
    final String thisDir = getFileSystemPath();
    final List<WikiPage> children = new ArrayList<WikiPage>();
    if (fileSystem.exists(thisDir)) {
      final String[] subFiles = fileSystem.list(thisDir);
      for (final String subFile : subFiles) {
        if (fileIsValid(subFile, thisDir)) {
          children.add(getChildPage(subFile));
        }
      }
    }
    return children;
  }

  @Override
  protected WikiPage getNormalChildPage(String pageName) {
    final String file = getFileSystemPath() + "/" + pageName;
    if (fileSystem.exists(file)) {
      return addChildPage(pageName);
    }
    return null;
  }

  @Override
  public PageData getData() {
    PageData pageData = versionsController.getRevisionData(this, null);
    return new PageData(pageData, getVariableSource());
  }

  @Override
  public ReadOnlyPageData readOnlyData() {
    return getData();
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
    // Note: RecentChanges is not handled by the versionsController?
    VersionInfo versionInfo = versionsController.makeVersion(this, data);
    return versionInfo;
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return (Collection<VersionInfo>) versionsController.history(this);
  }

  @Override
  public PageData getDataVersion(final String versionName) {
    return new PageData(versionsController.getRevisionData(this, versionName), getVariableSource());
  }

  @Override
  public String toString() {
    try {
      return getClass().getName() + " at " + this.getFileSystemPath();
    } catch (final Exception e) {
      return super.toString();
    }
  }

  public void autoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }
}
