package fitnesse.revisioncontrol.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    public SVNRevisionController(Properties properties) {
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
        String userName = properties.getProperty("SvnUser");
        String password = properties.getProperty("SvnPassword");
        if (userName == null && password == null)
            manager = SVNClientManager.newInstance();
        else
            manager = SVNClientManager.newInstance(null, userName, password);
        initializeSVNStatusTypeToStateMap();
        initializeUnhandledSVNStatusTypeToErrorMsgsMap();
    }

    public void add(String... filePaths) throws RevisionControlException {
        for (String fileName : filePaths) {
            File file = new File(fileName);
            try {
                if (notUnderVersionControl(file))
                    addEntry(file);
            } catch (SVNException e) {
                throw new RevisionControlException("Unable to add file : " + fileName, e);
            }
        }
    }

    public void checkin(String... filePaths) throws RevisionControlException {
        SVNCommitClient commitClient = manager.getCommitClient();
        try {
            commitClient.doCommit(files(filePaths), false, "Auto Commit", false, false);
        } catch (SVNException e) {
            throw new RevisionControlException("Unable to commit files : " + filePaths, e);
        }
    }

    public void checkout(String... filePaths) throws RevisionControlException {
        throw new RevisionControlException("This operation is currently not supported");
    }

    public State checkState(String... filePaths) throws RevisionControlException {
        State finalState = SVNState.UNKNOWN;
        for (String fileName : filePaths) {
            State state = getState(fileName);
            if (finalState == null) {
                finalState = state;
                continue;
            }
            if (finalState != state)
                throw new RevisionControlException(
                        "Following file should be in the same state, but are in different states. Please check their SVN status and manually sync it up"
                                + filePaths);
        }
        return finalState;
    }

    public void delete(String... filePaths) throws RevisionControlException {
        SVNWCClient client = manager.getWCClient();
        for (String fileName : filePaths)
            try {
                client.doDelete(new File(fileName), false, false);
            } catch (SVNException e) {
                throw new RevisionControlException("Unable to delete file : " + fileName, e);
            }
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
        return new VersionInfo(page.getName());
    }

    public void prune(FileSystemPage page) throws Exception {
    }

    public void removeVersion(FileSystemPage page, String versionName) throws Exception {
    }

    public void revert(String... filePaths) throws RevisionControlException {
        SVNWCClient client = manager.getWCClient();
        for (String fileName : filePaths)
            try {
                client.doRevert(new File(fileName), true);
            } catch (SVNException e) {
                throw new RevisionControlException("Unable to revert file : " + fileName, e);
            }
    }

    public void update(String... filePaths) throws RevisionControlException {
        final SVNUpdateClient client = manager.getUpdateClient();
        client.setIgnoreExternals(true);
        for (String fileName : filePaths)
            try {
                client.doUpdate(new File(fileName), SVNRevision.HEAD, false);
            } catch (SVNException e) {
                throw new RevisionControlException("Unable to update file : " + fileName, e);
            }
    }

    private void addEntry(File wcPath) throws SVNException {
        manager.getWCClient().doAdd(wcPath, false, false, false, true);
    }

    private File[] files(String... filePaths) {
        List<File> files = new ArrayList<File>();
        for (String fileName : filePaths)
            files.add(new File(fileName));
        return files.toArray(new File[files.size()]);
    }

    private State getState(String fileName) throws RevisionControlException {
        try {
            SVNStatusType status = getStatus(new File(fileName));
            State state = states.get(status);
            if (state != null)
                return state;
            throwExceptionForUnhaldedStatues(status, fileName);
        } catch (SVNException e) {
            throw new RevisionControlException("Unable to check the status of file : " + fileName, e);
        }
        throw new RevisionControlException(fileName + " is in unknow state. Please update the file and try again");
    }

    private SVNStatusType getStatus(File file) throws SVNException {
        SVNStatus status = manager.getStatusClient().doStatus(file, false);
        return status.getContentsStatus();
    }

    private void initializeSVNStatusTypeToStateMap() {
        states.put(SVNStatusType.STATUS_UNVERSIONED, SVNState.UNKNOWN);
        states.put(SVNStatusType.STATUS_NONE, SVNState.UNKNOWN);
        states.put(null, SVNState.UNKNOWN);
        states.put(SVNStatusType.STATUS_ADDED, SVNState.ADDED);
        states.put(SVNStatusType.STATUS_DELETED, SVNState.DELETED);
        states.put(SVNStatusType.STATUS_NORMAL, SVNState.VERSIONED);
        states.put(SVNStatusType.STATUS_MODIFIED, SVNState.VERSIONED);
        states.put(SVNStatusType.STATUS_REPLACED, SVNState.VERSIONED);
        states.put(SVNStatusType.MERGED, SVNState.VERSIONED);
    }

    private void initializeUnhandledSVNStatusTypeToErrorMsgsMap() {
        errorMsgs.put(SVNStatusType.STATUS_CONFLICTED, " has Conflicts");
        errorMsgs.put(SVNStatusType.STATUS_MISSING, " is missing from your local drive");
        errorMsgs.put(SVNStatusType.STATUS_IGNORED, " is marked to be Ignored by SVN. Cannot perform SVN operations on ignored files");
        errorMsgs.put(SVNStatusType.STATUS_EXTERNAL, " is an SVN External File. Cannot perform local SVN operatiosn on external files");
        errorMsgs.put(SVNStatusType.STATUS_INCOMPLETE, " is marked as incomplete by SVN. Please update the file and try again");
        errorMsgs.put(SVNStatusType.STATUS_OBSTRUCTED, " is marked as obstructed by SVN. Please clean up the file/folder and try again");
    }

    private boolean notUnderVersionControl(File file) throws SVNException {
        return SVNStatusType.UNKNOWN.equals(getStatus(file));
    }

    private void throwExceptionForUnhaldedStatues(SVNStatusType status, String fileName) throws RevisionControlException {
        String errorMsg = errorMsgs.get(status);
        if (errorMsg != null)
            throw new RevisionControlException(fileName + errorMsg);
    }
}
