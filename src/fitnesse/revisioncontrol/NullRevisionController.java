// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.UNKNOWN;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;

public class NullRevisionController implements RevisionController {

  public NullRevisionController() {
    this(new Properties());
  }

  public NullRevisionController(final Properties properties) {
  }

  public void add(final String... filePaths) throws RevisionControlException {
  }

  public void checkin(final String... filePaths) throws RevisionControlException {
  }

  public void checkout(final String... filePaths) throws RevisionControlException {
  }

  public State checkState(final String... filePaths) throws RevisionControlException {
    return UNKNOWN;
  }

  public void delete(final String... filePaths) throws RevisionControlException {
  }

  public void move(final File src, final File dest) throws RevisionControlException {
  }

  public PageData getRevisionData(final FileSystemPage page, final String label) throws Exception {
    return page.getData();
  }

  public Collection<VersionInfo> history(final FileSystemPage page) throws Exception {
    return new HashSet<VersionInfo>();
  }

  public boolean isExternalReversionControlEnabled() {
    return true;
  }

  public VersionInfo makeVersion(final FileSystemPage page, final PageData data) throws Exception {
    return new VersionInfo(page.getFileSystemPath());
  }

  public void prune(final FileSystemPage page) {
  }

  public void removeVersion(final FileSystemPage page, final String versionName) throws Exception {
  }

  public void revert(final String... filePaths) throws RevisionControlException {
  }

  public void update(final String... filePaths) throws RevisionControlException {
  }
}
