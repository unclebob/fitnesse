// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.io.Serializable;
import java.util.List;


public interface WikiPage extends Serializable, Comparable<Object> {
  public WikiPage getParent() throws Exception;

  public WikiPage getParentForVariables() throws Exception;

  public void setParentForVariables(WikiPage parent);

  public WikiPage addChildPage(String name) throws Exception;

  public boolean hasChildPage(String name) throws Exception;

  public WikiPage getChildPage(String name) throws Exception;

  public void removeChildPage(String name) throws Exception;

  public List<WikiPage> getChildren() throws Exception;

  public String getName() throws Exception;

  public PageData getData() throws Exception;

  public PageData getDataVersion(String versionName) throws Exception;

  public VersionInfo commit(PageData data) throws Exception;

  public PageCrawler getPageCrawler();

  public WikiPage getHeaderPage() throws Exception;

  public WikiPage getFooterPage() throws Exception;
  //TODO Delete these method alone with ProxyPage when the time is right.
  public boolean hasExtension(String extensionName);

  public Extension getExtension(String extensionName);

  public String getHelpText() throws Exception;

  public List<WikiPageAction> getActions() throws Exception;
}



