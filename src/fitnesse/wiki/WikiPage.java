// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import fitnesse.wikitext.parser.ParsedPage;

public interface WikiPage extends Serializable, Comparable<Object> {
  WikiPage getParent();

  boolean isRoot();

  WikiPage addChildPage(String name);

  boolean hasChildPage(String name);

  WikiPage getChildPage(String name);

  void removeChildPage(String name);

  List<WikiPage> getChildren();

  String getName();

  PageData getData();

  Collection<VersionInfo> getVersions();

  WikiPage getVersion(String versionName);

  String getHtml();

  VersionInfo commit(PageData data);

  PageCrawler getPageCrawler();

  String getVariable(String name);
}



