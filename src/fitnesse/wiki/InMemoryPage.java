// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.ComponentFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPage extends CommitingPage {
  private static final long serialVersionUID = 1L;

  protected static final String currentVersionName = "current_version";

  protected Map<String, PageData> versions = new ConcurrentHashMap<String, PageData>();
  protected Map<String, WikiPage> children = new ConcurrentHashMap<String, WikiPage>();

  public InMemoryPage(String rootPath, String rootPageName, ComponentFactory factory) throws Exception {
    this(rootPageName, null);
  }

  protected InMemoryPage(String name, WikiPage parent) throws Exception {
    super(name, parent);
    addExtention(new VirtualCouplingExtension(this));
    versions.put(currentVersionName, new PageData(this, ""));
  }

  public WikiPage addChildPage(String name) throws Exception {
    WikiPage page = createChildPage(name);
    children.put(name, page);
    return page;
  }

  public static WikiPage makeRoot(String name) throws Exception {
    return new InMemoryPage(name, null);
  }

  protected WikiPage createChildPage(String name) throws Exception {
    BaseWikiPage newPage = new InMemoryPage(name, this);
    children.put(newPage.getName(), newPage);
    return newPage;
  }

  public void removeChildPage(String name) throws Exception {
    children.remove(name);
  }

  public boolean hasChildPage(String pageName) {
    return children.containsKey(pageName);
  }

  protected VersionInfo makeVersion() throws Exception {
    PageData current = getDataVersion(currentVersionName);

    String name = String.valueOf(VersionInfo.nextId());
    VersionInfo version = makeVersionInfo(current, name);
    versions.put(version.getName(), current);
    return version;
  }

  protected WikiPage getNormalChildPage(String name) throws Exception {
    return children.get(name);
  }

  public List<WikiPage> getNormalChildren() {
    return new LinkedList<WikiPage>(children.values());
  }

  public PageData getData() throws Exception {
    return new PageData(getDataVersion(currentVersionName));
  }

  public void doCommit(PageData newData) throws Exception {
    newData.setWikiPage(this);
    newData.getProperties().setLastModificationTime(new Date());
    versions.put(currentVersionName, newData);
  }

  public PageData getDataVersion(String versionName) throws Exception {
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

  protected VersionInfo makeVersionInfo(PageData current, String name) throws Exception {
    String author = current.getAttribute(WikiPage.LAST_MODIFYING_USER);
    if (author == null)
      author = "";
    Date date = current.getProperties().getLastModificationTime();
    return new VersionInfo(name, author, date);
  }
}
