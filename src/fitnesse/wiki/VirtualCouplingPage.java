// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class VirtualCouplingPage implements WikiPage {
  private static final long serialVersionUID = 1L;

  private WikiPage hostPage;
  private HashMap<String, WikiPage> children = new HashMap<String, WikiPage>();

  protected VirtualCouplingPage(WikiPage hostPage) {
    this.hostPage = hostPage;
  }

  public VirtualCouplingPage(WikiPage hostPage, WikiPage proxy) throws Exception {
    this.hostPage = hostPage;
    List<?> proxyChildren = proxy.getChildren();
    for (Iterator<?> iterator = proxyChildren.iterator(); iterator.hasNext();) {
      CommitingPage wikiPage = (CommitingPage) iterator.next();
      wikiPage.parent = this;
      children.put(wikiPage.getName(), wikiPage);
    }
  }

  public boolean hasChildPage(String pageName) throws Exception {
    return children.containsKey(pageName);
  }

  public PageData getData() throws Exception {
    return hostPage.getData();
  }

  public int compareTo(Object o) {
    return 0;
  }

  public WikiPage addChildPage(String name) throws Exception {
    return null;
  }

  public void removeChildPage(String name) throws Exception {
  }

  public PageData getDataVersion(String versionName) throws Exception {
    return null;
  }

  public WikiPage getParent() throws Exception {
    return hostPage.getParent();
  }

  public void setParentForVariables(WikiPage parent) {
    hostPage.setParentForVariables(parent);
  }

  public WikiPage getParentForVariables() throws Exception {
    return hostPage.getParentForVariables();
  }

  public String getName() throws Exception {
    return hostPage.getName();
  }

  public WikiPage getChildPage(String name) throws Exception {
    WikiPage subpage = children.get(name);
    if (subpage == null) subpage = hostPage.getChildPage(name);
    return subpage;
  }

  public List<WikiPage> getChildren() throws Exception {
    return new ArrayList<WikiPage>(children.values());
  }

  public VersionInfo commit(PageData data) throws Exception {
    return null;
  }

  public boolean hasExtension(String extensionName) {
    return false;
  }

  public Extension getExtension(String extensionName) {
    return null;
  }

  public PageCrawler getPageCrawler() {
    return hostPage.getPageCrawler();
  }

  public String getHelpText() throws Exception {
    return "Virtual coupling help text";
  }
}