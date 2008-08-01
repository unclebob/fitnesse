package fitnesse.revisioncontrol;

import java.util.Collection;

import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;

public interface RevisionController {

    public State add(String... filePaths) throws RevisionControlException;

    public State checkin(String... filePaths) throws RevisionControlException;

    public State checkout(String... filePaths) throws RevisionControlException;

    public State delete(String... filePaths) throws RevisionControlException;

    public State revert(String... filePaths) throws RevisionControlException;

    public State checkState(String... filePaths) throws RevisionControlException;

    public State update(String... filePaths) throws RevisionControlException;

    public PageData getRevisionData(FileSystemPage page, String label) throws Exception;

    public Collection<VersionInfo> history(FileSystemPage page) throws Exception;

    public VersionInfo makeVersion(FileSystemPage page, PageData data) throws Exception;

    public void removeVersion(FileSystemPage page, String versionName) throws Exception;

    public State getState(String state);

    public void prune(FileSystemPage page) throws Exception;

    public State execute(RevisionControlOperation operation, String... filePaths) throws RevisionControlException;

    public boolean isExternalReversionControlEnabled();
}
