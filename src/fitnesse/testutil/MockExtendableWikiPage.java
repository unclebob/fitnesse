// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.util.List;

import fitnesse.wiki.*;

public class MockExtendableWikiPage extends ExtendableWikiPage {
  private static final long serialVersionUID = 1L;

  public MockExtendableWikiPage(Extension e) {
    super("blah", null);
    addExtention(e);
  }

  public WikiPage getParent() {
    return null;
  }

  public WikiPage addChildPage(String name) {
    return null;
  }

  public boolean hasChildPage(String name) {
    return false;
  }

  public WikiPage getNormalChildPage(String name) {
    return null;
  }

  public void removeChildPage(String name) {
  }

  public List<WikiPage> getNormalChildren() {
    return null;
  }

  public String getName() {
    return null;
  }

  public PageData getData() {
    return null;
  }

  public ReadOnlyPageData readOnlyData() { return getData(); }

  public PageData getDataVersion(String versionName) {
    return null;
  }

  public VersionInfo commit(PageData data) {
    return null;
  }

  public int compareTo(Object o) {
    return 0;
  }
}
