// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol.svn;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import fitnesse.revisioncontrol.RevisionControlException;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.revisioncontrol.State;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;

public class SVNRevisionController implements RevisionController {
  private final SVNClientManager manager;
  private final Map<SVNStatusType, State> states = new HashMap<SVNStatusType, State>();
  private final Map<SVNStatusType, String> errorMsgs = new HashMap<SVNStatusType, String>();

  public SVNRevisionController() {
    this(new Properties());
  }

  public SVNRevisionController(final Properties properties) {
    SVNRepositoryFactoryImpl.setup();
    FSRepositoryFactory.setup();
    String userName = properties.getProperty("SvnUser");
    String password = properties.getProperty("SvnPassword");
    if (userName == null && password == null) {
      this.manager = SVNClientManager.newInstance();
    } else {
      this.manager = SVNClientManager.newInstance(null, userName, password);
    }
    initializeSVNStatusTypeToStateMap();
    initializeUnhandledSVNStatusTypeToErrorMsgsMap();
  }

  public void add(final String... filePaths) throws RevisionControlException {
    for (String fileName : filePaths) {
      File file = new File(fileName);
      try {
        if (notUnderVersionControl(file)) {
          System.out.println("Trying to add file -- " + file.getAbsolutePath());
          addEntry(file);
        }
      } catch (SVNException e) {
        e.printStackTrace();
        throw new RevisionControlException("Unable to add file : " + fileName, e);
      }
    }
  }

  public void checkin(final String... filePaths) throws RevisionControlException {
    SVNCommitClient commitClient = this.manager.getCommitClient();
    try {
      final File[] files = files(filePaths);
      for (File file : files) {
        final File parentFile = file.getParentFile();
        System.out.println("About to Commit the following file : " + parentFile.getAbsolutePath());
        commitClient.doCommit(new File[]{parentFile}, false, "Auto Commit", false, true);
        break;
      }
    } catch (SVNException e) {
      e.printStackTrace();
      throw new RevisionControlException("Unable to commit files : " + asString(filePaths), e);
    }
  }

  private String asString(final String[] filePaths) {
    StringBuilder output = new StringBuilder();
    for (String filePath : filePaths) {
      output.append(filePath).append("\n");
    }
    return output.toString();
  }

  public void checkout(final String... filePaths) throws RevisionControlException {
    throw new RevisionControlException("This operation is currently not supported");
  }

  public State checkState(final String... filePaths) throws RevisionControlException {
    State finalState = null;
    for (String fileName : filePaths) {
      State state = getState(fileName);
      if (finalState == null) {
        finalState = state;
        continue;
      }
      if (finalState != state) {
        throw new RevisionControlException(
          "Following file should be in the same state, but are in different states. Please check their SVN status and manually sync it up"
            + asString(filePaths));
      }
    }
    return finalState;
  }

  public void delete(final String... filePaths) throws RevisionControlException {
    SVNWCClient client = this.manager.getWCClient();
    for (File file : files(filePaths)) {
      final File parentFile = file.getParentFile();
      try {
        System.out.println("About to delete file : " + parentFile.getAbsolutePath());
        client.doDelete(parentFile, false, false);
        break;
      } catch (SVNException e) {
        e.printStackTrace();
        throw new RevisionControlException("Unable to delete file : " + parentFile.getAbsolutePath(), e);
      }
    }
  }

  public void move(final File src, final File dest) throws RevisionControlException {
    try {
      this.manager.getMoveClient().doMove(src, dest);
    } catch (SVNException e) {
      e.printStackTrace();
      throw new RevisionControlException("Unable to move file : " + src.getAbsolutePath() + " to location " + dest.getAbsolutePath(), e);
    }
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
    return new VersionInfo(page.getName());
  }

  public void prune(final FileSystemPage page) throws Exception {
  }

  public void removeVersion(final FileSystemPage page, final String versionName) throws Exception {
  }

  public void revert(final String... filePaths) throws RevisionControlException {
    SVNWCClient client = this.manager.getWCClient();
    for (String fileName : filePaths) {
      try {
        final File fileToBeReverted = new File(fileName);
        final File parentFile = fileToBeReverted.getParentFile();
        System.out.println("About to revert file : " + parentFile.getAbsolutePath());
        client.doRevert(parentFile, true);
        update(fileName);
        break;
      } catch (SVNException e) {
        e.printStackTrace();
        throw new RevisionControlException("Unable to revert file : " + fileName, e);
      }
    }
  }

  public void update(final String... filePaths) throws RevisionControlException {
    final SVNUpdateClient client = this.manager.getUpdateClient();
    client.setIgnoreExternals(true);
    for (String fileName : filePaths) {
      try {
        final File parentFile = new File(fileName).getParentFile();
        System.out.println("About to update file : " + parentFile.getAbsolutePath());
        client.doUpdate(parentFile, SVNRevision.HEAD, true);
      } catch (SVNException e) {
        e.printStackTrace();
        throw new RevisionControlException("Unable to update file : " + fileName, e);
      }
      break;
    }
  }

  private void addEntry(final File wcPath) throws SVNException {
    this.manager.getWCClient().doAdd(wcPath, false, wcPath.isDirectory(), true, false);
  }

  private File[] files(final String... filePaths) {
    Set<File> files = new HashSet<File>();
    for (String fileName : filePaths) {
      files.add(new File(fileName));
    }
    return files.toArray(new File[files.size()]);
  }

  private State getState(final String fileName) throws RevisionControlException {
    SVNStatusType status;
    try {
      status = getStatus(new File(fileName));
    } catch (SVNException e) {
      if (e.getMessage().contains("is not a working copy")) {
        return SVNState.UNKNOWN;
      }
      e.printStackTrace();
      throw new RevisionControlException("Unable to check the status of file : " + fileName, e);
    }
    State state = this.states.get(status);
    if (state != null) {
      return state;
    }
    throwExceptionForUnhaldedStatues(status, fileName);
    throw new RevisionControlException(fileName + " is in unknow state. Please update the file and try again");
  }

  private SVNStatusType getStatus(final File file) throws SVNException {
    System.out.println("About to get status for : " + file.getAbsolutePath());
    SVNStatus status = this.manager.getStatusClient().doStatus(file, false);
    return status.getContentsStatus();
  }

  private void initializeSVNStatusTypeToStateMap() {
    this.states.put(SVNStatusType.STATUS_UNVERSIONED, SVNState.UNKNOWN);
    this.states.put(SVNStatusType.STATUS_NONE, SVNState.UNKNOWN);
    this.states.put(null, SVNState.UNKNOWN);
    this.states.put(SVNStatusType.STATUS_ADDED, SVNState.ADDED);
    this.states.put(SVNStatusType.STATUS_DELETED, SVNState.DELETED);
    this.states.put(SVNStatusType.STATUS_NORMAL, SVNState.VERSIONED);
    this.states.put(SVNStatusType.STATUS_MODIFIED, SVNState.VERSIONED);
    this.states.put(SVNStatusType.STATUS_REPLACED, SVNState.VERSIONED);
    this.states.put(SVNStatusType.MERGED, SVNState.VERSIONED);
  }

  private void initializeUnhandledSVNStatusTypeToErrorMsgsMap() {
    this.errorMsgs.put(SVNStatusType.STATUS_CONFLICTED, " has Conflicts");
    this.errorMsgs.put(SVNStatusType.STATUS_MISSING, " is missing from your local drive");
    this.errorMsgs.put(SVNStatusType.STATUS_IGNORED, " is marked to be Ignored by SVN. Cannot perform SVN operations on ignored files");
    this.errorMsgs.put(SVNStatusType.STATUS_EXTERNAL, " is an SVN External File. Cannot perform local SVN operatiosn on external files");
    this.errorMsgs.put(SVNStatusType.STATUS_INCOMPLETE, " is marked as incomplete by SVN. Please update the file and try again");
    this.errorMsgs.put(SVNStatusType.STATUS_OBSTRUCTED, " is marked as obstructed by SVN. Please clean up the file/folder and try again");
  }

  private boolean notUnderVersionControl(final File file) throws RevisionControlException {
    return getState(file.getAbsolutePath()).isNotUnderRevisionControl();
  }

  private void throwExceptionForUnhaldedStatues(final SVNStatusType status, final String fileName) throws RevisionControlException {
    String errorMsg = this.errorMsgs.get(status);
    if (errorMsg != null) {
      throw new RevisionControlException(fileName + errorMsg);
    }
  }
}
