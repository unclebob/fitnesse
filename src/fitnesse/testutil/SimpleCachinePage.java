// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.util.Collection;
import java.util.Collections;

import fitnesse.wiki.CachingPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import util.Clock;

public class SimpleCachinePage extends CachingPage {
  private static final long serialVersionUID = 1L;

  private PageData data;

  public SimpleCachinePage(String name, WikiPage parent) {
    super(name, parent, null);
  }

  public boolean hasChildPage(String pageName) {
    return hasCachedSubpage(pageName);
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return Collections.emptySet();
  }

  protected WikiPage createChildPage(String name) {
    return new SimpleCachinePage(name, this);
  }

  protected void loadChildren() {
  }

  protected PageData makePageData() {
    if (data == null)
      return new PageData(this, "some content");
    else
      return new PageData(data);
  }

  protected VersionInfo makeVersion() {
    return new VersionInfo("abc", "Jon", Clock.currentDate());
  }

  public VersionInfo commit(PageData data) {
    this.data = data;
    super.commit(data);
    return makeVersion();
  }

  public PageData getDataVersion(String versionName) {
    return new PageData(this, "content from version " + versionName);
  }
}
