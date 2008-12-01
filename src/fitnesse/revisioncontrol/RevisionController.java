package fitnesse.revisioncontrol;

import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;

import java.io.File;
import java.util.Collection;

public interface RevisionController {

  public void add(String... filePaths) throws RevisionControlException;

  public void checkin(String... filePaths) throws RevisionControlException;

  public void checkout(String... filePaths) throws RevisionControlException;

  public void delete(String... filePaths) throws RevisionControlException;

  public void revert(String... filePaths) throws RevisionControlException;

  public State checkState(String... filePaths) throws RevisionControlException;

  public void update(String... filePaths) throws RevisionControlException;

  public void move(File src, File dest) throws RevisionControlException;

  public PageData getRevisionData(FileSystemPage page, String label) throws Exception;

  public Collection<VersionInfo> history(FileSystemPage page) throws Exception;

  public VersionInfo makeVersion(FileSystemPage page, PageData data) throws Exception;

  public void removeVersion(FileSystemPage page, String versionName) throws Exception;

  public void prune(FileSystemPage page) throws Exception;

  public boolean isExternalReversionControlEnabled();
}
