package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.UNKNOWN;

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

    public void add(String... filePaths) throws RevisionControlException {
    }

    public void checkin(String... filePaths) throws RevisionControlException {
    }

    public void checkout(String... filePaths) throws RevisionControlException {
    }

    public State checkState(String... filePaths) throws RevisionControlException {
        return UNKNOWN;
    }

    public void delete(String... filePaths) throws RevisionControlException {
    }

    public PageData getRevisionData(FileSystemPage page, String label) throws Exception {
        return page.getData();
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

    public void revert(String... filePaths) throws RevisionControlException {
    }

    public void update(String... filePaths) throws RevisionControlException {
    }
}
