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
                e.printStackTrace();
                throw new RevisionControlException("Unable to add file : " + fileName, e);
            }
        }
    }

    public void checkin(String... filePaths) throws RevisionControlException {
        SVNCommitClient commitClient = manager.getCommitClient();
        try {
            commitClient.doCommit(files(filePaths), false, "Auto Commit", false, true);
        } catch (SVNException e) {
            e.printStackTrace();
            throw new RevisionControlException("Unable to commit files : " + asString(filePaths), e);
        }
    }

    private String asString(String[] filePaths) {
        StringBuilder output = new StringBuilder();
        for (String filePath : filePaths)
            output.append(filePath).append("\n");
        return output.toString();
    }

    public void checkout(String... filePaths) throws RevisionControlException {
        throw new RevisionControlException("This operation is currently not supported");
    }

    public State checkState(String... filePaths) throws RevisionControlException {
        State finalState = null;
        for (String fileName : filePaths) {
            State state = getState(fileName);
            if (finalState == null) {
                finalState = state;
                continue;
            }
            if (finalState != state)
                throw new RevisionControlException(
                        "Following file should be in the same state, but are in different states. Please check their SVN status and manually sync it up"
                                + asString(filePaths));
        }
        return finalState;
    }

    public void delete(String... filePaths) throws RevisionControlException {
        SVNWCClient client = manager.getWCClient();
        for (File file : files(filePaths))
            try {
                client.doDelete(file, false, false);
            } catch (SVNException e) {
                e.printStackTrace();
                throw new RevisionControlException("Unable to delete file : " + file.getAbsolutePath(), e);
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
                client.doRevert(new File(fileName), false);
            } catch (SVNException e) {
                e.printStackTrace();
                throw new RevisionControlException("Unable to revert file : " + fileName, e);
            }
    }

    public void update(String... filePaths) throws RevisionControlException {
        final SVNUpdateClient client = manager.getUpdateClient();
        client.setIgnoreExternals(true);
        for (String fileName : filePaths) {
            try {
                client.doUpdate(new File(fileName).getParentFile(), SVNRevision.HEAD, true);
            } catch (SVNException e) {
                e.printStackTrace();
                throw new RevisionControlException("Unable to update file : " + fileName, e);
            }
            break;
        }
    }

    private void addEntry(File wcPath) throws SVNException {
        manager.getWCClient().doAdd(wcPath, false, wcPath.isDirectory(), true, false);
    }

    private File[] files(String... filePaths) {
        Set<File> files = new HashSet<File>();
        for (String fileName : filePaths) {
            File file = new File(fileName);
            files.add(file);
            files.add(file.getParentFile());
        }
        return files.toArray(new File[files.size()]);
    }

    private State getState(String fileName) throws RevisionControlException {
        SVNStatusType status;
        try {
            status = getStatus(new File(fileName));
        } catch (SVNException e) {
            if (e.getMessage().contains("is not a working copy"))
                return SVNState.UNKNOWN;
            e.printStackTrace();
            throw new RevisionControlException("Unable to check the status of file : " + fileName, e);
        }
        State state = states.get(status);
        if (state != null)
            return state;
        throwExceptionForUnhaldedStatues(status, fileName);
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

    private boolean notUnderVersionControl(File file) throws RevisionControlException {
        return getState(file.getAbsolutePath()).isNotUnderRevisionControl();
    }

    private void throwExceptionForUnhaldedStatues(SVNStatusType status, String fileName) throws RevisionControlException {
        String errorMsg = errorMsgs.get(status);
        if (errorMsg != null)
            throw new RevisionControlException(fileName + errorMsg);
    }
}
