// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.util.Collection;
import java.util.List;

/**
 * A wiki page. Wiki pages can have children, are versioned and are transactional.
 */
public interface WikiPage extends Comparable<WikiPage> {

  /**
   * @return the parent of this page. If the page is the root page, returns itself.
   */
  WikiPage getParent();

  /**
   * @return True if this page is the wiki root.
   */
  boolean isRoot();

  /**
   * Create a child page with a given name. Data should still be committed (see {@link #commit(PageData)})
   * for the page to be persisted.
   *
   * @param name new page name
   * @return a new wiki page.
   */
  WikiPage addChildPage(String name);

  boolean hasChildPage(String name);

  WikiPage getChildPage(String name);

  /**
   * Deprecated. Use WikiPage.remove() instead.
   *
   * @param name change page's name
   */
  @Deprecated
  void removeChildPage(String name);

  /**
   * Remove this page.
   */
  void remove();

  /**
   * Get child pages for this wiki page
   *
   * @return children, an empty list if there are none.
   */
  List<WikiPage> getChildren();

  String getName();

  PageData getData();

  /**
   * Get a list/set of version info
   *
   * @return a collection, never null.
   */
  Collection<VersionInfo> getVersions();

  WikiPage getVersion(String versionName);

  String getHtml();

  /**
   * Commit new content
   *
   * @param data PageData to commit
   * @return version information about this new data version, may be null.
   */
  VersionInfo commit(PageData data);

  PageCrawler getPageCrawler();

  String getVariable(String name);
}



