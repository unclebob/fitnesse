package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.ADDED;
import static fitnesse.revisioncontrol.NullState.DELETED;
import static fitnesse.revisioncontrol.NullState.UNKNOWN;
import static fitnesse.revisioncontrol.NullState.VERSIONED;

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

    public NullRevisionController(Properties properties) {
    }

    public State add(String... filePaths) throws RevisionControlException {

        return ADDED;
    }

    public State checkin(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    public State checkout(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    public State checkState(String... filePaths) throws RevisionControlException {

        return UNKNOWN;
    }

    public State delete(String... filePaths) throws RevisionControlException {

        return DELETED;
    }

    public State execute(RevisionControlOperation operation, String... filePaths) throws RevisionControlException {
        return UNKNOWN;
    }

    public PageData getRevisionData(FileSystemPage page, String label) throws Exception {

        return page.getData();
    }

    public State getState(String state) {

        return UNKNOWN;
    }

    public Collection<VersionInfo> history(FileSystemPage page) throws Exception {

        return new HashSet<VersionInfo>();
    }

    public boolean isExternalReversionControlEnabled() {
        return true;
    }

    public VersionInfo makeVersion(FileSystemPage page, PageData data) throws Exception {

        return new VersionInfo(page.getFileSystemPath());
    }

    public void prune(FileSystemPage page) {
    }

    public void removeVersion(FileSystemPage page, String versionName) throws Exception {

    }

    public State revert(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    public State update(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }
}
