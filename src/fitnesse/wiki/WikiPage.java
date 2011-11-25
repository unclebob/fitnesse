// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.io.Serializable;
import java.util.List;


public interface WikiPage extends Serializable, Comparable<Object> {
  public WikiPage getParent();

  public WikiPage getParentForVariables();

  public void setParentForVariables(WikiPage parent);

  public WikiPage addChildPage(String name);

  public boolean hasChildPage(String name);

  public WikiPage getChildPage(String name);

  public void removeChildPage(String name);

  public List<WikiPage> getChildren();

  public String getName();

  public PageData getData();

  public PageData getDataVersion(String versionName);

  public VersionInfo commit(PageData data);

  public PageCrawler getPageCrawler();

  public WikiPage getHeaderPage();

  public WikiPage getFooterPage();
  //TODO Delete these method alone with ProxyPage when the time is right.
  public boolean hasExtension(String extensionName);

  public Extension getExtension(String extensionName);

  public String getHelpText();

  public List<WikiPageAction> getActions() throws Exception;
}



