// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.util.Collection;
import java.util.List;

public interface WikiPage extends Comparable<Object> {

  WikiPage getParent();

  boolean isRoot();

  WikiPage addChildPage(String name);

  boolean hasChildPage(String name);

  WikiPage getChildPage(String name);

  void removeChildPage(String name);

  /**
   * Get child pages for this wiki page
   * @return children, an empty list if there are none.
   */
  List<WikiPage> getChildren();

  String getName();

  PageData getData();

  /**
   * Get a list/set of version info
   * @return a collection, never null.
   */
  Collection<VersionInfo> getVersions();

  WikiPage getVersion(String versionName);

  String getHtml();

  /**
   * Commit new content
   * @param data
   * @return version information about this new data version, may be null.
   */
  VersionInfo commit(PageData data);

  PageCrawler getPageCrawler();

  String getVariable(String name);
}



