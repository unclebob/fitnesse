// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import util.Clock;
import util.FileSystem;

public class InMemoryPage extends CommitingPage {
  private static final long serialVersionUID = 1L;

  protected static final String currentVersionName = "current_version";

  protected Map<String, PageData> versions = new ConcurrentHashMap<String, PageData>();
  protected Map<String, WikiPage> children = new ConcurrentHashMap<String, WikiPage>();

    public InMemoryPage(String rootPath, String rootPageName, VersionsController versionsController) {
      this(rootPageName, null);
    }

    public InMemoryPage(String rootPath, String rootPageName, FileSystem fileSystem, VersionsController versionsController) {
      this(rootPageName, null);
    }

  protected InMemoryPage(String name, WikiPage parent) {
    super(name, parent);
    versions.put(currentVersionName, new PageData(this, ""));
  }

  public WikiPage addChildPage(String name) {
    WikiPage page = createChildPage(name);
    children.put(name, page);
    return page;
  }

  public static WikiPage makeRoot(String name) {
    return new InMemoryPage(name, null);
  }

  protected WikiPage createChildPage(String name) {
    BaseWikiPage newPage = new InMemoryPage(name, this);
    children.put(newPage.getName(), newPage);
    return newPage;
  }

  public void removeChildPage(String name) {
    children.remove(name);
  }

  public boolean hasChildPage(String pageName) {
    return children.containsKey(pageName);
  }

  protected VersionInfo makeVersion() {
    PageData current = getDataVersion(currentVersionName);

    String name = String.valueOf(VersionInfo.nextId());
    VersionInfo version = makeVersionInfo(current, name);
    versions.put(version.getName(), current);
    return version;
  }

  protected WikiPage getNormalChildPage(String name) {
    return children.get(name);
  }

  public List<WikiPage> getNormalChildren() {
    return new LinkedList<WikiPage>(children.values());
  }

  public PageData getData() {
    return new PageData(getDataVersion(currentVersionName));
  }

  public ReadOnlyPageData readOnlyData() {
      return getDataVersion(currentVersionName);
  }

  public void doCommit(PageData newData) {
    newData.setWikiPage(this);
    newData.getProperties().setLastModificationTime(Clock.currentDate());
    versions.put(currentVersionName, newData);
  }

  public PageData getDataVersion(String versionName) {
    PageData version = versions.get(versionName);
    if (version == null)
      throw new NoSuchVersionException("There is no version '" + versionName + "'");

    Set<String> names = new HashSet<String>(versions.keySet());
    names.remove(currentVersionName);
    List<VersionInfo> pageVersions = new LinkedList<VersionInfo>();
    for (String name : names) {
      PageData data = versions.get(name);
      pageVersions.add(makeVersionInfo(data, name));
    }
    version.addVersions(pageVersions);
    return new PageData(version);
  }

  public int numberOfVersions() {
    return versions.size() - 1;
  }

  protected VersionInfo makeVersionInfo(PageData current, String name) {
    String author = current.getAttribute(PageData.LAST_MODIFYING_USER);
    if (author == null)
      author = "";
    Date date = current.getProperties().getLastModificationTime();
    return new VersionInfo(name, author, date);
  }
}
