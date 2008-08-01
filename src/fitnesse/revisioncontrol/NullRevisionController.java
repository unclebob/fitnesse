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

    private final Properties properties;

    public NullRevisionController(Properties properties) {
        this.properties = properties;
    }

    public NullRevisionController() {
        this(new Properties());
    }

    @Override
    public State add(String... filePaths) throws RevisionControlException {

        return ADDED;
    }

    @Override
    public State checkState(String... filePaths) throws RevisionControlException {

        return UNKNOWN;
    }

    @Override
    public State checkin(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    @Override
    public State checkout(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    @Override
    public State delete(String... filePaths) throws RevisionControlException {

        return DELETED;
    }

    @Override
    public PageData getRevisionData(FileSystemPage page, String label) throws Exception {

        return page.getData();
    }

    @Override
    public State getState(String state) {

        return UNKNOWN;
    }

    @Override
    public Collection<VersionInfo> history(FileSystemPage page) throws Exception {

        return new HashSet<VersionInfo>();
    }

    @Override
    public VersionInfo makeVersion(FileSystemPage page, PageData data) throws Exception {

        return new VersionInfo(page.getFileSystemPath());
    }

    @Override
    public void removeVersion(FileSystemPage page, String versionName) throws Exception {

    }

    @Override
    public State revert(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    @Override
    public State update(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    @Override
    public void prune(FileSystemPage page) {
    }

    @Override
    public State execute(RevisionControlOperation operation, String... filePaths) throws RevisionControlException {
        return UNKNOWN;
    }

    @Override
    public boolean isExternalReversionControlEnabled() {
        return true;
    }
}
