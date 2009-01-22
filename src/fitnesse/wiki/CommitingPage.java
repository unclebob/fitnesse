// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

public abstract class CommitingPage extends ExtendableWikiPage {
  private static final long serialVersionUID = 1L;

  protected CommitingPage(String name, WikiPage parent) {
    super(name, parent);
  }

  protected abstract VersionInfo makeVersion() throws Exception;

  protected abstract void doCommit(PageData data) throws Exception;

  public VersionInfo commit(PageData data) throws Exception {
    VersionInfo previousVersion = makeVersion();
    doCommit(data);
    return previousVersion;
  }

}
